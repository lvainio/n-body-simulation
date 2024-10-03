/**
 * 2D vector class.
 * 
 * @author Leo Vainio
 */

public class Vector {
    private double x;
    private double y;
    
    /*
     * Creates a vector with specified x and y value.
     */
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // ----- GETTERS ----- //
    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    // ----- SETTERS ----- //
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}