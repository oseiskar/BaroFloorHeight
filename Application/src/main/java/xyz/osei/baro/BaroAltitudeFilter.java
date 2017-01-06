package xyz.osei.baro;

public class BaroAltitudeFilter {

    public interface Observer {
        void observeCalibrated();
    }

    private static double CALIBRATION_TIME_SECONDS = 3.;
    private static double EMA_HALF_LIFE_SECONDS = 1.0;

    private double maxDiff = 0.0;

    private long firstTimestamp = -1;
    private double prevT = 0;
    private int nSamples = 0;

    private EmaFilter filter = new EmaFilter(EMA_HALF_LIFE_SECONDS);

    public double getFiltered() {
        return filter.get();
    }

    public double getMaxDiff() {
        return maxDiff;
    }

    void pushValue(long timestamp, double x) {

        if (firstTimestamp < 0) {
            firstTimestamp = timestamp;
            return; // ignore first sample
        }
        double t = (timestamp - firstTimestamp) * 1e-9;
        double dt = t - prevT;
        prevT = t;

        filter.update(x, dt);

        maxDiff = Math.max(maxDiff, Math.abs(getFiltered() - x));
        nSamples++;

        if (!calibrated && t > CALIBRATION_TIME_SECONDS) {
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
