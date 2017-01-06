package xyz.osei.baro;

public class EmaFilter {

    private final double beta;

    // state
    private double total = 0;
    private double s;

    EmaFilter(double halfLifeSeconds) {
        this.beta = Math.log(2) / halfLifeSeconds;
    }

    void update(double x, double dt) {
        final double alpha = 1.0 - Math.exp(-this.beta * dt);
        s = (1.0-alpha)*s + alpha*x;
        total = (1.0-alpha)*total + alpha;
    }

    double get() {
        if (total == 0)
            return s;
        else
            return s / total;
    }

}
