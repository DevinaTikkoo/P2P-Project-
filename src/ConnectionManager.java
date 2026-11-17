//Manages incoming and outgoing TCP connections between peers.
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public class ConnectionManager {
    private final PeerInfo currentPeer;
    private final List<PeerInfo> allPeers;
    private final Map<Integer, PeerConnection> connections;
    private final PeerLogger logger;
    private final Set<Socket> pendingSockets = new HashSet<>();
    private static final int CONNECT_TIMEOUT = 2000;
    static final int HANDSHAKE_TIMEOUT = 5000;
    private static final int RETRY_LIMIT = 15;
    private Listener listener;
    private Thread listenerThread;
    private volatile boolean running;
    public ConnectionManager(PeerInfo currentPeer, List<PeerInfo> allPeers, PeerLogger logger) {
        this.currentPeer = currentPeer;
        this.allPeers = allPeers;
        this.connections = new ConcurrentHashMap<>();
        this.logger = logger;
        this.running = false;
    }
    public synchronized void addConnection(PeerConnection connection) throws IOException {
        if (!running) {
            throw new IOException("Connection manager is stopped");
        }
        int peerId = connection.getRemotePeerId();
        if (peerId == currentPeer.getPeerId()) {
            connection.close();
            throw new IOException("A peer cannot connect to itself");
        }
        boolean validPeer = false;
        for (PeerInfo peer : allPeers) {
            if (peer.getPeerId() == peerId) {
                validPeer = true;
                break;
            }
        }
        if (!validPeer) {
            connection.close();
            throw new IOException("Unknown peer ID: " + peerId);
        }
        PeerConnection existingConnection = connections.putIfAbsent(peerId, connection);
        if (existingConnection != null) {
            connection.close();
            throw new IOException("Duplicate connection to peer " + peerId);
        }

        System.out.println("Registered connection to peer " + peerId);
    }
    public synchronized void trackSocket(Socket socket) throws IOException {
        if (!running) {
            socket.close();
            throw new IOException("Connection manager is stopped");
        }
        pendingSockets.add(socket);
    }
    public synchronized void releaseSocket(Socket socket) {
        pendingSockets.remove(socket);
    }
    public void connectToEarlierPeers() throws IOException {
        for (PeerInfo peer : ConfigurationLoader.getEarlierPeers(allPeers, currentPeer.getPeerId())) {
            IOException lastError = null;
            boolean registered = false;
            for (int attempt = 0; attempt < RETRY_LIMIT && running; attempt++) {
                Socket socket = new Socket();
                try {
                    trackSocket(socket);
                    socket.connect(new InetSocketAddress(peer.getHost(), peer.getPort()), CONNECT_TIMEOUT);
                    socket.setSoTimeout(HANDSHAKE_TIMEOUT);
                    PeerConnection connection = new PeerConnection(socket, currentPeer.getPeerId(), logger);
                    connection.sendHandshake();
                    connection.receiveHandshake();
                    if (connection.getRemotePeerId() != peer.getPeerId()) {
                        throw new IOException("Unexpected peer ID: " + connection.getRemotePeerId());
                    }
                    socket.setSoTimeout(0);
                    addConnection(connection);
                    registered = true;
                    logger.logConnectionTo(peer.getPeerId());
                    break;
                } catch (IOException e) {
                    lastError = e;
                } finally {
                    releaseSocket(socket);
                    if (!registered) {
                        socket.close();
                    }
                }
                if (attempt + 1 < RETRY_LIMIT && running) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Connection attempt interrupted", e);
                    }
                }
            }
            if (!registered) {
                throw new IOException("Could not connect to peer " + peer.getPeerId(), lastError);
            }
        }
    }
    public void start() throws IOException {
        synchronized (this) {
            if (running) {
                return;
            }
            listener = new Listener(currentPeer.getPort(), currentPeer.getPeerId(), this, logger);
            running = true;
            listenerThread = new Thread(listener);
            listenerThread.start();
        }
        try {
            connectToEarlierPeers();
        } catch (IOException e) {
            stop();
            throw e;
        }
    }
    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (listener != null) {
            listener.stop();
        }
        for (Socket socket : pendingSockets) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
        pendingSockets.clear();
        for (PeerConnection connection : connections.values()) {
            connection.close();
        }
        connections.clear();
        System.out.println("Stopped connection manager for peer "
                + currentPeer.getPeerId());
    }
}