package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

public class FillTriangle extends Command {
	short x1,y1,x2,y2,x3,y3;
	static Paint p = DefaultPaint();
	static Path tri = new Path();

	private static Paint DefaultPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.FILL);
        p.setStrokeWidth(0);
		return p;
	}

	public FillTriangle(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 12;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		x1 = buffer.getShort(0);
		y1 = buffer.getShort(2);
		x2 = buffer.getShort(4);
		y2 = buffer.getShort(6);
		x3 = buffer.getShort(8);
		y3 = buffer.getShort(10);
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		p.setColor(state.foreColor);
		tri.reset();
		tri.moveTo(x1,y1);
		tri.lineTo(x2,y2);
		tri.lineTo(x3,y3);
		tri.lineTo(x1,y1);
		c.drawPath(tri, p);
	}
}
