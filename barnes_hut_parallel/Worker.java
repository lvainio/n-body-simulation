import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class computes the forces of the bodies and moves the bodies. 
 * Each thread is assigned a subset of the calculations.
 * 
 * @author Leo Vainio
 */

public class Worker extends Thread {    
    private int id;
    private Body[] bodies;
    private CyclicBarrier barrier;   
    private AtomicInteger counter; 
    private GUI gui;
    private QuadTree quadTree; 
    private Settings settings;

    public Worker(int id, Body[] bodies, CyclicBarrier barrier, AtomicInteger counter, QuadTree quadTree, Settings settings) {
        this.id = id;
        this.bodies = bodies;
        this.barrier = barrier;
        this.counter = counter;
        this.quadTree = quadTree;
        this.settings = settings;
        if (id == 0 && settings.guiToggled()) {
            gui = new GUI(bodies, settings);
        } 
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < settings.numSteps(); i++) {
                barrier.await();
                if (id == 0) {
                    if (settings.guiToggled()) {
                        gui.repaint();
                    }
                    Quadrant quadrant = getBoundaries();
                    quadTree.reset(quadrant, bodies);
                }
                barrier.await();
                int quadIdx = counter.getAndIncrement();
                while (quadIdx < 4) {
                    if (quadIdx == 0) {
                        quadTree.getNW().insertBodies(bodies);
                    } else if (quadIdx == 1) {
                        quadTree.getNE().insertBodies(bodies);
                    } else if (quadIdx == 2) {
                        quadTree.getSW().insertBodies(bodies);
                    } else if (quadIdx == 3) {
                        quadTree.getSE().insertBodies(bodies);
                    }
                    quadIdx = counter.getAndIncrement();
                }
                barrier.await();
                if (id == 0) {
                    counter.set(0);
                }
                calculateForces();
                moveBodies();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
            System.exit(1);
        } catch (BrokenBarrierException bbe) {
            bbe.printStackTrace();
            System.exit(1);
        } 
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
     * Build quadtree and compute total force exerted on each body. 
     */
    private void calculateForces() {
        for (int i = id; i < settings.numBodies(); i += settings.numWorkers()) {
            quadTree.calculateForce(bodies[i]);
        }
    }

     /*
     * Move all the bodies depending on the force exerted on each body.
     */
    private void moveBodies() {
        for (int i = id; i < settings.numBodies(); i += settings.numWorkers()) {
            bodies[i].move();
        }
    }
}