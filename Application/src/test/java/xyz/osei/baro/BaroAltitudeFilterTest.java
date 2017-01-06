package xyz.osei.baro;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BaroAltitudeFilterTest {

    BaroAltitudeFilter filter;

    @Before
    public void setUp() {
        filter = new BaroAltitudeFilter(null);
    }

    @Test
    public void testSingleSample() {
        filter.pushValue(0, 7.0);
        assertEquals(0.0, filter.getFiltered(), 1e-10);
    }

    @Test
    public void testTwoSamples() {
        filter.pushValue(0, 7.0);
        filter.pushValue(1, 7.0);
        assertEquals(7.0, filter.getFiltered(), 1e-10);
    }


    @Test
    public void testManySamples() {

        long million = 1000_000;

        for (int i=0; i<2000; ++i) filter.pushValue(i*million, 0);
        assertEquals(0.0, filter.getFiltered(), 1e-10);

        for (int i=2000; i<2500; ++i) filter.pushValue(i*million, 7.0);
        assertEquals(2.5, filter.getFiltered(), 1e-2);
    }

    @Test
    public void testCalibration() {
        final List<Boolean> calibrated = new ArrayList<>();

        filter = new BaroAltitudeFilter(new BaroAltitudeFilter.Observer() {
            @Override
            public void observeCalibrated() {
                calibrated.add(true);
            }
        });

        long million = 1000_000;
        for (int i=0; i<2000; ++i) filter.pushValue(i*million, 0);
        assertTrue(calibrated.isEmpty());

        for (int i=2000; i<3500; ++i) filter.pushValue(i*million, 7.0);
        assertFalse(calibrated.isEmpty());
    }
}
