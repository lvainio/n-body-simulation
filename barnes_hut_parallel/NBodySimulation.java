import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parallel implementation of Barnes-Hut simulation.
 * 
 * Compile:
 *      - javac *.java
 * 
 * Run:
 *      - java NBodySimulation [default settings]
 *      - java NBodySimulation <numBodies> <numSteps> <theta> <numWorkers>
 *      - java NBodySimulation <numBodies> <numSteps> <theta> <numWorkers> -g -r
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
    private static final int MAX_NUM_WORKERS = 16;

    private static final double DT = 1.0;
    private static final double G = 6.67e-11;
    private static final double RADIUS = 1_000_000.0;
    private static final double MASS = 100.0;

    private Settings settings;

    private Random rng;
    private Timer timer;

    private Body[] bodies;

    /**
     * Parse command line arguments, toggle the settings and start the simulation.
     * 
     * @param args  Command line args (numBodies, numSteps, far, -g, -r).
     */
    public static void main(String[] args) {
        int numBodies = MAX_NUM_BODIES;
        int numSteps = MAX_NUM_STEPS;
        double theta = DEFAULT_THETA;
        int numWorkers = MAX_NUM_WORKERS;
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
            if (args.length >= 4) {
                numWorkers = Integer.parseInt(args[3]);
                if (numWorkers <= 0 || numWorkers > MAX_NUM_WORKERS)
                    numWorkers = MAX_NUM_WORKERS;
            }
            if (args.length >= 5)
                if (args[4].equals("-g")) 
                    guiToggled = true;
            if (args.length >= 6) 
                if (args[5].equals("-r")) 
                    ringToggled = true;

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            System.exit(1);
        }
        new NBodySimulation(new Settings(numBodies, numSteps, theta, numWorkers, guiToggled, ringToggled, DT, G, MASS, RADIUS));
    }

    /**
     * Generate the bodies and start the simulation.
     * 
     * @param settings  A record that holds the settings of the simulation.
     */
    public NBodySimulation(Settings settings) {
        this.settings = settings;
        System.out.println("\n> Simulating the gravitational n-body problem with the following settings:");
        System.out.println(settings);

        rng = new Random();
        rng.setSeed(System.nanoTime());

        if (!settings.ringToggled()) {
            generateBodies();
        } else {
            generateBodiesRing();
        }

        // run simulation.
        timer = new Timer();
        timer.start();
        Worker[] workers = new Worker[settings.numWorkers()]; 
        CyclicBarrier barrier = new CyclicBarrier(settings.numWorkers());
        AtomicInteger counter = new AtomicInteger(0);
        QuadTree quadTree = new QuadTree(null, settings);
        for (int id = 0; id < settings.numWorkers(); id++) {
            workers[id] = new Worker(id, bodies, barrier, counter, quadTree, settings);
            workers[id].start();
        }
        for (int id = 0; id < settings.numWorkers(); id++) {
            try {
                workers[id].join();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                System.exit(1);
            }
        }
        timer.stopAndPrint();
    }

    /**
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

        // Generate the ring of bodies.
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
}