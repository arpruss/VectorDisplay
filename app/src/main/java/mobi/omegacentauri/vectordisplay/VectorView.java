package mobi.omegacentauri.vectordisplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Alexander_Pruss on 10/13/2017.
 */

public class VectorView extends View {
    RecordAndPlay record;
    boolean redraw = false;
    Bitmap bitmap;
    Canvas savedCanvas = null;
    float aspectRatio = 4f/3f;
    MainActivity main;
    static final int MOTION_TIMING = 30;
    static final byte[] UP = new byte[] { 'U', 'P'};
    static final byte[] DOWN = new byte[] { 'D', 'N'};
    static final byte[] MOVE = new byte[] { 'M', 'V'};
    long lastEvent = -MOTION_TIMING;
    byte[] outBuf = new byte[8];

    @Override
    protected void onMeasure(int wspec, int hspec) {
        int w = View.MeasureSpec.getSize(wspec);
        int h = View.MeasureSpec.getSize(hspec);
        Log.v( "VectorDisplay", "onmeasure "+w+" "+h+" want "+aspectRatio );
        if ((float)w/h >= aspectRatio) {
            w = (int) (h * aspectRatio);
        }
        else {
            h = (int) (w / aspectRatio);
        }
        Log.v( "VectorDisplay", "set "+w+" "+h);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (bitmap != null) {
            bitmap.recycle();
        }
        savedCanvas = new Canvas();
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        savedCanvas.setBitmap(bitmap);
        MainActivity.log( "size "+w+" "+h+" "+(double)w/h);
        redraw = true;
    }

    public VectorView(Context c, AttributeSet set) {
        super(c,set);

        main = (MainActivity)c;

        this.redraw = true;
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
                IntCoords xy = record.parser.state.unscale(savedCanvas, event.getX(), event.getY());
                outBuf[2] = (byte) (xy.x & 0xFF);
                outBuf[3] = (byte) (xy.x >> 8);
                outBuf[4] = (byte) (xy.y & 0xFF);
                outBuf[5] = (byte) (xy.y >> 8);
                outBuf[6] = 0;
                outBuf[7] = 0;
                synchronized(main) {
                    if (main.usbService != null)
                        main.usbService.write(outBuf);
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
        if (redraw) {
            MainActivity.log( "redrawing");
            record.redraw(savedCanvas);
            redraw = false;
        }
        else {
            MainActivity.log( "drawing");
            record.draw(savedCanvas);
        }
        MainActivity.log( "bitmap "+bitmap.getWidth()+" "+bitmap.getHeight());
        canvas.drawBitmap(bitmap, 0f, 0f, null);
    }
}
