package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Rectangle extends Command {
	int x1,y1,x2,y2;
	
	public Rectangle(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 8;
	}

	@Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		x1 = buffer.getInteger(0, 2);
		y1 = buffer.getInteger(2, 2);
		x2 = buffer.getInteger(4, 2);
		y2 = buffer.getInteger(6, 2);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		Paint p = new Paint();
		p.setColor(state.foreColor);
		p.setStyle(Paint.Style.FILL);
		p.setStrokeWidth(state.getThickness(c));
		Coords start = state.scale(c, x1, y1, false);
		Coords end = state.scale(c, x2, y2, false);
		c.drawRect(start.x, start.y, end.x, end.y, p);
	}
}
