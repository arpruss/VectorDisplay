package mobi.omegacentauri.vectordisplay.commands;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.commands.Command;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Line extends Command {
	short x1,y1,x2,y2;
	static Paint p = DefaultPaint();

	private static Paint DefaultPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeCap(Paint.Cap.ROUND);
		p.setStrokeJoin(Paint.Join.ROUND);
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
	public DisplayState parseArguments(Activity context, Buffer buffer) {
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
		MainActivity.log( "foreColor "+state.foreColor);
		c.drawLine(x1, y1, x2, y2, p);
	}
}