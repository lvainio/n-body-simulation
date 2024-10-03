/**
 * This class represents a quadrant. The vector "center" contains the centerpoint
 * of this quadrant.
 * 
 * @author Leo Vainio
 */

public class Quadrant {
    private Vector center; 
    private double radius;

    /*
     * Create a new quadrant with center point (x,y) and the specified radius. 
     */
    public Quadrant(double x, double y, double radius) {
        center = new Vector(x, y);
        this.radius = radius;
    }

    /*
     * Returns true if the body is within the boundaries of this quadrant.
     */
    public boolean containsBody(Body body) {
        double bx = body.getX();
        double by = body.getY();
        double cx = center.getX();
        double cy = center.getY();

        if (bx >= cx - radius  &&
            bx < cx + radius &&
            by >= cy - radius &&
            by < cy + radius) {
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
