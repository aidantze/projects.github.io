package unsw.blackout;

import unsw.utils.Angle;

public class TeleportingSatellite extends Satellite {
    private boolean hasTeleported;

    public TeleportingSatellite(String id, String type, double height, Angle position) {
        super(id, type, height, position);
    }

    @Override
    public void move() {
        double height = super.getHeight();
        double position = super.getPosition().toRadians();
        if (position == 0.0) {
            super.invertDirection();
        }
        int direction = super.getDirection();

        double moveAngle = super.getLinearSpeed() / height;
        double newAngle = position + moveAngle * direction;
        if (newAngle < 0.0) {
            newAngle = newAngle + Math.PI * 2;
            Angle newPos = Angle.fromRadians(newAngle);
            super.setPosition(newPos);
        } else if (checkTeleport(newAngle)) {
            Angle newPos = Angle.fromRadians(0.0);
            super.setPosition(newPos);
            toggleTeleport();
        } else {
            Angle newPos = Angle.fromRadians(newAngle);
            super.setPosition(newPos);
        }
    }

    // check conditions for teleport
    public boolean checkTeleport(double newAngle) {
        double position = super.getPosition().toRadians();
        return (position > Math.PI && newAngle <= Math.PI) || (position < Math.PI && newAngle >= Math.PI);
    }

    @Override
    public boolean hasTeleported() {
        return this.hasTeleported;
    }

    @Override
    public void toggleTeleport() {
        this.hasTeleported = !hasTeleported;
    }
}
