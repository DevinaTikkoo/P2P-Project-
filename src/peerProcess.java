//Starts a peer using the configuration files and command-line peer ID.
import java.io.*;
import java.util.*;

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

            if (currentPeer == null) {
                System.err.println("Peer ID not found: " + peerId);
                return;
            }

            ConnectionManager connectionManager =
                    new ConnectionManager(currentPeer, allPeers);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                connectionManager.stop();
            }));

            System.out.println("Starting peer " + peerId);
            connectionManager.start();

        } catch (NumberFormatException e) {
            System.err.println("Peer ID must be an integer.");
        }
        catch (IOException e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
    }
}
