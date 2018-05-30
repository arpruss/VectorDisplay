package mobi.omegacentauri.vectordisplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import static android.util.TypedValue.*;

/**
 * Created by Alexander_Pruss on 10/13/2017.
 */

public class VectorView extends View {
    RecordAndPlay record;
    //boolean redraw = false;
    Bitmap bitmap;
    MyCanvas savedCanvas = null;
    float aspectRatio = 4f/3f;
    MainActivity main;
    static final int MOTION_TIMING = 30;
    static final byte[] UP = new byte[] { 'U', 'P'};
    static final byte[] DOWN = new byte[] { 'D', 'N'};
    static final byte[] MOVE = new byte[] { 'M', 'V'};
    long lastEvent = -MOTION_TIMING;
    byte[] outBuf = new byte[8];
    float[] coords = new float[2];
    TextPaint statusPaint;

    @Override
    protected void onMeasure(int wspec, int hspec) {
        int w = View.MeasureSpec.getSize(wspec);
        int h = View.MeasureSpec.getSize(hspec);
        MainActivity.log( "onmeasure "+w+" "+h+" want "+aspectRatio );
        if (h != 0 && aspectRatio != 0f) {
            if ((float) w / h >= aspectRatio) {
                w = (int) (h * aspectRatio);
            } else {
                h = (int) (w / aspectRatio);
            }
        }
        if (w==0)
            w=1;
        if (h==0)
            h=1;
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Bitmap old = bitmap;
        savedCanvas = new MyCanvas();
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        savedCanvas.setBitmap(bitmap);
        if (old != null) {
            Rect oldR = new Rect(0, 0, old.getWidth(), old.getHeight());
            Rect newR = new Rect(0, 0, w, h);
            savedCanvas.drawBitmap(old, oldR, newR, new Paint());
            old.recycle();
        }
        MainActivity.log( "size "+w+" "+h+" "+(double)w/h);
        //redraw = true;

        statusPaint = new TextPaint();
        statusPaint.setStyle(Paint.Style.FILL);
        statusPaint.setTypeface(Typeface.MONOSPACE);
        statusPaint.setColor(Color.YELLOW);
        statusPaint.setTextAlign(Paint.Align.CENTER);
    }

    public VectorView(Context c, AttributeSet set) {
        super(c,set);

        main = (MainActivity)c;

        //this.redraw = true;
        this.record = main.record;

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (record == null || record.parser == null || record.parser.state == null || savedCanvas == null)
                    return true;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    outBuf[0] = 'U';
                    outBuf[1] = 'P';
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    outBuf[0] = 'D';
                    outBuf[1] = 'N';
                } else if (event.getAction() == MotionEvent.ACTION_MOVE &&
                        System.currentTimeMillis() >= lastEvent + MOTION_TIMING) {
                    outBuf[0] = 'M';
                    outBuf[1] = 'V';
                } else {
                    return true;
                }
                lastEvent = System.currentTimeMillis();
                Matrix inv = new Matrix();
                if (!savedCanvas.matrix.invert(inv))
                    inv.reset();
                coords[0] = event.getX();
                coords[1] = event.getY();
                inv.mapPoints(coords);
                int x = (int)(coords[0]+0.5f);
                int y = (int)(coords[1]+0.5f);

                if (record.parser.buffer.lowEndian) {
                    outBuf[2] = (byte) (x & 0xFF);
                    outBuf[3] = (byte) (x >> 8);
                    outBuf[4] = (byte) (y & 0xFF);
                    outBuf[5] = (byte) (y >> 8);
                }
                else {
                    outBuf[3] = (byte) (x & 0xFF);
                    outBuf[2] = (byte) (x >> 8);
                    outBuf[5] = (byte) (y & 0xFF);
                    outBuf[4] = (byte) (y >> 8);
                }
                outBuf[6] = 0;
                outBuf[7] = 0;
                synchronized(main) {
                    if (main.connectionService != null)
                        main.connectionService.write(outBuf);
                }
                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (savedCanvas == null) {
            MainActivity.log( "creating savedCanvas");
            onSizeChanged(0,0, canvas.getWidth(), canvas.getHeight());
        }

        record.draw(savedCanvas);
        canvas.drawBitmap(bitmap, 0f, 0f, null);

        if (statusPaint != null) {
            String[] status = record.getStatus();
            if (status != null) {
                int w = canvas.getWidth();
                statusPaint.setTextSize(w / 15f);
                float lineHeight = -(statusPaint.ascent() - statusPaint.descent()) * 1.1f;
                float y = bitmap.getHeight() - lineHeight * (status.length - 1) - statusPaint.descent();
                for (String line : status) {
                    statusPaint.setTextSize(w / 15f);
                    float m = statusPaint.measureText(line);
                    if (m > w)
                        statusPaint.setTextSize(w / 15f * w / m);
                    canvas.drawText(line, bitmap.getWidth() / 2, y, statusPaint);
                    y += lineHeight;
                }
            }
        }
    }
}
