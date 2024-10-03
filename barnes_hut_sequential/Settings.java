/**
 * A record that holds all the settings of the simulation.
 * 
 * @author Leo Vainio
 */

public record Settings (int numBodies,
                        int numSteps,
                        double theta,
                        boolean guiToggled,
                        boolean ringToggled,
                        double DT,
                        double G,
                        double mass,
                        double spaceRadius) {

    /**
     * Returns a string representation of the data in the record.
     */
    public String toString() {
        return "\t- numBodies=" + numBodies + ",\n" +
            "\t- numSteps=" + numSteps + ",\n" +
            "\t- theta= " + theta + ",\n" +
            "\t- guiToggled=" + guiToggled + ",\n" +
            "\t- ringToggled=" + ringToggled + ",\n" +
            "\t- DT=" + DT + ",\n" +
            "\t- G=" + G + ",\n" +
            "\t- mass=" + mass + ",\n" +
            "\t- spaceRadius=" + spaceRadius + ",\n";
    }
}
