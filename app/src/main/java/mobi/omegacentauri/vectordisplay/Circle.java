package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

public class Circle extends Command {
	short x,y,r;

	public Circle(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 6;
	}

	@Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		x = (short)buffer.getInteger(0, 2);
		y = (short)buffer.getInteger(2, 2);
		r = (short)buffer.getInteger(4, 2);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		Paint p = new Paint();
		p.setColor(state.foreColor);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(state.getThickness(c));

        Coords start = state.scale(c, x, y, false);

		c.drawCircle(start.x, start.y, state.scaleY(c,r), p);
	}
}
