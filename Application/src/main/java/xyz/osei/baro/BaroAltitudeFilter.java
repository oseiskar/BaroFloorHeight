package xyz.osei.baro;

public class BaroAltitudeFilter {

    public interface Observer {
        void observeCalibrated();
    }

    private static double CALIBRATION_TIME = 3_000_000_000L; // nanoseconds

    private static int EXP_WINDOW_LENGTH_SAMPLES = 100;
    private static final double ALPHA = 1.0 - Math.pow(2, -1.0 / EXP_WINDOW_LENGTH_SAMPLES);

    private double v = 0.0;
    private double total = 0.0;
    private double maxDiff = 0.0;

    private long firstTimestamp = 0;
    private int nSamples = 0;

    public double getFiltered() {
        return v / total;
    }

    public double getMaxDiff() {
        return maxDiff;
    }

    void pushValue(long timestamp, double x) {
        v = ALPHA * x + (1-ALPHA) * v;
        total = ALPHA * 1.0 + (1-ALPHA) * total;
        if (firstTimestamp == 0) {
            firstTimestamp = timestamp;
        }
        maxDiff = Math.max(maxDiff, Math.abs(getFiltered() - x));
        nSamples++;

        if (!calibrated && timestamp > firstTimestamp + CALIBRATION_TIME) {
            calibrated = true;
            System.out.println("calibrated after "+nSamples+" sample(s)");
            observer.observeCalibrated();
        }
    }

    private Observer observer;
    boolean calibrated = false;

    BaroAltitudeFilter(BaroAltitudeFilter.Observer observer) {
        this.observer = observer;
    }
}
