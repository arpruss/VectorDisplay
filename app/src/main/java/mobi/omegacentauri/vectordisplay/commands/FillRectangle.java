package mobi.omegacentauri.vectordisplay.commands;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.commands.Command;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;

public class FillRectangle extends Command {
	short x1,y1,x2,y2;
	static Paint p = DefaultPaint();

	private static Paint DefaultPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.FILL);
        p.setStrokeWidth(0);
		return p;
	}

	public FillRectangle(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 8;
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
/*		if (x1==0)
			x1 = -1;
		if (y1==0)
			y1 = -1;
		short w = (short) state.rotatedWidth();
		if (x2==w-1)
			x2 = w;
		short h = (short) state.rotatedHeight();
		if (y2==h-1)
			y2 = h; */
		c.drawRect(x1-0.5f, y1-0.5f, x2+0.5f, y2+0.5f, p);
	}
}
