import java.util.Random;

/**
 * Sequential implementation of brute force n-body simulation.
 * 
 * Compile (Requires JDK version 14 or later): 
 *      javac *.java
 * 
 * Run:
 *      java NBodySimulation [default settings]
 *      java NBodySimulation <numBodies> <numSteps> 
 *      java NBodySimulation <numBodies> <numSteps> -g -r
 * 
 * The flags can be set after the other arguments:
 *      g: the simulation will be shown in a gui.
 *      r: the bodies will be generated in a ring formation around a central, more massive body.
 * 
 * @author: Leo Vainio
 */

public class NBodySimulation {
    private static final int MAX_NUM_BODIES = 240;
    private static final int MAX_NUM_STEPS = 10_000_000;

    private static final double DT = 1.0;
    private static final double G = 6.67e-11;
    private static final double SPACE_RADIUS = 1000_000;
    private static final double MASS = 100.0;

    private GUI gui;
    private Random rng;
    private Settings settings;
    private Timer timer;

    private Body[] bodies;

    /**
     * Parse command line arguments and configure settings of the simulation.
     * 
     * @param args  <numBodies> <numSteps> -g -r
     */
    public static void main(String[] args) {
        int numBodies = MAX_NUM_BODIES;
        int numSteps = MAX_NUM_STEPS;
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
            if (args.length >= 3)
                if (args[2].equals("-g")) 
                    guiToggled = true;
            if (args.length >= 4) 
                if (args[3].equals("-r")) 
                    ringToggled = true;

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            System.exit(1);
        }

        new NBodySimulation(new Settings(numBodies, numSteps, guiToggled, ringToggled, DT, G, MASS, SPACE_RADIUS));
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

        bodies = new Body[settings.numBodies()];
        if (settings.ringToggled()) {
            generateBodiesRing();
        } else {
            generateBodies();
        }

        if (settings.guiToggled()) {
            gui = new GUI(bodies, settings);
        }

        // Run simulation.
        timer = new Timer();
        timer.start();
        simulate();
        timer.stopAndPrint();
    }

    /*
     * Generate bodies randomly within set boundaries.
     */
    private void generateBodies() {
        for (int i = 0; i < bodies.length; i++) {
            double x = rng.nextDouble() * (settings.spaceRadius() * 2);
            double y = rng.nextDouble() * (settings.spaceRadius() * 2);
            double vx = rng.nextDouble() * 25 - 12.5; 
            double vy = rng.nextDouble() * 25 - 12.5; 
            double mass = MASS;
            bodies[i] = new Body(x, y, vx, vy, mass);
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
        bodies[0] = new Body(centerX, centerY, centerVx, centerVy, centerMass);

        // Create the ring of bodies.
        for (int i = 1; i < bodies.length; i++) {
            Vector unit = getRandomUnitVector();

            double magnitude = (r * 0.6) + (r * 0.8 - r * 0.6) * rng.nextDouble(); // min + (max - min) * random
            double x = unit.getX() * magnitude + r;
            double y = unit.getY() * magnitude + r;

            Vector velocity = getOrthogonalVector(unit);
            double vx = velocity.getX() * 10.0;
            double vy = velocity.getY() * 10.0;
            
            bodies[i] = new Body(x, y, vx, vy, settings.mass());
        }
    }

    /*
     * Returns a randomized unit vector.
     */
    private Vector getRandomUnitVector() {
        double angle = rng.nextDouble() * 2 * Math.PI;
        return new Vector(Math.cos(angle), Math.sin(angle));
    }

    /*
     * Returns a vector that is orthogonal to the input vector.
     */
    private Vector getOrthogonalVector(Vector vec) {
        return new Vector(vec.getY(), -vec.getX());
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
     * Calculates total force for every pair of bodies.
     */
    private void calculateForces() {
        double distance;
        double magnitude;
        double dirX;
        double dirY;

        for (int i = 0; i < settings.numBodies() - 1; i++) {
            for (int j = i + 1; j < settings.numBodies(); j++) {
                Body b1 = bodies[i];
                Body b2 = bodies[j];

                double dx = b1.getX() - b2.getX();
                double dy = b1.getY() - b2.getY();
                distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                magnitude = (G * b1.getMass() * b2.getMass()) / Math.pow(distance, 2.0);
                dirX = b2.getX() - b1.getX();
                dirY = b2.getY() - b1.getY();

                b1.setFx(b1.getFx() + magnitude * dirX / distance);
                b2.setFx(b2.getFx() - magnitude * dirX / distance);
                b1.setFy(b1.getFy() + magnitude * dirY / distance);
                b2.setFy(b2.getFy() - magnitude * dirY / distance);
            }
        }
    }        

    /*
     * Calculates new velocity and position for each body.
     */
    private void moveBodies() {
        for (int i = 0; i < settings.numBodies(); i++) {
            Body b = bodies[i];

            double dVx = (b.getFx() / b.getMass()) * DT;
            double dVy = (b.getFy() / b.getMass()) * DT;
            double dPx = (b.getVx() + dVx / 2.0) * DT;
            double dPy = (b.getVy() + dVy / 2.0) * DT;

            b.setVx(b.getVx() + dVx);
            b.setVy(b.getVy() + dVy);
            b.setX(b.getX() + dPx);
            b.setY(b.getY() + dPy);
            b.setFx(0.0);
            b.setFy(0.0);
        }
    }
}