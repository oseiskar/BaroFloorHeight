package xyz.osei.baro;

public class BaroGraphFilter {

    private long lastTimesStamp = 0;
    private int nSinceLast = 0;
    private double runningSum = 0;

    private static final long UPDATE_INTERVAL = 1000; // microseconds
    private static final long MIN_SAMPLES = 1;

    public interface Observer {
        void pushValue(float value);
    }

    private Observer observer;

    public BaroGraphFilter(Observer obs) {
        this.observer = obs;
    }

    public void pushValue(long timestamp, float value) {
        nSinceLast++;
        runningSum += value;

        if (timestamp > lastTimesStamp + UPDATE_INTERVAL && nSinceLast >= MIN_SAMPLES) {
            float v = (float)(runningSum / nSinceLast);
            observer.pushValue(v);
            nSinceLast = 0;
            runningSum = 0;
            lastTimesStamp = timestamp;
        }
    }
}
