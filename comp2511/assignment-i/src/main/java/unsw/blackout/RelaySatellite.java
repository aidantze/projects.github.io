package unsw.blackout;

import unsw.utils.Angle;

public class RelaySatellite extends Satellite {
    public RelaySatellite(String id, String type, double height, Angle position) {
        super(id, type, height, position);
    }

    @Override
    public void move() {
        int direction = super.getDirection();
        //double linearSpeed = super.getLinearSpeed();
        double height = super.getHeight();
        double position = super.getPosition().toRadians();

        double moveAngle = super.getLinearSpeed() / height;
        if (!isWithinRange(position)) {
            if (isPastThreshold(position)) {
                // take positive direction
                if (direction == -1) {
                    super.invertDirection();
                    direction = 1;
                }
                double newAngle = position + moveAngle * direction;
                if (newAngle > Math.PI * 2) {
                    newAngle = newAngle - Math.PI * 2;
                }
                Angle newPos = Angle.fromRadians(newAngle);
                super.setPosition(newPos);
            } else {
                // take negative direction
                if (direction == 1) {
                    super.invertDirection();
                    direction = -1;
                }
                double newAngle = position + moveAngle * direction;
                if (newAngle < 0.0) {
                    newAngle = newAngle + Math.PI * 2;
                }
                Angle newPos = Angle.fromRadians(newAngle);
                super.setPosition(newPos);
            }
        } else {
            // within range: take direction of direction
            double newAngle = position + moveAngle * direction;
            Angle newPos = Angle.fromRadians(newAngle);
            super.setPosition(newPos);
        }
    }

    // checks if angle (in radians) is within relay bounds [140, 190]
    public boolean isWithinRange(double angle) {
        return angle <= (190 * Math.PI / 180) && angle >= (140 * Math.PI / 180);
    }

    // checks if angle (in radians) is past threshold angle 345°
    public boolean isPastThreshold(double angle) {
        return angle >= (345 * Math.PI / 180) || angle < (140 * Math.PI / 180);
    }

}
