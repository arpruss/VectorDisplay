package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

public class Text extends Command {
	int x1,y1;
	String text;
	
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
		x1 = buffer.getInteger(0, 2);
		y1 = buffer.getInteger(2, 2);
		text = buffer.getString(4, buffer.length()-1-4);
        MainActivity.log("parsing text: "+text);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
        MainActivity.log( "drawing text");
		Paint p = new Paint();
		p.setStyle(Style.FILL);
		p.setColor(state.foreColor);

		float size = state.scale(c, 0, state.textSize, false).y;
		p.setTextSize(size);
		p.setFakeBoldText(state.bold);
		Coords xy = state.scale(c, x1, y1, false);
		xy.y = 	state.vAlignText == DisplayState.ALIGN_TOP ? xy.y + p.ascent() :
                state.vAlignText == DisplayState.ALIGN_BOTTOM ? xy.y + p.descent() :
                state.vAlignText == DisplayState.ALIGN_CENTER ? xy.y + p.ascent() * 0.5f :
                                xy.y;

        float w = p.measureText(text);
		xy.x = state.hAlignText == DisplayState.ALIGN_LEFT ? xy.x :
                state.hAlignText == DisplayState.ALIGN_RIGHT ? xy.x - w :
                xy.x - w * 0.5f;
		if (state.opaqueTextBackground) {
            float h = p.descent() - p.ascent();
            Paint fill = new Paint();
            fill.setColor(state.textBackColor);
            fill.setStyle(Paint.Style.FILL);
            fill.setStrokeWidth(0);

            c.drawRect(xy.x, xy.y + p.ascent(), xy.x + w + 0.5f, xy.y + p.ascent() + h, fill);
        }
		c.drawText(text, xy.x, xy.y, p);
	}
}
