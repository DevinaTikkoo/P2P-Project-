//Manages incoming and outgoing TCP connections between peers.
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final PeerInfo currentPeer;
    private final List<PeerInfo> allPeers;
    private final Map<Integer, PeerConnection> connections;
    private final PeerLogger logger;
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
    public void addConnection(PeerConnection connection) throws IOException {
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
    public void connectToEarlierPeers() {
        for (PeerInfo peer : allPeers) {
            if (peer.getPeerId() == currentPeer.getPeerId()) {
                break;
            }
            try {
                Socket socket = new Socket(peer.getHost(), peer.getPort());
                PeerConnection connection = null;
                boolean registered = false;
                try {
                    connection = new PeerConnection(socket, currentPeer.getPeerId(), logger);
                    connection.sendHandshake();
                    connection.receiveHandshake();
                    if (connection.getRemotePeerId() != peer.getPeerId()) {
                        throw new IOException("Unexpected peer ID: "
                                + connection.getRemotePeerId());
                    }
                    addConnection(connection);
                    registered = true;
                    logger.logConnectionEstablished(peer.getPeerId());
                } finally {
                    if (!registered) {
                        if (connection != null) {
                            connection.close();
                        }
                        else {
                            socket.close();
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not connect to peer "
                        + peer.getPeerId() + ": " + e.getMessage());
            }
        }
    }
    public synchronized void start() {
        if (running) {
            return;
        }
        running = true;
        listener = new Listener(
                currentPeer.getPort(),
                currentPeer.getPeerId(),
                this,
                logger
        );
        listenerThread = new Thread(listener);
        listenerThread.start();
        connectToEarlierPeers();
    }
    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (listener != null) {
            listener.stop();
        }
        for (PeerConnection connection : connections.values()) {
            connection.close();
        }
        connections.clear();
        System.out.println("Stopped connection manager for peer "
                + currentPeer.getPeerId());
    }
}
