package unsw.blackout;

public class FileTransfer {
    private String filename;
    private String fromId;
    private String toId;

    public FileTransfer(String filename, String fromId, String toId) {
        this.filename = filename;
        this.fromId = fromId;
        this.toId = toId;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getFromId() {
        return this.fromId;
    }

    public String getToId() {
        return this.toId;
    }
}
