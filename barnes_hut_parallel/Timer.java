/**
 * High resolution timer used to benchmark the simulation.
 * 
 * @author Leo Vainio
 */

public class Timer {
    private long startTime;
    private long endTime;
    
    /*
     * Creates a timer and sets start time to now.
     */
    public Timer() {
        startTime = System.nanoTime();
    }

    /*
     * Saves start time.
     */
    public void start() {
        startTime = System.nanoTime();
    }

    /*
     * Saves end time and print the elapsed time.
     */
    public void stopAndPrint() {
        stop();
        print();
    }

    /*
     * Saves end time.
     */
    public void stop() {
        endTime = System.nanoTime();
    }

    // TODO: remove
    public double getElapsed() {
        return (endTime-startTime) / 1e9;
    }

    /*
     * Print the elapsed time.
     */
    public void print() {
        double elapsedSeconds = (endTime-startTime) / 1e9;
        System.out.println("> Execution time: " + elapsedSeconds);
    }
}
