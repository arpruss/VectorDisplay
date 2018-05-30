package mobi.omegacentauri.vectordisplay.commands;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.commands.Command;

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

	private static TextPaint DefaultPaint() {
		TextPaint p = new TextPaint();
		p.setStyle(Style.FILL);
		p.setTypeface(Typeface.MONOSPACE);
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
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		x = (short)buffer.getInteger(0, 2);
		y = (short)buffer.getInteger(2, 2);
		text = buffer.getString(4, buffer.length()-1-4, state);
        MainActivity.log("parsing text: "+text);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
        MainActivity.log( "drawing text");
		p.setColor(state.foreColor);

		p.setTextSize(state.textSize*state.monoFontScale);
		p.setTextScaleX(state.monoFontScaleX);
		p.setFakeBoldText(state.bold);
		float y1 = 	state.vAlignText == DisplayState.ALIGN_TOP ? y - p.ascent() :
                state.vAlignText == DisplayState.ALIGN_BOTTOM ? y + p.descent() :
                state.vAlignText == DisplayState.ALIGN_CENTER ? y + p.ascent() * 0.5f :
                                y;

        float w = p.measureText(text);
		float x1 = state.hAlignText == DisplayState.ALIGN_LEFT ? x :
                state.hAlignText == DisplayState.ALIGN_RIGHT ? x - w :
                x - w * 0.5f;
		if (state.opaqueTextBackground) {
            float h = p.descent() - p.ascent();
            Paint fill = new Paint();
            fill.setColor(state.textBackColor);
            fill.setStyle(Paint.Style.FILL);
            fill.setStrokeWidth(0);

            c.drawRect(x1, y1 + p.ascent(), x1 + w + 0.5f, y1 + p.ascent() + h, fill);
        }
		c.drawText(text, x1, y1, p);
	}
}
