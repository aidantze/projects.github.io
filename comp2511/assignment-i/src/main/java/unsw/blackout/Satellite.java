package unsw.blackout;

import java.util.ArrayList;
import java.util.List;

import unsw.utils.Angle;

public abstract class Satellite extends Entity {
    // constants
    public static final double STANDARD_RANGE = 150000.0;
    public static final double TELEPORT_RANGE = 200000.0;
    public static final double RELAY_RANGE = 300000.0;

    public static final double STANDARD_SPEED = 2500.0;
    public static final double TELEPORT_SPEED = 1000.0;
    public static final double RELAY_SPEED = 1500.0;

    public static final int STANDARD_RECLIM = 1;
    public static final int TELEPORT_RECLIM = 15;

    public static final int STANDARD_SENDLIM = 1;
    public static final int TELEPORT_SENDLIM = 10;

    public static final int STANDARD_MAXBYTES = 80;
    public static final int TELEPORT_MAXBYTES = 200;

    public static final int STANDARD_MAXFILES = 3;

    // parameters
    private double maxRange; // maximum range to connect to a device in km
    private List<String> deviceTypes; // list of devices the satellite is compatible with
    private double linearSpeed; // speed of satellite in km per minute
    private int receiveLimit; // limit for receiving files in bytes per minute
    private int sendLimit; // limit for sending files in bytes per minute
    private int maxBytes; // max bytes the satellite can store
    private int maxFiles; // max number of files the satellite can store

    public Satellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, type, position, height);

        List<String> deviceTypes = new ArrayList<>();
        deviceTypes.add("HandheldDevice");
        deviceTypes.add("LaptopDevice");

        switch (type) {
        case "StandardSatellite":
            this.maxRange = STANDARD_RANGE;
            this.linearSpeed = STANDARD_SPEED;

            this.deviceTypes = deviceTypes; // does not support desktop devices

            this.receiveLimit = STANDARD_RECLIM;
            this.sendLimit = STANDARD_SENDLIM;
            this.maxBytes = STANDARD_MAXBYTES;
            this.maxFiles = STANDARD_MAXFILES;
            break;
        case "TeleportingSatellite":
            this.maxRange = TELEPORT_RANGE;
            this.linearSpeed = TELEPORT_SPEED;

            deviceTypes.add("DesktopDevice");
            this.deviceTypes = deviceTypes;

            this.receiveLimit = TELEPORT_RECLIM;
            this.sendLimit = TELEPORT_SENDLIM;
            this.maxBytes = TELEPORT_MAXBYTES;
            // can store as many files that can fit into maxBytes
            break;
        case "RelaySatellite":
            this.maxRange = RELAY_RANGE;
            this.linearSpeed = RELAY_SPEED;

            deviceTypes.add("DesktopDevice");
            this.deviceTypes = deviceTypes;

            // relay satellites don't store any files and no bandwidth limits
            break;
        default:
            break;
        }
    }

    public double getMaxRange() {
        return this.maxRange;
    }

    public List<String> getDeviceTypes() {
        return this.deviceTypes;
    }

    public double getLinearSpeed() {
        return this.linearSpeed;
    }

    public int getReceiveLimit() {
        return this.receiveLimit;
    }

    public int getSendLimit() {
        return this.sendLimit;
    }

    public int getMaxBytes() {
        return this.maxBytes;
    }

    public int getMaxFiles() {
        return this.maxFiles;
    }

    public int totalBytes() {
        int totalBytes = 0;
        for (File file : super.getFiles()) {
            totalBytes += file.getFileSize();
        }
        return totalBytes;
    }

    public List<File> getTransferFiles() {
        List<File> transferFiles = new ArrayList<>();
        for (File file : super.getFiles()) {
            if (!file.isFileComplete()) {
                transferFiles.add(file);
            }
        }
        return transferFiles;
    }

    public void move() {
        int direction = super.getDirection();
        double height = super.getHeight();
        double position = super.getPosition().toRadians();

        double moveAngle = linearSpeed / height;
        double newAngle = position + moveAngle * direction;
        // standard satellites only move in -ve direction
        if (newAngle < 0.0) {
            newAngle = newAngle + Math.PI * 2;
        }
        Angle newPos = Angle.fromRadians(newAngle);
        super.setPosition(newPos);
    }

    public boolean hasTeleported() {
        // return this.hasTeleported;
        return true;
    }

    public void toggleTeleport() {
        // this.hasTeleported = !hasTeleported;
    }
}
