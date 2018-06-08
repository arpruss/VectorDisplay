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
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import static android.util.TypedValue.*;

/**
 * Created by Alexander_Pruss on 10/13/2017.
 */

public class VectorView extends SurfaceView implements SurfaceHolder.Callback {
    RecordAndPlay record;
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
    MySurfaceThread thread;
    private Canvas savedCanvas;
    Paint paint = new Paint();
    volatile long waitForSurfaceChanged = -1;

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

/*    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        MainActivity.log("onLayout1");
    } */

    public VectorView(Context c, AttributeSet set) {
        super(c,set);

        Log.v("VectorDisplay", "creating vector view");

        getHolder().addCallback(this);
        setZOrderOnTop(true);

        main = (MainActivity)c;

        statusPaint = new TextPaint();
        statusPaint.setStyle(Paint.Style.FILL);
        statusPaint.setTypeface(Typeface.MONOSPACE);
        statusPaint.setColor(Color.YELLOW);
        statusPaint.setTextAlign(Paint.Align.CENTER);

        //this.redraw = true;
        this.record = main.record;

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (record == null || record.parser == null || record.parser.state == null)
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
                synchronized(record.curMatrix) {
                    if (!record.curMatrix.invert(inv))
                        inv.reset();
                }
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

    public void updateStatus(Canvas canvas) {
        String[] status = record.getStatus();
        if (status != null) {
            int w = canvas.getWidth();
            statusPaint.setTextSize(w / 15f);
            float lineHeight = -(statusPaint.getFontMetrics().top - statusPaint.getFontMetrics().bottom) * 1.1f;
            float y = canvas.getHeight() - lineHeight * (status.length - 1) - statusPaint.descent();
            for (String line : status) {
                statusPaint.setTextSize(w / 15f);
                float m = statusPaint.measureText(line);
                if (m > w)
                    statusPaint.setTextSize(w / 15f * w / m);
                canvas.drawText(line, canvas.getWidth() / 2, y, statusPaint);
                y += lineHeight;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.v("VectorDisplay", "surfaceCreated");
        thread = new MySurfaceThread(holder);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.v("VectorDisplay", "surfaceChanged");
        record.forceUpdate();
        waitForSurfaceChanged = -1;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (thread != null) {
            thread.close();
            thread = null;
        }
    }

    public class MySurfaceThread extends Thread {
        private SurfaceHolder holder;
        volatile private boolean active;
        private Bitmap bitmap;

        public MySurfaceThread(SurfaceHolder h) {
            holder = h;
            active = true;
        }

        synchronized public void close() {
            active = false;
        }

        private void prepareBitmap(Canvas c) {
            int w = c.getWidth();
            int h = c.getHeight();
            if (bitmap == null || bitmap.getWidth() != w || bitmap.getHeight() != h) {
                Bitmap old = bitmap;
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                savedCanvas = new Canvas();
                savedCanvas.setBitmap(bitmap);
                if (old != null) {
                    Rect newR = new Rect(0, 0, w, h);
                    savedCanvas.drawBitmap(old, null, newR, null);
                    old.recycle();
                }
            }
        }

        private void deleteBitmap() {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }

        @Override
        public void run() {
            while(active) {
                boolean drew = false;
                long t0 = System.currentTimeMillis();
                synchronized (record) {
                    if (record.haveStuffToDraw() && active) { // check we're still active
                        try {
                            Canvas c;
                            if (Build.VERSION.SDK_INT >= 23)
                                c = holder.getSurface().lockHardwareCanvas();
                            else
                                c = holder.lockCanvas();
                            prepareBitmap(c);
                            record.draw(savedCanvas);
                            c.drawBitmap(bitmap, 0f, 0f, paint);
                            updateStatus(c);
                            holder.unlockCanvasAndPost(c);
                            drew = true;
                        } catch (Exception e) {
                            // in case surface gets invalidated mid-way
                        }
                    }
                }
                long t = System.currentTimeMillis()-t0;
                long pauseTime = drew ? record.updateTimeMillis : 2;
                if (0 <= t && t < pauseTime) {
                    try {
                        sleep(pauseTime-t);
                    } catch (InterruptedException e) {
                    }
                }
            }
            deleteBitmap();
        }
    }
}
