/**
 * This class stores information about a single body.
 * 
 * @author Leo Vainio
 */

 public class Body {
    private Vector position;
    private Vector velocity;
    private Vector force;
    private double mass;

    private Settings settings;

    public Body(double x, double y, double vx, double vy, double mass, Settings settings) {
        position = new Vector(x, y);
        velocity = new Vector(vx, vy);
        force = new Vector(0.0, 0.0);
        this.mass = mass;
        this.settings = settings;
    }

    /**
     * Move the body depending on the force that is exerted on it. Reset the force to 0 after.
     */
    public void move() {
        double dVx = (getFx() / getMass()) * settings.DT();
        double dVy = (getFy() / getMass()) * settings.DT();
        double dPx = (getVx() + dVx / 2.0) * settings.DT();
        double dPy = (getVy() + dVy / 2.0) * settings.DT();

        setVx(getVx() + dVx);
        setVy(getVy() + dVy);
        setX(getX() + dPx);
        setY(getY() + dPy);
        setFx(0.0);
        setFy(0.0);
    }

    /**
     * Add another bodies mass to this body and calculate the new center of mass.
     * 
     * @param body  The body that is to be added.
     */
    public void addBody(Body body) {
        double centerX = (getX()*getMass() + body.getX()*body.getMass()) / (getMass() + body.getMass());
        double centerY = (getY()*getMass() + body.getY()*body.getMass()) / (getMass() + body.getMass());
        setMass(getMass() + body.getMass());
        setX(centerX);
        setY(centerY);
    }

    /**
     * Add the force from the gravitational pull of another body.
     * 
     * @param body  The body which force is to be added.
     */
    public void addForce(Body body) {
        double distance;
        double magnitude;
        double dirX;
        double dirY;

        distance = Math.sqrt(Math.pow(getX()-body.getX(), 2) + Math.pow(getY()-body.getY(), 2));
        magnitude = (settings.G() * getMass() * body.getMass()) / (distance * distance);
        dirX = body.getX() - getX();
        dirY = body.getY() - getY();

        setFx(getFx() + magnitude * dirX / distance);
        setFy(getFy() + magnitude * dirY / distance);
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

    public void setMass(double mass) {
        this.mass = mass;
    }
}
