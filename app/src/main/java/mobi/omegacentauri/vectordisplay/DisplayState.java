package mobi.omegacentauri.vectordisplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

public class DisplayState implements Cloneable {
    public int width;
    public int height;
    public float pixelAspectRatio;
    public int foreColor;
    public int backColor;
    public float thickness;
    public float textSize;
    public char hAlignText;
    public char vAlignText;
    public boolean opaqueTextBackground;
    public boolean bold;
    public boolean continuousUpdate;
    public static final char ALIGN_LEFT = 'l';
    public static final char ALIGN_RIGHT = 'r';
    public static final char ALIGN_CENTER = 'c';
    public static final char ALIGN_TOP = 't';
    public static final char ALIGN_BOTTOM = 'b';
    static final char ALIGN_BASELINE = 'l';
    public int textBackColor;
    public char rotate;
    public float monoFontScaleX;
    public float monoFontScale;

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
        vAlignText = 't';
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
