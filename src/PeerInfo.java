//Peer information class that stores ID, hostname, port #, and if has complete file
public class PeerInfo {
    private int peerId;
    private String host;
    private int port;
    private boolean hasFile;
    public PeerInfo(int peerId, String host, int port, boolean hasFile) {
        if (peerId <= 0 || host == null || host.isEmpty() || port < 1 || port > 65535) {
            throw new IllegalArgumentException("Error: Invalid peer information.");
        }
        this.peerId = peerId;
        this.host = host;
        this.port = port;
        this.hasFile = hasFile;
    }
    public int getPeerId() {
        return peerId;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    public boolean hasFile() {
        return hasFile;
    }
    @Override
    public String toString() {
        return "Peer " + peerId + " (" + host + ":" + port + "), hasFile=" + hasFile;
    }
}
