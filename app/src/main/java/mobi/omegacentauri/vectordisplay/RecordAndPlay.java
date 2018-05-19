package mobi.omegacentauri.vectordisplay;

import android.content.Context;
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
    public VectorAPI parser;
    private int head;
    private int tail;
    private int len;
    private int playOffset;

    public RecordAndPlay(Context c) {
        head = 0;
        len = 0;
        playOffset = 0;
        parser = new VectorAPI(c);
    }

    public void feed(Command c) {
        if (c.needToClearHistory()) {
            head = 0;
            len = 0;
        }
        commands[(head+len) % MAX_ITEMS] = c;
        if (len == MAX_ITEMS)
            head = (head + 1) % MAX_ITEMS;
        else
            len++;
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
