//Listens for incoming TCP connections from other peers.
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener implements Runnable {
    private final int port;
    private final int currentPeerId;
    private final ConnectionManager connectionManager;
    private final ServerSocket serverSocket;
    private volatile boolean running;
    private final PeerLogger logger;
    public Listener(int port, int currentPeerId, ConnectionManager connectionManager, PeerLogger logger) throws IOException {
        this.port = port;
        this.currentPeerId = currentPeerId;
        this.connectionManager = connectionManager;
        this.serverSocket = new ServerSocket(port);
        this.running = true;
        this.logger = logger;
    }
    @Override
    public void run() {
        try {
            System.out.println("Listening on port " + port);
            while (running) {
                Socket socket = serverSocket.accept();
                connectionManager.trackSocket(socket);
                System.out.println("Accepted connection from "
                        + socket.getRemoteSocketAddress());
                Thread connectionThread = new Thread(() -> {
                    PeerConnection connection = null;
                    boolean registered = false;
                    try {
                        socket.setSoTimeout(ConnectionManager.HANDSHAKE_TIMEOUT);
                        connection = new PeerConnection(socket, currentPeerId, logger);
                        connection.sendHandshake();
                        connection.receiveHandshake();
                        socket.setSoTimeout(0);
                        connectionManager.addConnection(connection);
                        registered = true;
                        logger.logConnectionFrom(connection.getRemotePeerId());
                    } catch (IOException e) {
                        System.err.println("Connection error: " + e.getMessage());
                    } finally {
                        connectionManager.releaseSocket(socket);
                        if (!registered) {
                            if (connection != null) {
                                connection.close();
                            } else {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    System.err.println("Error closing socket: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
                connectionThread.start();
            }
        } catch (IOException e) {
            if (running) {
                logger.logListenerError(e.getMessage());
                connectionManager.stop();
            }
        } finally {
            stop();
        }
    }
    public void stop() {
        running = false;
        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing listener: " + e.getMessage());
        }
    }
}