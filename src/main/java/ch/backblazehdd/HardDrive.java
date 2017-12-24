package ch.backblazehdd;

/**
 * Implementation for a Hard Drive record, immutable.
 */
public class HardDrive {

    private String min = "";
    private String max = "";

    private String model = "";

    private boolean dead = false;

    public HardDrive() {
    }

    public HardDrive(String min, String max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Creates a new Hard Drive from the string representing a line:
     * 2013-04-11,5XW0MXD7,ST32000542AS,2000398934016,0,,200336848,,,,,,,,0,,,,,,1302,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,23,,,,,,0,,,,,,,,,,,,,,,,,,,,,,,,,,,,
     *
     * @param line The line from a hdd stats file.
     */
    public HardDrive(String line) {
        this.min = line.substring(0, 10);
        this.max = this.min;

        int modelStartIndex = line.indexOf(44, 12) + 1;
        int modelEndIndex = line.indexOf(44, modelStartIndex + 1);

        this.model = line.substring(modelStartIndex, modelEndIndex);

        int deadIndex = line.indexOf(44, modelEndIndex + 1) + 1;

        this.dead = line.charAt(deadIndex) == '1';

    }

    /**
     * When reducing, this method reduces two records of the same hard drive into one.
     * <p>
     * - Takes the minimum of both hard drive lifetimes
     * - Takes the maximum of both hard drive lifetimes
     * - Takes the model description of either (non empty) hard drive
     * - If either of the two records are marked as dead, the merged hard drive is dead as well.
     *
     * @param hd1 The first hard drive record.
     * @param hd2 The second hard drive record.
     * @return The merged hard drive record. It's always a new object.
     */
    public HardDrive(HardDrive hd1, HardDrive hd2) {
        if (hd1.min.compareTo(hd2.min) > 0 || hd1.min.equals("")) {
            this.min = hd2.min;
        } else {
            this.min = hd1.min;
        }

        //Compare Max

        if (hd1.max.compareTo(hd2.max) < 0) {
            this.max = hd2.max;
        } else {
            this.max = hd1.max;
        }

        this.model = hd1.model.equals("") ? hd2.model : hd1.model;
        this.dead = hd1.dead || hd2.dead;
    }


    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    public String getModel() {
        return model;
    }

    public boolean isDead() {
        return dead;
    }

    public static String getID(String line) {
        return line.substring(11, line.indexOf(44, 12));
    }

    @Override
    public String toString() {
        return "HardDrive{" +
                "min='" + min + '\'' +
                ", max='" + max + '\'' +
                ", model='" + model + '\'' +
                ", dead=" + dead +
                '}';
    }
}
