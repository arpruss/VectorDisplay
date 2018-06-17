package mobi.omegacentauri.vectordisplay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;

public class DisplayState implements Cloneable {
    public float cursorX;
    public float cursorY;
    public boolean wrap;
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
    public boolean continuousUpdate;
    public static final char ALIGN_LEFT = 'l';
    public static final char ALIGN_RIGHT = 'r';
    public static final char ALIGN_CENTER = 'c';
    public static final char ALIGN_TOP = 't';
    public static final char ALIGN_BOTTOM = 'b';
    public static final char ALIGN_BASELINE = 'l';
    public int textBackColor;
    public int textForeColor;
    public byte rotate;
    public float monoFontScaleX;
    public float monoFontScale;
    public boolean cp437;
    public boolean rounded;
    public byte fontInfo;
    public static final byte FONT_TYPEFACE_MASK = 0x7;
    public static final byte FONT_SERIF = 0;
    public static final byte FONT_SANS = 1;
    public static final byte FONT_MONO = 2;
    public static final byte FONT_ITALIC = 8;
    public static final byte FONT_BOLD = 16;

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
        textForeColor = Color.WHITE;
        thickness = 1f;
        textSize = 8;
        hAlignText = 'l';
        vAlignText = 't';
        fontInfo = FONT_MONO;
        rotate = 0;
        cp437 = false;
        opaqueTextBackground = true;
        continuousUpdate = true;
        rounded = true;
        measureMonoFont();
        wrap = true;
        cursorX = 0f;
        cursorY = 0f;
    }

    private void measureMonoFont() {
        TextPaint p = new TextPaint();
        p.setTypeface(Typeface.MONOSPACE);
        p.setTextSize(8f);
        float h = p.getFontMetrics().bottom - p.getFontMetrics().top;
        monoFontScale = 8f / h;
        p.setTextSize(monoFontScale * 8f);
        float w = p.measureText("0123456789")/10f;
        monoFontScaleX = 5f / w;
    }

    public DisplayState() {
        reset();
    }

    public float getAspectRatio() {
        return width*pixelAspectRatio/height;
    }

    public int rotatedWidth() {
        return rotate % 2 == 0 ? width : height;
    }

    public int rotatedHeight() {
        return rotate % 2 == 0 ? height : width;
    }
}
