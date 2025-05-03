package unsw.blackout;

import unsw.utils.Angle;

public class StandardSatellite extends Satellite {
    public StandardSatellite(String id, String type, double height, Angle position) {
        super(id, type, height, position);
    }

    @Override
    public void move() {
        int direction = super.getDirection();
        double height = super.getHeight();
        double position = super.getPosition().toRadians();

        double moveAngle = super.getLinearSpeed() / height;
        double newAngle = position + moveAngle * direction;
        // standard satellites only move in -ve direction
        if (newAngle < 0.0) {
            newAngle = newAngle + Math.PI * 2;
        }
        Angle newPos = Angle.fromRadians(newAngle);
        super.setPosition(newPos);
    }
}
