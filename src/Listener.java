//Listens for incoming TCP connections from other peers.
import java.io.*;
import java.net.*;

public class Listener implements Runnable {

    private final int port;
    private final int currentPeerId;
    private final ConnectionManager connectionManager;
    private ServerSocket serverSocket;
    private volatile boolean running;

    public Listener(int port, int currentPeerId, ConnectionManager connectionManager) {
        this.port = port;
        this.currentPeerId = currentPeerId;
        this.connectionManager = connectionManager;
        this.running = false;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;

            System.out.println("Listening on port " + port);

            while (running) {
                Socket socket = serverSocket.accept();

                System.out.println("Accepted connection from "
                        + socket.getRemoteSocketAddress());

                Thread connectionThread = new Thread(() -> {
                    PeerConnection connection = null;
                    boolean registered = false;

                    try {
                        connection = new PeerConnection(socket, currentPeerId);
                        connection.sendHandshake();
                        connection.receiveHandshake();

                        connectionManager.addConnection(connection);
                        registered = true;

                        System.out.println("Connected to peer "
                                + connection.getRemotePeerId());

                    } catch (IOException e) {
                        System.err.println("Connection error: " + e.getMessage());

                    } finally {
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
                System.err.println("Listener error: " + e.getMessage());
            }
        } finally {
            running = false;
        }
    }

    public void stop() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing listener: " + e.getMessage());
        }
    }
}