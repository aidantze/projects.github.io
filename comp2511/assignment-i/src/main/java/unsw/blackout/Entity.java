package unsw.blackout;

import unsw.response.models.FileInfoResponse;
import unsw.utils.Angle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity {
    private static final int DEFAULT_DIRECTION = -1;

    // parameters
    private String id; // unique id
    private String type; // type of device/satellite
    private Angle position; // position in radians
    private double height; // height in km
    private List<File> files;

    private int direction;

    public Entity(String deviceId, String type, Angle position, double height) {
        this.id = deviceId;
        this.type = type;
        this.position = position;
        this.height = height;
        this.direction = DEFAULT_DIRECTION;
        this.files = new ArrayList<>();
    }

    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public Angle getPosition() {
        return this.position;
    }

    public void setPosition(Angle pos) {
        this.position = pos;
    }

    public double getHeight() {
        return this.height;
    }

    public int getDirection() {
        return this.direction;
    }

    public void invertDirection() {
        this.direction = -direction;
    }

    public Map<String, FileInfoResponse> getFileResponses() {
        Map<String, FileInfoResponse> fileResponses = new HashMap<>();
        for (File file : this.files) {
            FileInfoResponse fileResponse = new FileInfoResponse(file.getFilename(), file.getData(), file.getFileSize(),
                    file.isFileComplete());
            fileResponses.put(file.getFilename(), fileResponse);
        }
        return fileResponses;
    }

    public List<File> getFiles() {
        return this.files;
    }

    public File getFile(String filename) {
        for (File file : this.files) {
            if (file.getFilename().equals(filename)) {
                return file;
            }
        }
        return null;
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    public boolean hasFile(String filename) {
        for (File file : this.files) {
            if (file.getFilename().equals(filename)) {
                return true;
            }
        }
        return false;
    }

    public double getFileSize(String filename) {
        for (File file : this.files) {
            if (file.getFilename().equals(filename)) {
                return file.getFileSize();
            }
        }
        return 0.0;
    }

    public String getData(String filename) {
        for (File file : this.files) {
            if (file.getFilename().equals(filename)) {
                return file.getData();
            }
        }
        return null;
    }

    public void removeFile(String filename) {
        for (File file : this.files) {
            if (file.getFilename().equals(filename)) {
                this.files.remove(file);
                break;
            }
        }
    }

    public void appendFile(String filename, String content) {
        for (File file : this.files) {
            if (file.getFilename().equals(filename)) {
                file.appendData(content);
                break;
            }
        }
    }

    public void removeData(String filename, int numBytes) {
        for (File file : this.files) {
            if (file.getFilename().equals(filename)) {
                file.removeData(numBytes);
                break;
            }
        }
    }

    public void recoverFile(String filename) {
        for (File file : this.files) {
            if (file.getFilename().equals(filename)) {
                file.setData(file.getOgData());
                break;
            }
        }
    }

    public void setFileComplete(String filename, boolean fileComplete) {
        for (File file : this.files) {
            if (file.getFilename().equals(filename)) {
                file.setFileComplete(fileComplete);
                break;
            }
        }
    }
}
