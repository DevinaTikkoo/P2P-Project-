
import java.io.*;
import java.time.*;
import java.time.format.*;

public class PeerLogger {
    private final int currentPeerId;
    private final PrintWriter writer;
    private final DateTimeFormatter formatter;
    public PeerLogger(int currentPeerId) throws IOException {
        this.currentPeerId = currentPeerId;
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.writer = new PrintWriter(new FileWriter("log_peer_" + currentPeerId + ".log", true));
    }
    public synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String entry = "[" + timestamp + "]: " + message;
        writer.println(entry);
        writer.flush();
        System.out.println(entry);
    }
    public void logStartup(int port) {
        log("Starting peer " + currentPeerId + " on port " + port);
    }
    public void logListening(int port) {
        log("Listening on port " + port);
    }
    public void logConnectionTo(int remotePeerId) {
        log("Peer " + currentPeerId + " makes a connection to Peer " + remotePeerId + ".");
    }
    public void logConnectionFrom(int remotePeerId) {
        log("Peer " + currentPeerId + " is connected from Peer " + remotePeerId + ".");
    }
    public void logRegisteredConnection(int remotePeerId) {
        log("Registered connection to peer " + remotePeerId);
    }
    public void logHandshakeSent() {
        log("Sent handshake from peer " + currentPeerId);
    }
    public void logHandshakeReceived(int remotePeerId) {
        log("Received handshake from peer " + remotePeerId);
    }
    public void logConnectionError(int remotePeerId, String message) {
        log("Could not connect to peer " + remotePeerId + ": " + message);
    }
    public void logListenerError(String message) {
        log("Listener error: " + message);
    }
    public void logFileInfo(CommonConfig commonConfig) {
        log("File name: " + commonConfig.getFileName());
        log("File size: " + commonConfig.getFileSize());
        log("Piece size: " + commonConfig.getPieceSize());
        log("Number of pieces: " + commonConfig.getNumberOfPieces());
    }
    public void logPeerInfo(PeerInfo currentPeer) {
        log("Current peer: " + currentPeer.getPeerId());
        log("Host: " + currentPeer.getHost());
        log("Port: " + currentPeer.getPort());
        log("Has complete file: " + currentPeer.hasFile());
    }
    public void logPeerCount(int peerCount) {
        log("Total peers: " + peerCount);
    }
    public void logConnectionEstablished(int remotePeerId) {
        log("Connection established with peer " + remotePeerId);
    }
    public void logShutdown() {
        log("Stopped connection manager for peer " + currentPeerId);
    }
    public void logConnectionSummary(int connectedPeers, int totalPeers) {
        log("Connection summary for peer " + currentPeerId);
        log("Connected peers: " + connectedPeers);
        log("Total other peers: " + (totalPeers - 1));
        if (connectedPeers == totalPeers - 1) {
            log("All configured peers are connected.");
        } else {
            log("Waiting for additional peer connections.");
        }
    }
    public synchronized void close() {
        writer.flush();
        writer.close();
    }
}
