//take settings from Common.cfg input and calculate # of lines for file
public class CommonConfig {
    private int numberOfPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private long fileSize;
    private int pieceSize;
    
    public CommonConfig(int numberOfPreferredNeighbors, int unchokingInterval, int optimisticUnchokingInterval, String fileName, long fileSize, int pieceSize) {
        if (numberOfPreferredNeighbors < 0 || unchokingInterval <= 0 || optimisticUnchokingInterval <= 0 || fileName == null || fileName.isEmpty() || fileSize <= 0 || pieceSize <= 0) {
            throw new IllegalArgumentException("Invalid configuration values");
        }
        this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
        this.unchokingInterval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
    }
    public int getNumberOfPreferredNeighbors() {
        return numberOfPreferredNeighbors;
    }
    public int getUnchokingInterval() {
        return unchokingInterval;
    }
    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }
    public String getFileName() {
        return fileName;
    }
    public long getFileSize() {
        return fileSize;
    }
    public int getPieceSize() {
        return pieceSize;
    }
    public int getNumberOfPieces() {
        return Math.toIntExact((fileSize - 1) / pieceSize + 1);
    }
    public int getPieceLength(int index) {
        if (index < 0 || index >= getNumberOfPieces()) {
            throw new IllegalArgumentException("Invalid piece index");
        }
        return (int) Math.min(pieceSize, fileSize - (long) index * pieceSize);
    }
}
