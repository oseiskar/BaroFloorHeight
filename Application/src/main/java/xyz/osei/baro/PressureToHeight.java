package xyz.osei.baro;

public class PressureToHeight implements BaroAltitudeFilter.Observer {

    private final static double T0 = 273.15; // zero Celsius to Kelvin
    private final static double L = -6.5e-3; // K/m, atmospheric lapse rate

    // see https://en.wikipedia.org/wiki/Vertical_pressure_variation
    // R = 287.053; // J/(kg K), specific gas constant
    // g = 9.80665; // m/s^2, standard gravity
    private final static double pressureExp = - L * 287.053 / 9.80665; // = -L*R/g = −0.190263189

    @Override
    public void observePressureQuotient(double pOverP0, double err) {

        System.out.println("observed p/p0 "+pOverP0+" +- "+err);

        final double TCelsius = 20; // assumed air temperature, Celsius
        final double TDiff = 30; // about temperature difference in transition

        double height = pressureToHeight(pOverP0, TCelsius);
        double minHeight = pressureToHeight(pOverP0+err, TCelsius+TDiff/2);
        double maxHeight = pressureToHeight(pOverP0-err, TCelsius-TDiff/2);

        double heightErr = Math.max(maxHeight-height, height-minHeight);

        observer.observeHeight(height, heightErr);
    }

    static double pressureToHeight(double pOverP0, double TCelsius) {
        return (TCelsius+T0)/L * (Math.pow(pOverP0, pressureExp) - 1);
    }

    interface Observer {
        void observeHeight(double height, double err);
    }

    final Observer observer;

    PressureToHeight(Observer obs) {
        observer = obs;
    }
}
