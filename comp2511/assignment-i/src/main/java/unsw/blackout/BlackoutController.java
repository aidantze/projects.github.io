package unsw.blackout;

import java.util.ArrayList;
import java.util.List;

import unsw.blackout.FileTransferException.VirtualFileAlreadyExistsException;
import unsw.blackout.FileTransferException.VirtualFileNoBandwidthException;
import unsw.blackout.FileTransferException.VirtualFileNoStorageSpaceException;
import unsw.blackout.FileTransferException.VirtualFileNotFoundException;
import unsw.response.models.EntityInfoResponse;
import unsw.response.models.FileInfoResponse;
import unsw.utils.Angle;

import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;
import static unsw.utils.MathsHelper.getDistance;
import static unsw.utils.MathsHelper.isVisible;

/**
 * The controller for the Blackout system.
 *
 * WARNING: Do not move this file or modify any of the existing method
 * signatures
 *  
 * Please mark task 3 -> moving devices
 */
public class BlackoutController {
    // parameters
    private List<Device> devices = new ArrayList<>(); // list of devices
    private List<Satellite> satellites = new ArrayList<>(); // list of satellites
    private List<FileTransfer> fileTransfers = new ArrayList<>(); // files undergoing transfer
    private List<Slope> slopes = new ArrayList<>(); // list of slopes

    // getters

    // Returns the Device belonging to Id, null if doesn't exist
    public Device getDevice(String id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return device;
            }
        }
        return null;
    }

    // Returns the Device belonging to Id, null if doesn't exist
    public Satellite getSatellite(String id) {
        for (Satellite satellite : satellites) {
            if (satellite.getId().equals(id)) {
                return satellite;
            }
        }
        return null;
    }

    // Return true if given id belongs to a Device
    public boolean isDevice(String id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    // Return the type of the entity the ID belongs to
    public String getType(String id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return device.getType();
            }
        }
        for (Satellite satellite : satellites) {
            if (satellite.getId().equals(id)) {
                return satellite.getType();
            }
        }
        return null;
    }

    public boolean isTeleporting(String id) {
        for (Satellite satellite : satellites) {
            if (satellite.getId().equals(id)) {
                return satellite.getType().equals("TeleportingSatellite");
            }
        }
        return false;
    }

    public boolean isRelay(String id) {
        for (Satellite satellite : satellites) {
            if (satellite.getId().equals(id)) {
                return satellite.getType().equals("RelaySatellite");
            }
        }
        return false;
    }

    public Angle getPosition(String id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return device.getPosition();
            }
        }
        for (Satellite satellite : satellites) {
            if (satellite.getId().equals(id)) {
                return satellite.getPosition();
            }
        }
        return null;
    }

    public double getHeight(String id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                return device.getHeight();
            }
        }
        for (Satellite satellite : satellites) {
            if (satellite.getId().equals(id)) {
                return satellite.getHeight();
            }
        }
        return 0;
    }

    // helper methods
    public boolean contains(List<Device> devs, Device d) {
        for (Device dev : devs) {
            if (dev.equals(d)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(List<Satellite> sats, Satellite s) {
        for (Satellite sat : sats) {
            if (sat.equals(s)) {
                return true;
            }
        }
        return false;
    }

    // task 1 methods

    public void createDevice(String deviceId, String type, Angle position) {
        createDevice(deviceId, type, position, false);
    }

    public void removeDevice(String deviceId) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getId().equals(deviceId)) {
                devices.remove(i);
                break;
            }
        }
    }

    public void createSatellite(String satelliteId, String type, double height, Angle position) {
        switch (type) {
        case "StandardSatellite":
            StandardSatellite standard = new StandardSatellite(satelliteId, type, height, position);
            satellites.add(standard);
            break;
        case "TeleportingSatellite":
            TeleportingSatellite teleport = new TeleportingSatellite(satelliteId, type, height, position);
            satellites.add(teleport);
            break;
        case "RelaySatellite":
            RelaySatellite relay = new RelaySatellite(satelliteId, type, height, position);
            satellites.add(relay);
            break;
        default:
            break;
        }
    }

    public void removeSatellite(String satelliteId) {
        for (int i = 0; i < satellites.size(); i++) {
            if (satellites.get(i).getId().equals(satelliteId)) {
                satellites.remove(i);
                break;
            }
        }
    }

    public List<String> listDeviceIds() {
        List<String> deviceIds = new ArrayList<String>();
        for (Device device : devices) {
            deviceIds.add(device.getId());
        }
        return deviceIds;
    }

    public List<String> listSatelliteIds() {
        List<String> satelliteIds = new ArrayList<String>();
        for (Satellite satellite : satellites) {
            satelliteIds.add(satellite.getId());
        }
        return satelliteIds;
    }

    public void addFileToDevice(String deviceId, String filename, String content) {
        for (Device device : devices) {
            if (device.getId().equals(deviceId)) {
                File file = new File(filename, content);
                device.addFile(file);
            }
        }
    }

    public EntityInfoResponse getInfo(String id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                EntityInfoResponse response = new EntityInfoResponse(device.getId(), device.getPosition(),
                        RADIUS_OF_JUPITER, device.getType(), device.getFileResponses());
                return response;
            }
        }
        for (Satellite satellite : satellites) {
            if (satellite.getId().equals(id)) {
                EntityInfoResponse response = new EntityInfoResponse(satellite.getId(), satellite.getPosition(),
                        satellite.getHeight(), satellite.getType(), satellite.getFileResponses());
                return response;
            }
        }
        return null;
    }

    // task 2 methods

    public void simulate() {
        // Satellite movement
        for (Satellite sat : satellites) {
            sat.move();
        }

        // Movable Device movement
        for (Device dev : devices) {
            if (dev.canMove()) {
                dev.moveDevice();
            }
        }

        if (fileTransfers.size() > 0) {
            transferFiles();
        }
    }

    public void transferFiles() {
        // Check existance and range of entities in file transfer, then transfer files
        for (FileTransfer fileTransfer : fileTransfers) {
            String fromId = fileTransfer.getFromId();
            String toId = fileTransfer.getToId();
            String filename = fileTransfer.getFilename();

            // satellite has teleported out of range of entity
            if (!isDevice(toId) && isTeleporting(toId) && getSatellite(toId).hasTeleported()) {
                if (isDevice(fromId)) {
                    // remove file from teleporting satellite
                    // recover original contents of file in sender, then remove file transfer
                    getSatellite(toId).removeFile(filename);
                    getDevice(fromId).recoverFile(filename);
                    getDevice(fromId).setFileComplete(filename, true);
                    fileTransfers.remove(fileTransfer);
                    getSatellite(toId).toggleTeleport();
                    transferFiles();
                    break;
                }
                // instantly download file onto receiver
                // remove file from sender (which is a satellite)
                String data = getSatellite(fromId).getData(filename);
                getSatellite(toId).appendFile(filename, data);
                getSatellite(fromId).removeFile(filename);
                getSatellite(toId).setFileComplete(filename, true);
                fileTransfers.remove(fileTransfer);
                getSatellite(toId).toggleTeleport();
                transferFiles();
                break;
            } else if (!isDevice(fromId) && isTeleporting(fromId) && getSatellite(fromId).hasTeleported()) {
                // instantly download file onto receiver
                // remove file from sender
                String data = getSatellite(fromId).getData(filename);
                if (isDevice(toId)) {
                    getDevice(toId).appendFile(filename, data);
                    getDevice(toId).setFileComplete(filename, true);
                } else {
                    getSatellite(toId).appendFile(filename, data);
                    getSatellite(toId).setFileComplete(filename, true);
                }
                getSatellite(fromId).removeFile(filename);
                fileTransfers.remove(fileTransfer);
                getSatellite(fromId).toggleTeleport();
                transferFiles();
                break;
            }

            // Satellite no longer in range of entity
            if (!isWithinRange(fromId, toId)) {
                // remove file entirely from recipient. i.e. toId
                if (isDevice(toId)) {
                    getDevice(toId).removeFile(filename);
                } else {
                    getSatellite(toId).removeFile(filename);
                }

                // revive original contents of file in sender, then remove file transfer
                if (isDevice(fromId)) {
                    getDevice(fromId).recoverFile(filename);
                } else {
                    getSatellite(fromId).recoverFile(filename);
                }

                fileTransfers.remove(fileTransfer);
                transferFiles();
                break;
            }

            if (isDevice(fromId) && !isDevice(toId)) {
                // device -> satellite
                int recBytesAlloc = bytesPerFileTransfer(toId, true);

                String data = getDevice(fromId).getData(filename);
                if (data.length() <= recBytesAlloc) {
                    getSatellite(toId).appendFile(filename, data);
                    // transfer complete. Remove from sender
                    getSatellite(toId).setFileComplete(filename, true);
                    getDevice(fromId).removeFile(filename);
                    fileTransfers.remove(fileTransfer);
                    transferFiles();
                    break;
                } else {
                    getSatellite(toId).appendFile(filename, data.substring(0, recBytesAlloc));
                    getDevice(fromId).removeData(filename, recBytesAlloc);
                }

            } else if (!isDevice(fromId) && isDevice(toId)) {
                // satellite -> device
                int sendBytesAlloc = bytesPerFileTransfer(fromId, false);

                String data = getSatellite(fromId).getData(filename);
                if (data.length() <= sendBytesAlloc) {
                    getDevice(toId).appendFile(filename, data);
                    // transfer complete. Remove from sender
                    getDevice(toId).setFileComplete(filename, true);
                    getSatellite(fromId).removeFile(filename);
                    fileTransfers.remove(fileTransfer);
                    transferFiles();
                    break;
                } else {
                    getDevice(toId).appendFile(filename, data.substring(0, sendBytesAlloc));
                    getSatellite(fromId).removeData(filename, sendBytesAlloc);
                }

            } else if (!isDevice(fromId) && !isDevice(toId)) {
                // satellite -> satellite
                int sendBytesAlloc = bytesPerFileTransfer(fromId, false);
                int recBytesAlloc = bytesPerFileTransfer(toId, true);
                int bottleneck = Math.min(sendBytesAlloc, recBytesAlloc);

                String data = getSatellite(fromId).getData(filename);
                if (data.length() <= bottleneck) {
                    getSatellite(toId).appendFile(filename, data);
                    // transfer complete. Remove from sender
                    getSatellite(toId).setFileComplete(filename, true);
                    getSatellite(fromId).removeFile(filename);
                    fileTransfers.remove(fileTransfer);
                    transferFiles();
                    break;
                } else {
                    getSatellite(toId).appendFile(filename, data.substring(0, bottleneck));
                    getSatellite(fromId).removeData(filename, bottleneck);
                }
            }
        }
    }

    public boolean isWithinRange(String fromId, String toId) {
        for (String id : communicableEntitiesInRange(fromId)) {
            if (id.equals(toId)) {
                return true;
            }
        }
        return false;
    }

    // get number of bytes per file involved in file transdfer
    // isFrom: true if id is fromId, false if id is toId
    // isReceiving: true if we want receive bandwidth, false if we want send bandwidth
    // @precondition id must refer to a satellite that's not a relay
    public int bytesPerFileTransfer(String id, boolean isReceiving) {
        double bandwidth;
        double numFilesTransfer;
        if (isReceiving) {
            bandwidth = getSatellite(id).getReceiveLimit();
            numFilesTransfer = fileTransfers.stream().filter(f -> f.getToId().equals(id)).count();
        } else {
            bandwidth = getSatellite(id).getSendLimit();
            numFilesTransfer = fileTransfers.stream().filter(f -> f.getFromId().equals(id)).count();
        }
        return (int) Math.floor(bandwidth / numFilesTransfer);
    }

    /**
     * Simulate for the specified number of minutes. You shouldn't need to modify
     * this function.
     */
    public void simulate(int numberOfMinutes) {
        for (int i = 0; i < numberOfMinutes; i++) {
            simulate();
        }
    }

    public List<String> communicableEntitiesInRange(String id) {
        for (Device device : devices) {
            if (device.getId().equals(id)) {
                if (!device.getType().equals("DesktopDevice")) {
                    return entitiesInDeviceRange(device);
                }
                // desktops cannot communicate with standard satellites
                List<String> entityIds = entitiesInDeviceRange(device);
                for (int i = 0; i < entityIds.size(); i++) {
                    if (getType(entityIds.get(i)).equals("StandardSatellite")) {
                        entityIds.remove(i);
                        i--; // subsequent elements shifted left 1 space
                    }
                }
                return entityIds;
            }
        }
        for (Satellite satellite : satellites) {
            if (satellite.getId().equals(id)) {
                if (!satellite.getType().equals("StandardSatellite")) {
                    return entitiesInSatelliteRange(satellite);
                }
                // standard satellites cannot communicate with desktops
                List<String> entityIds = entitiesInSatelliteRange(satellite);
                for (int i = 0; i < entityIds.size(); i++) {
                    if (getType(entityIds.get(i)).equals("DesktopDevice")) {
                        entityIds.remove(i);
                        i--; // subsequent elements shifted left 1 space
                    }
                }
                return entityIds;
            }
        }
        return null; // raise error
    }

    public List<String> entitiesInSatelliteRange(Satellite satellite) {
        List<String> entitiesInRange = new ArrayList<>();
        List<Satellite> relaySatellitesInRange = new ArrayList<>();
        Angle pos = satellite.getPosition();
        double height = satellite.getHeight();
        double range = satellite.getMaxRange();
        // satellites in range of satellite
        for (Satellite sat : satellites) {
            if (sat.getId().equals(satellite.getId())) {
                continue;
            }
            if (getDistance(sat.getHeight(), sat.getPosition(), height, pos) <= range
                    && isVisible(sat.getHeight(), sat.getPosition(), height, pos)) {
                entitiesInRange.add(sat.getId());
                if (sat.getType().equals("RelaySatellite")) {
                    relaySatellitesInRange.add(sat);
                }
            }
        }
        // devices in range of satellite
        for (Device device : devices) {
            if (getDistance(height, pos, device.getPosition()) <= range
                    && isVisible(height, pos, device.getPosition())) {
                entitiesInRange.add(device.getId());
            }
        }
        // satellites and devices in range via a relay satellite
        for (String entity : entitiesInRelaySatelliteRange(relaySatellitesInRange, new ArrayList<Satellite>(), true)) {
            if (entitiesInRange.contains(entity) || satellite.getId().equals(entity)) {
                continue;
            }
            entitiesInRange.add(entity);
        }
        return entitiesInRange;
    }

    // Recursive function that finds satellites/devices in range of a relay satellite
    // inclDevices: true if we want to include devices to output list, false otherwise
    public List<String> entitiesInRelaySatelliteRange(List<Satellite> relays, List<Satellite> relaysMarked,
            boolean inclDevices) {
        List<Satellite> relaysMarkedR = relaysMarked;
        List<String> entitiesInRange = new ArrayList<>();
        List<Satellite> relaysInRange = new ArrayList<>();

        for (Satellite relay : relays) {
            relaysMarkedR.add(relay);
            Angle pos = relay.getPosition();
            double height = relay.getHeight();
            double range = relay.getMaxRange();

            // satellites in range of relay satellite
            for (Satellite sat : satellites) {
                if (sat.getId().equals(relay.getId()) || contains(relays, sat) || contains(relaysMarkedR, sat)) {
                    continue;
                }
                if (getDistance(sat.getHeight(), sat.getPosition(), height, pos) <= range
                        && isVisible(sat.getHeight(), sat.getPosition(), height, pos)) {
                    entitiesInRange.add(sat.getId());
                    if (sat.getType().equals("RelaySatellite")) {
                        relaysInRange.add(sat);
                    }
                }
            }

            if (inclDevices) {
                // devices in range
                for (Device dev : devices) {
                    if (getDistance(height, pos, dev.getPosition()) <= range
                            && isVisible(height, pos, dev.getPosition())) {
                        entitiesInRange.add(dev.getId());
                    }
                }
            }

            for (String entity : entitiesInRelaySatelliteRange(relaysInRange, relaysMarkedR, inclDevices)) {
                if (entitiesInRange.contains(entity) || relay.getId().equals(entity)) {
                    continue;
                }
                entitiesInRange.add(entity);
            }
        }
        return entitiesInRange;
    }

    public List<String> entitiesInDeviceRange(Device device) {
        // devices cannot communicate with each other unless via a satellite
        List<String> satellitesInRange = new ArrayList<>();
        List<Satellite> relaySatellitesInRange = new ArrayList<>();
        Angle pos = device.getPosition();
        double range = device.getMaxRange();
        // satellites in range of device
        for (Satellite sat : satellites) {
            if (getDistance(sat.getHeight(), sat.getPosition(), pos) <= range
                    && isVisible(sat.getHeight(), sat.getPosition(), pos)) {
                satellitesInRange.add(sat.getId());
                if (sat.getType().equals("RelaySatellite")) {
                    relaySatellitesInRange.add(sat);
                }
            }
        }
        // satellites in range via a relay satellite
        for (String entity : entitiesInRelaySatelliteRange(relaySatellitesInRange, new ArrayList<Satellite>(), false)) {
            if (satellitesInRange.contains(entity)) {
                continue;
            }
            satellitesInRange.add(entity);
        }
        return satellitesInRange;
    }

    public void sendFile(String fileName, String fromId, String toId) throws FileTransferException {
        // FileNotFound and FileAlreadyExists
        if (!getInfo(fromId).getFiles().containsKey(fileName)
                || !getInfo(fromId).getFiles().get(fileName).isFileComplete()) {

            if (!getInfo(toId).getFiles().containsKey(fileName)) {
                throw new VirtualFileNotFoundException(fileName);
            }
            FileInfoResponse tmp = getInfo(toId).getFiles().get(fileName);
            if (tmp.getData().length() != tmp.getFileSize()) {
                throw new VirtualFileNotFoundException(fileName);
            }
            throw new VirtualFileAlreadyExistsException(fileName);
        }
        for (FileTransfer fileTransfer : fileTransfers) {
            if (fileTransfer.getFilename().equals(fileName)) {
                throw new VirtualFileNotFoundException(fileName);
            }
        }

        // FileNoBandwidth
        if (!isDevice(fromId) && !isRelay(fromId)) {
            checkBandwidth(fromId, true);
        }
        if (!isDevice(toId) && !isRelay(toId)) {
            if (isDevice(fromId)) {
                checkBandwidth(toId, false);
            } else {
                checkBandwidth(toId, false);
            }
        }

        // FileAlreadyExists
        if (getInfo(toId).getFiles().containsKey(fileName)) {
            throw new VirtualFileAlreadyExistsException(fileName);
        }

        // FileNoStorageSpace
        if (!isDevice(fromId) && !isRelay(fromId)) {
            checkStorage(fromId, getSatellite(fromId).getData(fileName));
        }
        if (!isDevice(toId) && !isRelay(toId)) {
            if (isDevice(fromId)) {
                checkStorage(toId, getDevice(fromId).getData(fileName));
            } else {
                checkStorage(toId, getSatellite(fromId).getData(fileName));
            }
        }

        FileTransfer newFileTransfer = new FileTransfer(fileName, fromId, toId);
        fileTransfers.add(newFileTransfer);

        // change file status in each FileInfoResponse in fromId and toId
        String ogdata;
        if (isDevice(fromId)) {
            getDevice(fromId).setFileComplete(fileName, false);
            ogdata = getDevice(fromId).getData(fileName);
        } else {
            getSatellite(fromId).setFileComplete(fileName, false);
            ogdata = getSatellite(fromId).getData(fileName);
        }

        // add file in toId to eventually transfer to
        File file = new File(fileName, ogdata);
        file.setData("");
        file.setFileComplete(false);
        if (isDevice(toId)) {
            getDevice(toId).addFile(file);
        } else {
            getSatellite(toId).addFile(file);
        }

    }

    // perform checks for satellite bandwidth
    // isFrom = true if id is fromId, false if id is toId
    // @precondition id must refer to a satellite that's not a relay
    public void checkBandwidth(String id, boolean isFrom) throws FileTransferException {
        int totalTransferFiles = getSatellite(id).getTransferFiles().size();
        int receiveLimit = getSatellite(id).getReceiveLimit();
        int sendLimit = getSatellite(id).getSendLimit();
        //int fileSize = file.getFileSize();
        if (isFrom && totalTransferFiles >= sendLimit) {
            throw new VirtualFileNoBandwidthException(id);
        }
        if (totalTransferFiles >= receiveLimit) {
            throw new VirtualFileNoBandwidthException(id);
        }
    }

    // perform checks for satellite storage
    // @precondition id must refer to a satellite that's not a relay
    public void checkStorage(String id, String content) throws FileTransferException {
        int totalFiles = getSatellite(id).getFiles().size();
        int totalBytes = getSatellite(id).totalBytes();
        int maxBytes = getSatellite(id).getMaxBytes();
        // teleporting satellite has no maxFiles storage limit
        if (!isTeleporting(id)) {
            int maxFiles = getSatellite(id).getMaxFiles();
            if (totalFiles >= maxFiles) {
                throw new VirtualFileNoStorageSpaceException("Max Files Reached");
            }
        }
        if (totalBytes + content.length() > maxBytes) {
            throw new VirtualFileNoStorageSpaceException("Max Storage Reached");
        }
    }

    // Task 3 methods

    public void createDevice(String deviceId, String type, Angle position, boolean isMoving) {
        switch (type) {
        case "HandheldDevice":
            HandheldDevice handheld = new HandheldDevice(deviceId, type, position, isMoving);
            devices.add(handheld);
            break;
        case "LaptopDevice":
            LaptopDevice laptop = new LaptopDevice(deviceId, type, position, isMoving);
            devices.add(laptop);
            break;
        case "DesktopDevice":
            DesktopDevice desktop = new DesktopDevice(deviceId, type, position, isMoving);
            devices.add(desktop);
            break;
        default:
            break;
        }
        // currently does not handle slopes
    }

    public void createSlope(int startAngle, int endAngle, int gradient) {
        Slope slope = new Slope(startAngle, endAngle, gradient);
        slopes.add(slope);
    }
}
