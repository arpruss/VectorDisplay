package mobi.omegacentauri.vectordisplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

class DisplayState implements Cloneable {
    int width;
    int height;
    float pixelAspectRatio;
    int foreColor;
    int backColor;
    float thickness;
    float textSize;
    char hAlignText;
    char vAlignText;
    boolean opaqueTextBackground;
    boolean bold;
    boolean continuousUpdate;
    static final char ALIGN_LEFT = 'l';
    static final char ALIGN_RIGHT = 'r';
    static final char ALIGN_CENTER = 'c';
    static final char ALIGN_TOP = 't';
    static final char ALIGN_BOTTOM = 'b';
    static final char ALIGN_BASELINE = 'l';
    public int textBackColor;
    public char rotate;
    float monoFontScaleX;
    float monoFontScale;

    public Object clone() throws
            CloneNotSupportedException
    {
        return super.clone();
    }

    public void reset() {
        width = 240;
        height = 320;
        pixelAspectRatio = 1.0f;
        foreColor = Color.WHITE;
        backColor = Color.BLACK;
        textBackColor = Color.BLACK;
        thickness = 1f;
        textSize = 8;
        hAlignText = 'l';
        vAlignText = 'b';
        bold = false;
        rotate = 0;
        opaqueTextBackground = true;
        continuousUpdate = true;
        measureMonoFont();
    }

    private void measureMonoFont() {
        TextPaint p = new TextPaint();
        p.setTypeface(Typeface.MONOSPACE);
        p.setTextSize(8f);
        float h = p.descent() - p.ascent();
        monoFontScale = 8f / h;
        p.setTextSize(monoFontScale * 8f);
        float w = p.measureText("0123456789")/10f;
        monoFontScaleX = 5f / w;
    }

    public DisplayState() {
        reset();
    }

    public Coords getScale(Canvas c) {
        int cw = c.getWidth();
        int ch = c.getHeight();
        return new Coords((float)cw/width, (float)ch/height);
    }

    public IntCoords unscale(Canvas c, float x, float y) {
        Coords s = getScale(c);
        int x1 = (int) (x/s.x+0.5f);
        int y1 = (int) (y/s.y+0.5f);
        return new IntCoords(x1,y1);
    }

    public float getAspectRatio() {
        return width*pixelAspectRatio/height;
    }
}
