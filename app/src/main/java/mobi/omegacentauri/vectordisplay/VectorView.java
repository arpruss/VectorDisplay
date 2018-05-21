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
    byte[] outBuf = new byte[6];

    @Override
    protected void onMeasure(int wspec, int hspec) {
        int w = View.MeasureSpec.getSize(wspec);
        int h = View.MeasureSpec.getSize(hspec);
        MainActivity.log( "onmeasure "+w+" "+h );
        if ((float)w/h >= aspectRatio) {
            w = (int) (h * aspectRatio);
        }
        else {
            h = (int) (w / aspectRatio);
        }
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
                synchronized (record.parser) {
                    synchronized(main) {
                        if (main.usbService != null) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                main.usbService.write(UP);
                            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                main.usbService.write(DOWN);
                            } else if (event.getAction() == MotionEvent.ACTION_MOVE &&
                                    System.currentTimeMillis() >= lastEvent + MOTION_TIMING) {
                                main.usbService.write(MOVE);
                            } else {
                                return true;
                            }
                            lastEvent = System.currentTimeMillis();
                            IntCoords xy = record.parser.state.unscale(savedCanvas, event.getX(), event.getY());
                            outBuf[0] = (byte) (xy.x & 0xFF);
                            outBuf[1] = (byte) (xy.x >> 8);
                            outBuf[2] = (byte) (xy.y & 0xFF);
                            outBuf[3] = (byte) (xy.y >> 8);
                            outBuf[4] = 0;
                            outBuf[5] = 0;
                            main.usbService.write(outBuf);
                        }
                    }
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
