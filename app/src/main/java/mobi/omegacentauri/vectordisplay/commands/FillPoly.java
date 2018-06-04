package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

public class FillPoly extends Command {
	int n;
	short[] x;
	short[] y;
	int pos;
	static Paint p = DefaultPaint();
	static Path poly = new Path();

	private static Paint DefaultPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.FILL);
		return p;
	}

	public FillPoly(DisplayState state) {
		super(state);
	}

	@Override
    public boolean haveFullData(Buffer buffer) {
	    if (buffer.length() < 2)
	        return false;
        int n = buffer.getInteger(0,2);
        return buffer.length() >= 2+n*4+1;
    }

    @Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
	    n = buffer.getInteger(0, 2);
	    x = new short[n];
        y = new short[n];
	    for (int i=0;i<n;i++) {
	        x[i] = (short) buffer.getInteger(2+4*i,2);
            y[i] = (short) buffer.getInteger(2+4*i+2,2);
        }
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
        if (n<3)
            return;
		p.setStrokeCap(state.rounded ? Paint.Cap.ROUND : Paint.Cap.SQUARE);
		p.setStrokeJoin(state.rounded ? Paint.Join.ROUND : Paint.Join.MITER);
		p.setColor(state.foreColor);
		poly.reset();
		poly.moveTo(x[0],y[0]);

		for (int i=1;i<n; i++) {
		    poly.lineTo(x[i], y[i]);
        }

        if (x[n-1] != x[0] || y[n-1] != y[0])
        	poly.lineTo(x[0],y[0]);

		c.drawPath(poly, p);
	}
}
