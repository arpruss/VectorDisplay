package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Alexander_Pruss on 10/13/2017.
 */

public class RecordAndPlay {
    private static final int MAX_ITEMS = 2000;
    private Command commands[] = new Command[MAX_ITEMS];
    public VectorAPI parser;
    private int head;
    private int tail;
    Activity context;
    Resetter resetter;
    static final long updateTimeMillis = 100;
    long lastUpdate = 0;
    boolean posted = false;

    public RecordAndPlay(Activity c, Resetter r) {
        head = 0;
        tail = 0;
        parser = new VectorAPI(c);
        context = c;
        resetter = r;
    }

    synchronized public void feed(Command c) {
        if (c.needToClearHistory()) {
            head = tail;
        }

        if (c.needToResetView()) {
            resetter.resetVectorView(c.state);
        }

        commands[tail] = c;
        tail = (tail + 1) % MAX_ITEMS;
        if (tail == head)
            head = (head + 1) % MAX_ITEMS;

        if (!posted) {
            try {
                context.findViewById(R.id.vector).postInvalidateDelayed(updateTimeMillis);
                posted = true;
            } catch (Exception e) {
            }
        }
    }

    public void feed(byte datum) {
        Command c = parser.parse(datum);
        if (c != null) {
            feed(c);
        }
//        else {
//            Log.v("VectorView", "unknown "+datum);
//        }
    }

    public void feed(byte[] data) {
        for(byte datum: data)
            feed(datum);
    }

    synchronized public void redraw(Canvas canvas) {
        if (head == tail) {
            Clear.clearCanvas(canvas, parser.state);
        }
        else {
            Clear.clearCanvas(canvas, commands[head].state);
        }

        draw(canvas);
    }

    synchronized public void draw(Canvas canvas) {
        while (head != tail) {
            commands[head].draw(canvas);
            commands[head] = null;
            head = (head + 1) % MAX_ITEMS;
        }
        posted = false;
    }

    public interface Resetter {
        void resetVectorView(DisplayState state);
    }
}
