//Reads Common.cfg and PeerInfo.cfg files and loads the data into CommonConfig and PeerInfo objects; identifies which peers to connect to first
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class ConfigurationLoader {
    public static CommonConfig loadCommonConfig(String filePath) throws IOException {
        Map<String, String> settings = new HashMap<>();
        for (String line : Files.readAllLines(Path.of(filePath))) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] settingParts = line.split("\\s+", 2);
            if (settingParts.length != 2) {
                throw new IOException("Invalid configuration line: " + line);
            }
            settings.put(settingParts[0], settingParts[1]);
        }
        try {
            return new CommonConfig(
                Integer.parseInt(settings.get("NumberOfPreferredNeighbors")),
                Integer.parseInt(settings.get("UnchokingInterval")),
                Integer.parseInt(settings.get("OptimisticUnchokingInterval")),
                settings.get("FileName"),
                Long.parseLong(settings.get("FileSize")),
                Integer.parseInt(settings.get("PieceSize"))
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IOException("Invalid Common.cfg: " + exception.getMessage(), exception);
        }
    }
    public static List<PeerInfo> loadPeers(String filePath) throws IOException {
        List<PeerInfo> peerList = new ArrayList<>();
        Set<Integer> peerIds = new HashSet<>();
        int lineNumber = 0;
        for (String line : Files.readAllLines(Path.of(filePath))) {
            lineNumber++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            String[] peerDetails = line.split("\\s+");
            if (peerDetails.length != 4) {
                throw new IOException("Invalid PeerInfo.cfg line " + lineNumber);
            }
            try {
                int peerId = Integer.parseInt(peerDetails[0]);
                int portNumber = Integer.parseInt(peerDetails[2]);
                String hostName = peerDetails[1];
                String fileStatus = peerDetails[3];
                if (!fileStatus.equals("0") && !fileStatus.equals("1")) {
                    throw new IllegalArgumentException("Invalid file status");
                }
                if (!peerIds.add(peerId)) {
                    throw new IllegalArgumentException("Duplicate peer ID: " + peerId);
                }
                peerList.add(new PeerInfo(peerId, hostName, portNumber, fileStatus.equals("1")));
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid peer on line " + lineNumber + ": " + exception.getMessage(), exception);
            }
        }
        if (peerList.isEmpty()) {
            throw new IOException("No peers found in PeerInfo.cfg");
        }
        return peerList;
    }
    public static PeerInfo findPeer(List<PeerInfo> peerList, int peerId) {
        for (PeerInfo peer : peerList) {
            if (peer.getPeerId() == peerId) {
                return peer;
            }
        }
        throw new IllegalArgumentException("Peer ID not found: " + peerId);
    }
    public static List<PeerInfo> getEarlierPeers(List<PeerInfo> peerList, int peerId) {
        List<PeerInfo> earlierPeers = new ArrayList<>();
        for (PeerInfo peer : peerList) {
            if (peer.getPeerId() == peerId) {
                return earlierPeers;
            }
            earlierPeers.add(peer);
        }
        throw new IllegalArgumentException("Peer ID not found: " + peerId);
    }
}
