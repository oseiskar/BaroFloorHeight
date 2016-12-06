
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

public class MainActivity extends Activity implements SensorEventListener, BaroAltitudeFilter.Observer {

    private SensorManager sensorManager;
    private BaroAltitudeFilter activeFilter, previousFilter;
    private BaroGraphFilter graphFilter;
    private Button button;

    enum State {
        UNCALIBRATED(false, R.string.calibratingText),
        BEFORE_TRANSITION(true, R.string.startTransitionText),
        TRANSITION(true, R.string.endTransitionText),
        AFTER_TRANSITION_UNCALIBRATED(false, R.string.collectingMoreDataText),
        DONE(true, R.string.startNextTransitionText);

        boolean canPushButton;
        int buttonTextId;

        State(boolean button, int textId) {
            canPushButton = button;
            buttonTextId = textId;
        }

        State getNext() {
            if (this == DONE) return TRANSITION;
            else {
                return values()[this.ordinal()+1];
            }
        }
    }

    State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);

        graphFilter = new BaroGraphFilter((GraphView)findViewById(R.id.graphView));
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        activeFilter = new BaroAltitudeFilter(this);

        state = State.UNCALIBRATED;
        button = (Button)findViewById(R.id.button);
        button.setEnabled(state.canPushButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state.canPushButton) nextState();
            }
        });
    }

    private void nextState() {

        System.out.println("State transition "+state+" -> "+state.getNext());

        state = state.getNext();
        button.setText(state.buttonTextId);
        button.setEnabled(state.canPushButton);

        switch (state) {
            case TRANSITION:
                previousFilter = activeFilter;
                activeFilter = null;
                break;
            case AFTER_TRANSITION_UNCALIBRATED:
                activeFilter = new BaroAltitudeFilter(this);
                break;
            case DONE:
                computeAndShowHeight();
                break;
        }
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
            if (activeFilter != null) activeFilter.pushValue(event.timestamp, event.values[0]);
            graphFilter.pushValue(event.timestamp, event.values[0]);
        }
    }

    private void computeAndShowHeight() {
        double p0 = previousFilter.getFiltered();
        final double p1 = activeFilter.getFiltered();
        final double maxDiff = Math.max(previousFilter.getMaxDiff(), activeFilter.getMaxDiff());

        final double q = p1/p0;
        final double qMax = (p1+maxDiff)/(p0-maxDiff);
        final double qMin = (p1-maxDiff)/(p0+maxDiff);
        final double qErr = Math.max(qMax - q, q - qMin);

        final double height = PressureToHeight.pressureToHeight(q);
        final double err = PressureToHeight.pressureToHeightError(q, qErr);

        String text = String.format("%.2f ± %.2f m", height, err);
        ((TextView)findViewById(R.id.floorHeightText)).setText(text);
    }

    @Override
    public void observeCalibrated() {
        nextState();
    }
}