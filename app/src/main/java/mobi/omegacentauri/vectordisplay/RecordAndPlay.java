package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.util.Log;

/**
 * Created by Alexander_Pruss on 10/13/2017.
 */

public class RecordAndPlay {
    private static final int MAX_ITEMS = 50000;
    private Command commands[] = new Command[MAX_ITEMS];
    public VectorAPI parser;
    private int head;
    private int tail;
    Activity context;
    Resetter resetter;

    public RecordAndPlay(Activity c, Resetter r) {
        head = 0;
        tail = 0;
        parser = new VectorAPI(c);
        context = c;
        resetter = r;
    }

    public void feed(Command c) {
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

        try {
            context.findViewById(R.id.vector).invalidate();
        }
        catch(Exception e) {}
    }

    public void feed(byte datum) {
        Command c = parser.parse(datum);
        if (c != null) {
            feed(c);
        }
        else {
            Log.v("VectorView", "unknown "+datum);
        }
    }

    public void feed(byte[] data) {
        for(byte datum: data)
            feed(datum);
    }

    public void redraw(Canvas canvas) {
        if (head == tail) {
            Clear.clearCanvas(canvas, parser.state);
        }
        else {
            Clear.clearCanvas(canvas, commands[head].state);
        }

        draw(canvas);
    }

    public void draw(Canvas canvas) {
        while (head != tail) {
            commands[head].draw(canvas);
            head = (head + 1) % MAX_ITEMS;
        }
    }

    public interface Resetter {
        void resetVectorView(DisplayState state);
    }
}
