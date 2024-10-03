import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * This class computes the forces of the bodies and moves the bodies. 
 * Each thread is assigned a subset of the calculations.
 * 
 * @author Leo Vainio
 */

public class Worker extends Thread {
    private int id;
    private Body[] bodies;
    private Vector[][] forces;
    private CyclicBarrier barrier;
    private Settings settings;

    private GUI gui; // For thread 0.

    /**
     * Create a mew worker thread.
     * 
     * @param id  Id of the worker.
     * @param bodies  All the bodies being simulated.
     * @param barrier  Barrier used for synchronizing the workers.
     * @param forces  Contains all the partial forces.
     * @param settings  The settings of the simulation.
     */
    public Worker(int id, Body[] bodies, CyclicBarrier barrier, Vector[][] forces, Settings settings) {
        this.id = id;
        this.bodies = bodies;
        this.barrier = barrier;
        this.forces = forces;
        this.settings = settings;

        if (id == 0 && settings.guiToggled()) {
            gui = new GUI(bodies, settings);
        }
    }

    /**
     * Runs the simulation for set number of steps.
     */
    @Override
    public void run() {
        try {
            for (int i = 0; i < settings.numSteps(); i++) {
                calculateForces();
                barrier.await();
                moveBodies();
                barrier.await();
                if (id == 0 && settings.guiToggled()) {
                    gui.repaint();             
                }
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
     * Calculates total force for every pair of bodies.
     */
    private void calculateForces() {
        double distance;
        double magnitude;
        double dirX;
        double dirY;

        for (int i = id; i < bodies.length - 1; i += settings.numWorkers()) {
            for (int j = i + 1; j < bodies.length; j++) {
                Body b1 = bodies[i];
                Body b2 = bodies[j];

                distance = Math.sqrt(Math.pow(b1.getX()-b2.getX(), 2) + Math.pow(b1.getY()-b2.getY(), 2));
                magnitude = (settings.G() * b1.getMass() * b2.getMass()) / (distance * distance);
                dirX = b2.getX() - b1.getX();
                dirY = b2.getY() - b1.getY();

                forces[id][i].setX(forces[id][i].getX() + magnitude * dirX / distance);
                forces[id][j].setX(forces[id][j].getX() - magnitude * dirX / distance);
                forces[id][i].setY(forces[id][i].getY() + magnitude * dirY / distance);
                forces[id][j].setY(forces[id][j].getY() - magnitude * dirY / distance);
            }
        }
    }  
 
    /*
     * Calculates new velocity and position for each body.
     */
    private void moveBodies() { 
        Vector force = new Vector(0.0, 0.0);
        for (int i = id; i < bodies.length; i += settings.numWorkers()) {
            for (int j = 0; j < settings.numWorkers(); j++) {
                force.setX(force.getX() + forces[j][i].getX());
                force.setY(force.getY() + forces[j][i].getY());
                forces[j][i].setX(0.0);
                forces[j][i].setY(0.0);
            }

            Body b = bodies[i];

            double dVx = force.getX() / b.getMass() * settings.DT();
            double dVy = force.getY() / b.getMass() * settings.DT();
            double dPx = (b.getVx() + dVx / 2.0) * settings.DT();
            double dPy = (b.getVy() + dVy / 2.0) * settings.DT();

            b.setVx(b.getVx() + dVx);
            b.setVy(b.getVy() + dVy);
            b.setX(b.getX() + dPx);
            b.setY(b.getY() + dPy);

            force.setX(0.0);
            force.setY(0.0);
        }   
    }
}