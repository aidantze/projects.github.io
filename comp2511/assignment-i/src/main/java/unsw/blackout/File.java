package unsw.blackout;

public class File {
    private String filename; // name
    private String data; // currently transferred data
    private String ogContent; // full contents of file
    private int fileSize; // final size for the file
    private boolean isFileComplete;

    public File(String filename, String content) {
        this.filename = filename;
        this.data = content;
        this.ogContent = content;
        this.fileSize = content.length();
        this.isFileComplete = true;
    }

    public String getFilename() {
        return this.filename;
    }

    public boolean isFileComplete() {
        return isFileComplete;
    }

    public int getFileSize() {
        return fileSize;
    }

    public String getData() {
        return data;
    }

    public void setData(String content) {
        this.data = content;
        // fileSize refers to length of ogdata
    }

    // add data to end of file
    public void appendData(String content) {
        this.data += content;
    }

    // remove data from start of file
    public void removeData(int numBytes) {
        this.data = this.data.substring(numBytes);
    }

    public String getOgData() {
        return ogContent;
    }

    public void setFileComplete(boolean fileComplete) {
        this.isFileComplete = fileComplete;
    }
}
