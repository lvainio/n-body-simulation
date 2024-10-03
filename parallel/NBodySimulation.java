import java.util.Random;
import java.util.concurrent.CyclicBarrier;

/**
 * Parallel implementation of brute force n-body simulation.
 * 
 * Compile (Requires JDK version 14 or later):
 *      *.java
 *      
 * Run:
 *      java NBodySimulation
 *      java NBodySimulation <numBodies> <numSteps> <numWorkers>
 *      java NBodySimulation <numBodies> <numSteps> <numWorkers> -g -r
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
    private static final int MAX_NUM_WORKERS = 16;

    private static final double DT = 1.0;
    private static final double G = 6.67e-11;
    private static final double SPACE_RADIUS = 1_000_000;
    private static final double MASS = 100.0;
    
    private Random rng;
    private Settings settings;
    private Timer timer;

    /**
     * Parse command line arguments and configure settings of the simulation.
     * 
     * @param args  <numBodies> <numSteps> <numWorkers> -g -r
     */
    public static void main(String[] args) {
        int numBodies = MAX_NUM_BODIES;
        int numSteps = MAX_NUM_STEPS;
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
                numWorkers = Integer.parseInt(args[2]);
                if (numWorkers <= 0 || numWorkers > MAX_NUM_WORKERS) 
                    numWorkers = MAX_NUM_WORKERS;
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

        new NBodySimulation(new Settings(numBodies, numSteps, numWorkers, guiToggled, ringToggled, DT, G, MASS, SPACE_RADIUS));
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

        Body[] bodies;
        if (settings.ringToggled()) {
            bodies = generateBodiesRing();
        } else {
            bodies = generateBodies();
        }

        // Run simulation.
        timer = new Timer();
        timer.start();
        Worker[] workers = new Worker[settings.numWorkers()]; 
        CyclicBarrier barrier = new CyclicBarrier(settings.numWorkers());
        Vector[][] forces = new Vector[settings.numWorkers()][settings.numBodies()];
        for (int i = 0; i < settings.numWorkers(); i++) {
            for (int j = 0; j < settings.numBodies(); j++) {
                forces[i][j] = new Vector(0.0, 0.0);
            }
        }
        // Create threads.
        for (int id = 0; id < settings.numWorkers(); id++) {
            workers[id] = new Worker(id, bodies, barrier, forces, settings);
            workers[id].start();
        }
        // Join threads.
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

    /*
     * Generate bodies randomly within set boundaries.
     */
    private Body[] generateBodies() {
        Body[] bodies = new Body[settings.numBodies()];
        for (int i = 0; i < bodies.length; i++) {
            double x = rng.nextDouble() * (settings.spaceRadius() * 2);
            double y = rng.nextDouble() * (settings.spaceRadius() * 2);
            double vx = rng.nextDouble() * 25 - 12.5; 
            double vy = rng.nextDouble() * 25 - 12.5; 
            double mass = settings.mass();
            bodies[i] = new Body(x, y, vx, vy, mass);
        }
        return bodies;
    }

    /*
     * Generate bodies in a ring-like formation with a massive attracting body in the middle. 
     */
    private Body[] generateBodiesRing() {
        Body[] bodies = new Body[settings.numBodies()];
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
        return bodies;
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
}
