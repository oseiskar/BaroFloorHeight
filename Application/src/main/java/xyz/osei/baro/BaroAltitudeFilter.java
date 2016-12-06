package xyz.osei.baro;

public class BaroAltitudeFilter {

    public interface Observer {
        void observePressureQuotient(double pOverP0, double err);
    }

    private static class EmaFilter {

        private double v = 0.0;
        private double total = 0.0;
        private double maxDiff = 0.0;

        long lastTimestamp = 0;
        long firstTimestamp = 0;

        private final double alpha;

        EmaFilter(double a) {
            alpha = a;
        }

        void pushValue(long timestamp, double x) {
            v = alpha * x + (1-alpha) * v;
            total = alpha * 1.0 + (1-alpha) * total;
            lastTimestamp = timestamp;
            if (firstTimestamp == 0) {
                firstTimestamp = timestamp;
            }
            maxDiff = Math.max(maxDiff, Math.abs(getFiltered() - x));
        }

        double getFiltered() {
            return v / total;
        }

        double getMaxDiff() {
            return maxDiff;
        }
    }

    private static final double EMA_ALPHA = 1e-3;
    private static final long POST_TRANSITION_INTERVAL = 3000_000_000L; // microseconds

    private boolean transitionActive = false;

    private EmaFilter beforeTransition = null;
    private EmaFilter filter = new EmaFilter(EMA_ALPHA);

    private Observer observer;

    BaroAltitudeFilter(BaroAltitudeFilter.Observer observer) {
        this.observer = observer;
    }

    public void pushValue(long timestamp, float value) {
        if (filter != null) {
            filter.pushValue(timestamp, value);

            if (beforeTransition != null &&
                    !transitionActive &&
                    timestamp > filter.firstTimestamp + POST_TRANSITION_INTERVAL) {

                final double p0 = beforeTransition.getFiltered();
                final double p1 = filter.getFiltered();
                final double maxDiff = Math.max(beforeTransition.getMaxDiff(), filter.getMaxDiff());

                System.out.println("Pressure before transition "+p0);
                System.out.println("After transition "+p1);
                System.out.println("maxDiff "+maxDiff);

                final double q = p1/p0;
                final double qMax = (p1+maxDiff)/(p0-maxDiff);
                final double qMin = (p1-maxDiff)/(p0+maxDiff);
                final double qErr = Math.max(qMax - q, q - qMin);

                observer.observePressureQuotient(q, qErr);
                beforeTransition = null;
            }
        }
    }

    public void beginTransition() {
        transitionActive = true;
        beforeTransition = filter;
        filter = null;
    }

    public void endTransition() {
        transitionActive = false;
        filter = new EmaFilter(EMA_ALPHA);
    }

    public boolean isTransitionActive() {
        return transitionActive;
    }
}
