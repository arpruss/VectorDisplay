package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Handler;

import mobi.omegacentauri.vectordisplay.commands.Command;
import mobi.omegacentauri.vectordisplay.commands.Update;

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
    boolean continuous = false;
    volatile long waitTime = -1;
    public static final int LAYOUT_DELAY = 250;
    Handler commandHandler;
    Transformation curMatrix = new Transformation();
    boolean connected = false;
    volatile boolean forcedUpdate = false;
    String[] disconnectedStatus = new String[] { "Disconnected" };
    volatile public long updateTimeMillis;
    // TODO: control update speed
    // TODO: delay rendering after view change

    public RecordAndPlay(Activity c, Handler h) {
        head = 0;
        tail = 0;
        parser = new VectorAPI(c);
        context = c;
        commandHandler = h;
    }

    synchronized public void feed(Command c) {
        if (MainActivity.DEBUG)
            MainActivity.log("Feeding "+c.getClass());
        if (c.handleCommand(commandHandler)) {
            waitTime = System.currentTimeMillis() + LAYOUT_DELAY; // TODO: fix this hack
                                                         // so we correctly wait for the screen layout
        }

        continuous = c.state.continuousUpdate;

        if (c.needToClearHistory()) {
            head = tail;
        }

        if (c instanceof Update) {
            forceUpdate();
        }

        if (!c.doesDraw())
            return;

        commands[tail] = c;
        tail = (tail + 1) % MAX_ITEMS;
        if (tail == head)
            head = (head + 1) % MAX_ITEMS;
    }

    synchronized public void setConnected(boolean c) {
        connected = c;
    }

    public void setDisconnectedStatus(String[] lines) {
        disconnectedStatus = lines;
    }

    public void feed(byte[] data) {
        feed(data, data.length);
    }

    synchronized public void feed(byte[] data, int n) {
        for(int i=0; i<n; i++) {
            Command c = parser.parse(data[i]);
            if (c != null)
                feed(c);
        }
    }

    public int findAdjustedTail() {
        if (waitTime >= System.currentTimeMillis() || head == tail)
            return -1;
        if (continuous)
            return tail;
        int t0 = tail - 1;
        if (t0 < 0)
            t0 += MAX_ITEMS;
        while (t0 != head) {
            if (commands[t0] instanceof Update) {
                return (t0 + 1) % MAX_ITEMS;
            }
            t0--;
            if (t0<0)
                t0 = MAX_ITEMS - 1;
        }
        return -1;
    }

    synchronized public void draw(Canvas canvas) {
        MainActivity.log("drawing");
        forcedUpdate = false;

        int endPos = -1;

        endPos = findAdjustedTail();

        if (endPos < 0)
            return;

        while (head != endPos) {
            try {
                Command c = commands[head];
                synchronized (curMatrix) {
                    curMatrix.update(canvas, c.state);
                    canvas.setMatrix(curMatrix);
                }
                if (MainActivity.DEBUG)
                    MainActivity.log("" + c.getClass());
                c.draw(canvas);
            }
            catch (Exception e) {} // don't crash all of the rendering thread in case of bug
            commands[head] = null;
            head = (head + 1) % MAX_ITEMS;
        }
    }

    synchronized public String[] getStatus() {
        if (connected)
            return null;
        else
            return disconnectedStatus;
    }

    public void forceUpdate() {
        forcedUpdate = true;
    }

    synchronized public boolean haveStuffToDraw() {
        return forcedUpdate || 0 <= findAdjustedTail();
    }
}
