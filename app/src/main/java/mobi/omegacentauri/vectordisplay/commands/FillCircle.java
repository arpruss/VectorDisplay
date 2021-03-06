package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

public class FillCircle extends Command {
	short x,y,r;
	static Paint p = DefaultPaint();

	private static Paint DefaultPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.FILL);
		p.setStrokeWidth(0);
		return p;
	}

	public FillCircle(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 6;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		x = buffer.getShort(0);
		y = buffer.getShort(2);
		r = buffer.getShort(4);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		p.setColor(state.foreColor);

		c.drawCircle(x, y, r, p);
	}
}
