
package xyz.osei.baro;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private BaroAltitudeFilter baroAltitudeFilter;
    private BaroGraphFilter graphFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        graphFilter = new BaroGraphFilter((GraphView)findViewById(R.id.graphView));
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        baroAltitudeFilter = new BaroAltitudeFilter();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            baroAltitudeFilter.pushValue(event.timestamp, event.values[0]);
            graphFilter.pushValue(event.timestamp, event.values[0]);
        }
    }
}