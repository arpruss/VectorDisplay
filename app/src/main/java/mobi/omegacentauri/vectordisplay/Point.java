package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.VectorAPI.DisplayState;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class Point extends Command {
	int x1,y1;
	
	public Point(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 4;
	}

	@Override
	public DisplayState parseArguments(Context context, Buffer buffer) {
		x1 = buffer.getInteger(0, 2);
		y1 = buffer.getInteger(2, 2);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		Paint p = new Paint();
		p.setStyle(Style.FILL);
		p.setColor(state.foreColor);
		p.setStrokeWidth(state.getThickness(c));
		Coords xy = state.scale(c, x1, y1, true);
		c.drawCircle(xy.x, xy.y, state.getThickness(c)/2, p);
	}
}
