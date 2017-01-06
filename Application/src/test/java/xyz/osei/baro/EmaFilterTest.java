package xyz.osei.baro;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EmaFilterTest {

    EmaFilter filter;

    @Before
    public void setUp() {
        filter = new EmaFilter(10.);
    }

    @Test
    public void testOneSample() {
        filter.update(7., 0.1);
        assertEquals(7., filter.get(), 1e-10);
    }

    @Test
    public void testTwoSamplesShort() {
        filter.update(7., 1e-5);
        filter.update(11., 1e-5);
        assertEquals(9., filter.get(), 1e-5);
    }

    @Test
    public void testTwoSamplesLong() {
        filter.update(7., 1e5);
        filter.update(11., 1e5);
        assertEquals(11., filter.get(), 1e-5);
    }

    @Test
    public void testManySamples() {
        for (int i=0; i<100; ++i) filter.update(7.0, 0.1);
        assertEquals(7., filter.get(), 1e-10);
        for (int i=0; i<1500; ++i) filter.update(7.0, 0.1);
        assertEquals(7., filter.get(), 1e-10);

        for (int i=0; i<100; ++i) filter.update(11.0, 0.1);
        assertEquals(9., filter.get(), 1e-4);

        for (int i=0; i<5000; ++i) filter.update(11.0, 0.1);
        assertEquals(11., filter.get(), 1e-4);
    }
}
