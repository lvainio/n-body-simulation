import java.util.Random;

/**
 * Sequential implementation of Barnes-Hut simulation.
 * 
 * Compile:
 *      - javac *.java
 * 
 * Run:
 *      - java NBodySimulation [default settings]
 *      - java NBodySimulation <numBodies> <numSteps> <theta>
 *      - java NBodySimulation <numBodies> <numSteps> <theta> -g -r
 * 
 * Theta value 0.5 is the most commonly used value and 0.0 would turn this into 
 * a brute force simulation. Generally speaking, smaller values yield a more accurate 
 * simulation but higher values makes for a faster simulation.
 * 
 * The flags can be set after the other arguments:
 *      g: the simulation will be shown in a gui.
 *      r: the bodies will be generated in a ring formation around a central, more massive body.
 *      
 * @author: Leo Vainio
 */

public class NBodySimulation {
    private static final int MAX_NUM_BODIES = 500;
    private static final int MAX_NUM_STEPS = 10_000_000;
    private static final double DEFAULT_THETA = 0.5;

    private static final double DT = 1.0;
    private static final double G = 6.67e-11;
    private static final double SPACE_RADIUS = 1_000_000.0;
    private static final double MASS = 100.0;

    private GUI gui;
    private Random rng;
    private Settings settings;
    private Timer timer;

    private Body[] bodies;

    /**
     * Parse command line arguments and configure settings of the simulation.
     * 
     * @param args  <numBodies> <numSteps> <theta> -g -r
     */
    public static void main(String[] args) {
        int numBodies = MAX_NUM_BODIES;
        int numSteps = MAX_NUM_STEPS;
        double theta = DEFAULT_THETA;
        boolean guiToggled = false;
        boolean ringToggled = false;

        try {
            if (args.length >= 1) {
                numBodies = Integer.parseInt(args[0]);
                if (numBodies <= 0 || numBodies > MAX_NUM_BODIES) 
                    numBodies = MAX_NUM_BODIES;
            }
            if (args.length >= 2) {
                numSteps = Integer.parseInt(args[1]);
                if (numSteps <= 0 || numSteps > MAX_NUM_STEPS)
                    numSteps = MAX_NUM_STEPS;
            }
            if (args.length >= 3) {
                theta = Double.parseDouble(args[2]);
            }
            if (args.length >= 4)
                if (args[3].equals("-g")) 
                    guiToggled = true;
            if (args.length >= 5) 
                if (args[4].equals("-r")) 
                    ringToggled = true;

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            System.exit(1);
        }

        new NBodySimulation(new Settings(numBodies, numSteps, theta, guiToggled, ringToggled, DT, G, MASS, SPACE_RADIUS));
    }

    /**
     * Generate bodies and start simulation.
     * 
     * @param settings  The settings of the simulation.
     */
    public NBodySimulation(Settings settings) {
        this.settings = settings;
        System.out.println("\n> Simulating the gravitational n-body problem with the following settings:");
        System.out.println(settings);

        rng = new Random();
        rng.setSeed(System.nanoTime());

        if (settings.ringToggled()) {
            generateBodiesRing();
        } else {
            generateBodies();
        }

        if (settings.guiToggled()) {
            gui = new GUI(bodies, settings);
        }

        // run simulation.
        timer = new Timer();
        timer.start();
        simulate();
        timer.stopAndPrint();
    }

    /*
     * Generate bodies randomly within set boundaries.
     */
    private void generateBodies() {
        bodies = new Body[settings.numBodies()];
        for (int i = 0; i < bodies.length; i++) {
            double x = rng.nextDouble() * (settings.spaceRadius() * 2);
            double y = rng.nextDouble() * (settings.spaceRadius() * 2);
            double vx = rng.nextDouble() * 25 - 12.5; 
            double vy = rng.nextDouble() * 25 - 12.5; 
            double mass = settings.mass();
            bodies[i] = new Body(x, y, vx, vy, mass, settings);
        }
    }

    /*
     * Generate bodies in a ring-like formation with a massive attracting body in the center. 
     */
    private void generateBodiesRing() {
        bodies = new Body[settings.numBodies()];

        // Create the massive body in the center.
        double r = settings.spaceRadius();
        double centerX = r;
        double centerY = r;
        double centerVx = 0.0;
        double centerVy = 0.0;
        double centerMass = 1e18;
        bodies[0] = new Body(centerX, centerY, centerVx, centerVy, centerMass, settings);

        // Create the ring of bodies.
        for (int i = 1; i < bodies.length; i++) {
            Vector unit = getRandomUnitVector();

            double magnitude = (r * 0.6) + (r * 0.8 - r * 0.6) * rng.nextDouble(); // min + (max - min) * random
            double x = unit.getX() * magnitude + r;
            double y = unit.getY() * magnitude + r;

            Vector velocity = getOrthogonalVector(unit);
            double vx = velocity.getX() * 10.0;
            double vy = velocity.getY() * 10.0;
            
            bodies[i] = new Body(x, y, vx, vy, settings.mass(), settings);
        }
    }

    /*
     * Returns a randomized unit vector.
     */
    private Vector getRandomUnitVector() {
        double radians = rng.nextDouble() * 2 * Math.PI;
        return new Vector(Math.cos(radians), Math.sin(radians));
    }

    /*
     * Returns a vector that is orthogonal to the input vector.
     */
    private Vector getOrthogonalVector(Vector v) {
        return new Vector(v.getY(), -v.getX());
    }

    /*
     * Run the simulation for specified number of steps.
     */
    private void simulate() {
        for (int i = 0; i < settings.numSteps(); i++) {
            if (settings.guiToggled()) {
                gui.repaint();
            }
            calculateForces();
            moveBodies();
        }
    }

    /*
     * Build quadtree and compute total force exerted on each body.
     */
    private void calculateForces() {
        Quadrant quadrant = getBoundaries();
        QuadTree quadTree = new QuadTree(quadrant, settings);
        quadTree.insertBodies(bodies);
        quadTree.calculateForces(bodies);
    }

    /*
     * Returns a square quadrant that covers all bodies of the simulation.
     */
    private Quadrant getBoundaries() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (Body body : bodies) {
            if (body.getX() < min) min = body.getX();
            if (body.getX() > max) max = body.getX();
            if (body.getY() < min) min = body.getY();
            if (body.getY() > max) max = body.getY();
        }
        double x = (min + max) / 2;
        double y = (min + max) / 2;
        double radius = ((max - min) / 2) + 100.0; // + 100.0 to make sure bodies on the edge are contained.
        return new Quadrant(x, y, radius);
    }

    /*
     * Move all the bodies depending on the force exerted on each body.
     */
    private void moveBodies() {
        for (Body body : bodies) {
            body.move();
        }
    }
}
