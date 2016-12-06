
package xyz.osei.baro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayDeque;
import java.util.Deque;

public class GraphView extends View implements BaroGraphFilter.Observer {
    private float width;
    private float height;
    private Paint paint;

    private Deque<Float> values;
    private float min, max;

    private static final int GRAPH_BUFFER_SIZE = 200;

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        values = new ArrayDeque<>();
    }

    public void pushValue(float value) {
        if (values.isEmpty()) {
            min = value;
            max = value;
        } else {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        values.addLast(value);
        if (values.size() > GRAPH_BUFFER_SIZE) {
            values.removeFirst();
        }

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int i = 0;
        float x0 = 0;
        float y0 = 0;

        if (values.size() < 2) return;

        float yRange = max - min;
        if (yRange == 0) yRange = 1f;

        for (float v : values) {
            float x1 = i / (float)(values.size()-1) * width;
            float y1 = (max - v) / yRange * height;
            if (i > 0) {
                canvas.drawLine(x0, y0, x1, y1, paint);
            }
            x0 = x1;
            y0 = y1;
            i++;
        }
    }
}
