package mobi.omegacentauri.vectordisplay;

import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alexander_Pruss on 10/13/2017.
 */

public class RecordAndPlay {
    private static final int MAX_ITEMS = 500000;
    private Command commands[] = new Command[MAX_ITEMS];
    private VectorAPI parser = new VectorAPI();
    private int head;
    private int tail;
    private int len;
    private int playOffset;

    public RecordAndPlay() {
        head = 0;
        len = 0;
        playOffset = 0;
    }

    public void feed(byte datum) {
        Command c = parser.parse(datum);
        if (c != null) {
            commands[(head+len) % MAX_ITEMS] = c;
            if (len == MAX_ITEMS)
                head = (head + 1) % MAX_ITEMS;
            else
                len++;
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
        if (len == 0) {
            new Clear(parser.state).draw(canvas);
        }
        else {
            new Clear(commands[head].state).draw(canvas);
        }

        playOffset = 0;

        draw(canvas);
    }

    public void draw(Canvas canvas) {
        for (int i=playOffset; i<len; i++) {
            Log.v("VectorView", commands[(head + i) % MAX_ITEMS].getClass().toString());
            commands[(head + i) % MAX_ITEMS].draw(canvas);
        }
        playOffset = len;
    }
}
