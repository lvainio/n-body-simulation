/**
 * Each node in the tree has four children that represents each quadrant. A leaf of this 
 * tree is called an external node and may or may not contain a body. A node contains at 
 * most one body. Internal nodes does however sum up the mass and keep track of the center
 * of mass of all the bodies it contains.
 * 
 * @author Leo Vainio
 */

public class QuadTree {
    private QuadTree northWest = null;
    private QuadTree northEast = null;
    private QuadTree southWest = null;
    private QuadTree southEast = null;
    
    private Quadrant quadrant;
    
    private Body groupBody = null;
    private Body body = null;

    private Settings settings;

    /**
     * Create a new quadtree with quadrant as boundaries.
     * 
     * @param quadrant  Boundaries of the quadrant.
     * @param settings  Settings of the simulation.
     */
    public QuadTree(Quadrant quadrant, Settings settings) {
        this.quadrant = quadrant;
        this.settings = settings;
    }

    /**
     * Insert all the bodies into the quadtree.
     * 
     * @param bodies  All the bodies of the simulation.
     */
    public void insertBodies(Body[] bodies) {
        for (Body body : bodies) {
            insert(body);
        }
    }

    /*
     * Insert a single body into the tree. 
     */
    private void insert(Body body) {
        // External empty node.
        if (this.body == null) {
            this.body = body;
        } 
        // Internal node.
        else if (isInternal()) {
            groupBody.addBody(body);
            insertInQuadrant(body);
        } 
        // External full node.
        else if (isExternal()) {
            double x = quadrant.getX();
            double y = quadrant.getY();
            double r = quadrant.getRadius();

            northWest = new QuadTree(new Quadrant(x - r/2.0, y - r/2.0, r/2.0), settings);
            northEast = new QuadTree(new Quadrant(x + r/2.0, y - r/2.0, r/2.0), settings);
            southWest = new QuadTree(new Quadrant(x - r/2.0, y + r/2.0, r/2.0), settings);
            southEast = new QuadTree(new Quadrant(x + r/2.0, y + r/2.0, r/2.0), settings);

            insertInQuadrant(this.body);
            insertInQuadrant(body);

            double centerX = (this.body.getX()*this.body.getMass() + body.getX()*body.getMass()) / (this.body.getMass() + body.getMass());
            double centerY = (this.body.getY()*this.body.getMass() + body.getY()*body.getMass()) / (this.body.getMass() + body.getMass());

            groupBody = new Body(centerX, centerY, 0.0, 0.0, this.body.getMass()+body.getMass(), settings);
        }   
    }

    /*
     * Insert the body recursively into the correct quadrant.
     */
    private void insertInQuadrant(Body body) {
        if (northWest.getQuadrant().containsBody(body)) {
            northWest.insert(body);
        } 
        else if (northEast.getQuadrant().containsBody(body)) {
            northEast.insert(body);
        } 
        else if (southWest.getQuadrant().containsBody(body)) {
            southWest.insert(body);
        } 
        else if (southEast.getQuadrant().containsBody(body)) {
            southEast.insert(body);
        }
    }

    /**
     * Calculate the forces exerted on all bodies.
     * 
     * @param bodies  All the bodies of the simulation.
     */
    public void calculateForces(Body[] bodies) {
        for (Body body : bodies) {
            calculateForce(body);
        }
    }

    /*
     * Calculate the force exerted on a single body by traversing the tree and
     * adding up the total force.
     */
    private void calculateForce(Body body) {
        if (isExternal() && body != this.body && this.body != null) {
            body.addForce(this.body);
        } 
        else if (isInternal()) {
            double s = quadrant.getRadius() * 2;
            double dx = body.getX()-groupBody.getX();
            double dy = body.getY()-groupBody.getY();
            double d = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
            if (s / d < settings.theta()) {
                body.addForce(this.groupBody);
            } 
            else {
                if (northWest != null)
                    northWest.calculateForce(body);
                if (northEast != null)
                    northEast.calculateForce(body);
                if (southWest != null)
                    southWest.calculateForce(body);
                if (southEast != null)
                    southEast.calculateForce(body);
            }
        }
    }

    /*
     * Returns true iff the node is internal. A node is internal if it has
     * subtrees.
     */
    private boolean isInternal() {
        if (northWest != null) { // arbitrary choice of quadrant.
            return true;
        }
        return false;
    }

    /*
     * Returns true iff the node is external. A node is external if it has no
     * subtrees, i.e. the node is a leaf.
     */
    private boolean isExternal() {
        if (northWest == null) { // arbitrary choice of quadrant.
            return true;
        }
        return false;
    }

    /*
    * Returns the quadrant of this node.
    */
    private Quadrant getQuadrant() {
        return this.quadrant;
    }
}
