//Starts a peer using the configuration files and command-line peer ID.
import java.io.IOException;
import java.util.List;

public class peerProcess {
        public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java peerProcess <peerID>");
            return;
        }
        try {
            int peerId = Integer.parseInt(args[0]);
            CommonConfig commonConfig =
                    ConfigurationLoader.loadCommonConfig("Common.cfg");
            List<PeerInfo> allPeers =
                    ConfigurationLoader.loadPeers("PeerInfo.cfg");
            PeerInfo currentPeer =
                    ConfigurationLoader.findPeer(allPeers, peerId);
            PeerLogger logger = new PeerLogger(peerId);
            logger.logStartup(currentPeer.getPort());
            logger.logPeerCount(allPeers.size());
            logger.logPeerInfo(currentPeer);
            logger.logFileInfo(commonConfig);
            ConnectionManager connectionManager = new ConnectionManager(currentPeer, allPeers, logger);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                connectionManager.stop();
                logger.close();
            }));
            System.out.println("Starting peer " + peerId);
            try {
                connectionManager.start();
            } catch (IOException e) {
                connectionManager.stop();
                logger.close();
                System.err.println("Startup error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.err.println("Peer ID must be an integer.");
        }
        catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
        catch (IOException e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
    }
}