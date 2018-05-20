package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.DisplayState;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

public class Line extends Command {
	int x1,y1,x2,y2;

	@Override
	public int fixedArgumentsLength() {
		return 8;
	}

	public Line(DisplayState state) {
		super(state);
	}
	
	@Override 
	public DisplayState parseArguments(Context context, Buffer buffer) {
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
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeWidth(state.getThickness(c));
		Coords start = state.scale(c, x1, y1, true);
		Coords end = state.scale(c, x2, y2, true);
		Log.v("VectorDisplay", "line "+start.x+" "+start.y+" "+end.x+" "+end.y+" "+p.getStrokeWidth());
		c.drawLine(start.x, start.y, end.x, end.y, p);
	}
}
