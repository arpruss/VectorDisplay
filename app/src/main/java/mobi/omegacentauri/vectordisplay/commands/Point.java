package mobi.omegacentauri.vectordisplay.commands;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class Point extends Command {
	int x1,y1;
	static Paint p = DefaultPaint();

	private static Paint DefaultPaint() {
		Paint p = new Paint();
		p.setStyle(Style.FILL);
		p.setStrokeWidth(0);
		return p;
	}

	public Point(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 4;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		x1 = buffer.getInteger(0, 2);
		y1 = buffer.getInteger(2, 2);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		p.setColor(state.foreColor);
		if (state.rounded)
			c.drawCircle(x1, y1, state.thickness/2, p);
		else
			c.drawRect(x1-state.thickness/2,y1-state.thickness/2,x1+state.thickness/2,y1+state.thickness/2, p);
	}
}
