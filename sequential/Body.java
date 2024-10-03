/**
 * This class stores information about a single body.
 * 
 * @author Leo Vainio
 */

public class Body {
    private Vector position;
    private Vector velocity;
    private Vector force;
    private final double mass;

    public Body(double x, double y, double vx, double vy, double mass) {
        position = new Vector(x, y);
        velocity = new Vector(vx, vy);
        force = new Vector(0.0, 0.0);
        this.mass = mass;
    }

    // ----- GETTERS ----- //
    public double getX() {
        return position.getX();
    }

    public double getY() {
        return position.getY();
    }

    public double getVx() {
        return velocity.getX();
    }

    public double getVy() {
        return velocity.getY();
    }

    public double getFx() {
        return force.getX();
    }

    public double getFy() {
        return force.getY();
    }

    public double getMass() {
        return mass;
    }

    // ----- SETTERS ----- //
    public void setX(double x) {
        position.setX(x);
    }

    public void setY(double y) {
        position.setY(y);
    }

    public void setVx(double vx) {
        velocity.setX(vx);
    }

    public void setVy(double vy) {
        velocity.setY(vy);
    }

    public void setFx(double fx) {
        force.setX(fx);
    }

    public void setFy(double fy) {
        force.setY(fy);
    }
}
