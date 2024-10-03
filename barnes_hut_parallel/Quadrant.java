/**
 * This class represents a quadrant. The vector "center" contains the centerpoint
 * of this quadrant.
 * 
 * @author Leo Vainio
 */

public class Quadrant {
    private final Vector center; 
    private final double radius;

    /*
     * Create a new quadrant with center point (x,y) and the specified radius. 
     */
    public Quadrant(double x, double y, double radius) {
        this.center = new Vector(x, y);
        this.radius = radius;
    }

    /*
     * Returns true if the body is within the boundaries of this quadrant.
     */
    public boolean containsBody(Body body) {
        if (body.getX() >= center.getX() - radius  &&
            body.getX() < center.getX() + radius &&
            body.getY() >= center.getY() - radius &&
            body.getY() < center.getY() + radius) {
                return true;
        }
        return false;
    }

    // ----- GETTERS ----- //
    public double getX() {
        return center.getX();
    }

    public double getY() {
        return center.getY();
    }

    public double getRadius() {
        return radius;
    }
}
