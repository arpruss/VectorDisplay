package mobi.omegacentauri.vectordisplay.commands;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Line extends Command {
	short x1,y1,x2,y2;
	static Paint p = DefaultPaint();

	private static Paint DefaultPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.STROKE);
		return p;
	}

	@Override
	public int fixedArgumentsLength() {
		return 8;
	}

	public Line(DisplayState state) {
		super(state);
	}
	
	@Override 
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		x1 = (short)buffer.getInteger(0, 2);
		y1 = (short)buffer.getInteger(2, 2);
		x2 = (short)buffer.getInteger(4, 2);
		y2 = (short)buffer.getInteger(6, 2);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		p.setColor(state.foreColor);
		p.setStrokeWidth(state.thickness);
		p.setStrokeCap(state.rounded ? Paint.Cap.ROUND : Paint.Cap.SQUARE);
		p.setStrokeJoin(state.rounded ? Paint.Join.ROUND : Paint.Join.MITER);
		c.drawLine(x1, y1, x2, y2, p);
	}
}
