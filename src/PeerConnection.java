//Handles communication with another peer through a TCP connection.
import java.io.*;
import java.net.*;

public class PeerConnection {
    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;
    private final int currentPeerId;
    private final PeerLogger logger;
    private int remotePeerId;
    public PeerConnection(Socket socket, int currentPeerId, PeerLogger logger) throws IOException {
        this.socket = socket;
        this.currentPeerId = currentPeerId;
        this.remotePeerId = -1;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
        this.logger = logger;
    }
    public void sendHandshake() throws IOException {
        output.writeBytes("P2PFILESHARINGPROJ");
        for (int i = 0; i < 10; i++) {
            output.writeByte(0);
        }
        output.writeInt(currentPeerId);
        output.flush();
        logger.logHandshakeSent();
    }
    public void receiveHandshake() throws IOException {
        byte[] header = new byte[18];
        input.readFully(header);
        String receivedHeader = new String(header, java.nio.charset.StandardCharsets.US_ASCII);
        if (!receivedHeader.equals("P2PFILESHARINGPROJ")) {
            throw new IOException("Invalid handshake header");
        }
        for (int i = 0; i < 10; i++) {
            if (input.readUnsignedByte() != 0) {
                throw new IOException("Invalid handshake zero bits");
            }
        }
        remotePeerId = input.readInt();
        logger.logHandshakeReceived(remotePeerId);
    }
    public int getRemotePeerId() {
        return remotePeerId;
    }
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}