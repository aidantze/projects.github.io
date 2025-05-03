package unsw.blackout;

public class Slope {
    private int startAngle;

    public int getStartAngle() {
        return startAngle;
    }

    private int endAngle;

    public int getEndAngle() {
        return endAngle;
    }

    private int gradient;

    public int getGradient() {
        return gradient;
    }

    public Slope(int startAngle, int endAngle, int gradient) {
        this.startAngle = startAngle;
        this.endAngle = endAngle;
        this.gradient = gradient;
    }
}
