package xyz.osei.baro;

public class PressureToHeight {

    private final static double T0 = 273.15; // zero Celsius to Kelvin
    private final static double L = -6.5e-3; // K/m, atmospheric lapse rate

    // see https://en.wikipedia.org/wiki/Vertical_pressure_variation
    // R = 287.053; // J/(kg K), specific gas constant
    // g = 9.80665; // m/s^2, standard gravity
    private final static double pressureExp = - L * 287.053 / 9.80665; // = -L*R/g = −0.190263189

    private final static double TCelsiusDefault = 20; // assumed air temperature, Celsius

    static double pressureToHeight(double pOverP0, double TCelsius) {
        return (TCelsius+T0)/L * (Math.pow(pOverP0, pressureExp) - 1);
    }

    static double pressureToHeight(double pOverP0) {
        return pressureToHeight(pOverP0, TCelsiusDefault);
    }

    static double pressureToHeightError(double pOverP0, double err) {

        final double TDiff = 30; // about temperature difference in transition
        final double TCelsius = TCelsiusDefault;

        double height = pressureToHeight(pOverP0, TCelsius);
        double minHeight = pressureToHeight(pOverP0+err, TCelsius+TDiff/2);
        double maxHeight = pressureToHeight(pOverP0-err, TCelsius-TDiff/2);
        return Math.max(maxHeight-height, height-minHeight);
    }
}
