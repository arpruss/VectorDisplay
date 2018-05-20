package mobi.omegacentauri.vectordisplay;

import android.graphics.Canvas;
import android.graphics.Color;

class DisplayState {
    int width;
    int height;
    float pixelAspectRatio;
    int foreColor;
    int backColor;
    float thickness;
    int textSize;
    char hAlignText;
    char vAlignText;
    boolean opaqueTextBackground;
    boolean bold;
    static final char ALIGN_LEFT = 'l';
    static final char ALIGN_RIGHT = 'r';
    static final char ALIGN_CENTER = 'c';
    static final char ALIGN_TOP = 't';
    static final char ALIGN_BOTTOM = 'b';
    static final char ALIGN_BASELINE = 'l';

    public void reset() {
        width = 320;
        height = 240;
        pixelAspectRatio = 1.0f;
        foreColor = Color.WHITE;
        backColor = Color.BLACK;
        thickness = 1f;
        textSize = 8;
        hAlignText = 'l';
        vAlignText = 'b';
        bold = false;
        opaqueTextBackground = true;
    }

    public DisplayState() {
        reset();
    }

    public Coords getScale(Canvas c) {
        int cw = c.getWidth();
        int ch = c.getHeight();
        return new Coords((float)cw/width, (float)ch/height);
    }

    public Coords scale(Canvas c, int x, int y, boolean centerInPixel) {
        Coords s = getScale(c);
        if (centerInPixel)
            return new Coords((x+0.5f) * s.x, (y+0.5f) * s.y);
        else
            return new Coords(x * s.x, y * s.y);
    }

    public Coords unscale(Canvas c, float x, float y) {
        Coords s = getScale(c);
        return new Coords(x/s.x,y/s.y);
    }

    public float getThickness(Canvas c) {
        Coords s = getScale(c);
        return (float) (s.y * thickness);
    }
}
