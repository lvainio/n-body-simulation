/**
 * High resolution timer used to benchmark the simulation.
 * 
 * @author Leo Vainio
 */

 public class Timer {
    private long startTime;
    private long endTime;
    
    /**
     * Creates a timer and sets the starting time to now.
     */
    public Timer() {
        startTime = System.nanoTime();
        endTime = System.nanoTime();
    }

    /**
     * Saves the current time as starting time.
     */
    public void start() {
        startTime = System.nanoTime();
    }

    /**
     * Saves ending time and prints the elapsed time.
     */
    public void stopAndPrint() {
        stop();
        print();
    }

    /**
     * Saves the current time as ending time.
     */
    public void stop() {
        endTime = System.nanoTime();
    }

    /**
     * Print the elapsed time.
     */
    public void print() {
        double elapsedSeconds = (endTime-startTime) / 1e9;
        System.out.println("> Execution time: " + elapsedSeconds);
    }
}