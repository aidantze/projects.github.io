package unsw.blackout;

import unsw.utils.Angle;

import static unsw.utils.MathsHelper.RADIUS_OF_JUPITER;

public abstract class Device extends Entity {
    // constants
    public static final double HANDHELD_RANGE = 50000.0;
    public static final double LAPTOP_RANGE = 100000.0;
    public static final double DESKTOP_RANGE = 200000.0;

    public static final double HANDHELD_SPEED = 50.0;
    public static final double LAPTOP_SPEED = 30.0;
    public static final double DESKTOP_SPEED = 20.0;

    // parameters
    private double maxRange; // maximum range to connect to another entity in km
    private double linearSpeed; // speed of entity in km per minute
    private boolean canMove;

    public Device(String deviceId, String type, Angle position, boolean isMoving) {
        super(deviceId, type, position, RADIUS_OF_JUPITER);
        this.canMove = isMoving;
        switch (type) {
        case "HandheldDevice":
            this.maxRange = HANDHELD_RANGE;
            this.linearSpeed = isMoving ? HANDHELD_SPEED : 0.0;
            break;
        case "LaptopDevice":
            this.maxRange = LAPTOP_RANGE;
            this.linearSpeed = isMoving ? LAPTOP_SPEED : 0.0;
            break;
        case "DesktopDevice":
            this.maxRange = DESKTOP_RANGE;
            this.linearSpeed = isMoving ? DESKTOP_SPEED : 0.0;
            break;
        default:
            break;
        }
    }

    public double getMaxRange() {
        return this.maxRange;
    }

    public boolean canMove() {
        return this.canMove;
    }

    public double getLinearSpeed() {
        return this.linearSpeed;
    }

    public void moveDevice() {
        int direction = super.getDirection();
        double height = super.getHeight();
        double position = super.getPosition().toRadians();

        double moveAngle = linearSpeed / height;
        double newAngle = position + moveAngle * direction;

        // devices only move in -ve direction
        if (newAngle < 0.0) {
            newAngle = newAngle + Math.PI * 2;
        }
        Angle newPos = Angle.fromRadians(newAngle);
        super.setPosition(newPos);
    }

}
