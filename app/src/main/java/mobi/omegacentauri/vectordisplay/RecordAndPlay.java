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
    long updateTimeMillis = 60;
    long lastUpdate = 0;
    boolean posted = false;
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

        if (c.needToClearHistory()) {
            head = tail;
        }

        if (!c.doesDraw())
            return;

        commands[tail] = c;
        tail = (tail + 1) % MAX_ITEMS;
        if (tail == head)
            head = (head + 1) % MAX_ITEMS;

        if (!posted) {
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
        while (head != tail) {
            commands[head].draw(canvas);
            commands[head] = null;
            head = (head + 1) % MAX_ITEMS;
        }
        posted = false;
    }
}
