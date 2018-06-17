package mobi.omegacentauri.vectordisplay.commands;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.text.TextPaint;

public class Text extends Command {
	short x,y;
	String text;
	static TextPaint p = DefaultPaint();
	static Paint fillPaint = DefaultFillPaint();

	private static TextPaint DefaultPaint() {
		TextPaint p = new TextPaint();
		p.setStyle(Style.FILL);
		p.setTypeface(Typeface.MONOSPACE);
		return p;
	}

	private static Paint DefaultFillPaint() {
		Paint p = new Paint();
		p.setStyle(Paint.Style.FILL);
		p.setStrokeWidth(0);
		return p;
	}

	public Text(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 4;
	}

	@Override
	public boolean haveStringArgument() {
		return true;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		x = buffer.getShort(0);
		y = buffer.getShort(2);
		text = buffer.getString(4, buffer.length-1-4, state);
        MainActivity.log("parsing text: "+text);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		p.setColor(state.textForeColor);

		p.setTextSize(state.textSize*state.monoFontScale);
		p.setTextScaleX(state.monoFontScaleX);
		p.setFakeBoldText((state.fontInfo & DisplayState.FONT_BOLD) != 0);
		Paint.FontMetrics fm = p.getFontMetrics();
		float y1 = 	state.vAlignText == DisplayState.ALIGN_TOP ? y - fm.top :
                state.vAlignText == DisplayState.ALIGN_BOTTOM ? y - fm.bottom :
                state.vAlignText == DisplayState.ALIGN_CENTER ? y - (fm.bottom + fm.top) * 0.5f :
                                y;

        float w = p.measureText(text);
		float x1 = state.hAlignText == DisplayState.ALIGN_LEFT ? x :
                state.hAlignText == DisplayState.ALIGN_RIGHT ? x - w :
                x - w * 0.5f;
		if (state.opaqueTextBackground) {
            float h = fm.bottom - fm.top;
            fillPaint.setColor(state.textBackColor);

            c.drawRect(x1, y1 + fm.top, x1 + w + 0.5f, y1 + fm.top + h, fillPaint);
        }
		c.drawText(text, x1, y1, p);
	}
}
