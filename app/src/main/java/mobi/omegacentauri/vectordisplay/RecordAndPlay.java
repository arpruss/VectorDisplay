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
    private static final int MAX_ITEMS = 5000;
    private Command commands[] = new Command[MAX_ITEMS];
    public VectorAPI parser;
    private int head;
    private int tail;
    Activity context;
    long updateTimeMillis = 60;
    long lastUpdate = 0;
    boolean posted = false;
    boolean continuous = false;
    Handler commandHandler;

    public RecordAndPlay(Activity c, Handler h) {
        head = 0;
        tail = 0;
        parser = new VectorAPI(c);
        context = c;
        commandHandler = h;
    }

    synchronized public void feed(Command c) {
        c.handleCommand(commandHandler);
        continuous = c.state.continuousUpdate;

        if (c.needToClearHistory()) {
            head = tail;
        }

        if (c instanceof Update) {
            commandHandler.sendMessage(commandHandler.obtainMessage(MainActivity.INVALIDATE_VIEW));
            posted = true;
        }

        if (!c.doesDraw())
            return;

        commands[tail] = c;
        tail = (tail + 1) % MAX_ITEMS;
        if (tail == head)
            head = (head + 1) % MAX_ITEMS;

        if (!posted && continuous) {
            commandHandler.sendMessageDelayed(commandHandler.obtainMessage(MainActivity.INVALIDATE_VIEW), updateTimeMillis);
            posted = true;
        }
    }

    public void feed(byte[] data) {
        feed(data, data.length);
    }

    public void feed(byte[] data, int n) {
        for(int i=0; i<n; i++) {
            Command c = parser.parse(data[i]);
            if (c != null)
                feed(c);
        }
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
        int endPos = -1;

        if (! continuous) {
            if (head == tail) {
                posted = false;
                return;
            }
            int t0 = (tail - 1) % MAX_ITEMS;
            while (t0 != head) {
                if (commands[t0] instanceof Update) {
                    endPos = (t0 + 1) % MAX_ITEMS;
                    break;
                }
                t0--;
                if (t0<0)
                    t0 = MAX_ITEMS - 1;
            }
            if (endPos < 0) {
                posted = false;
                return;
            }
        }
        while (head != tail && head != endPos) {
            commands[head].draw(canvas);
            commands[head] = null;
            head = (head + 1) % MAX_ITEMS;
        }
        posted = false;
    }
}
