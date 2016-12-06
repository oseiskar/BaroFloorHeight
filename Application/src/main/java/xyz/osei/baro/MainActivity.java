
package xyz.osei.baro;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener, PressureToHeight.Observer {

    private SensorManager sensorManager;
    private BaroAltitudeFilter baroAltitudeFilter;
    private BaroGraphFilter graphFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        graphFilter = new BaroGraphFilter((GraphView)findViewById(R.id.graphView));
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        baroAltitudeFilter = new BaroAltitudeFilter(new PressureToHeight(this));

        final Button button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (baroAltitudeFilter.isTransitionActive()) {
                    // end transition
                    button.setText(R.string.startTransitionText);
                    baroAltitudeFilter.endTransition();
                } else {
                    // begin transition
                    button.setText(R.string.endTransitionText);
                    baroAltitudeFilter.beginTransition();
                }
            }
        });
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

    @Override
    public void observeHeight(double height, double err) {

        String text = String.format("%.2f ± %.2f m", height, err);
        ((TextView)findViewById(R.id.floorHeightText)).setText(text);

    }
}