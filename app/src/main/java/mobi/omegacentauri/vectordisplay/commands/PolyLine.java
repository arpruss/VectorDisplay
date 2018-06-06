package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

public class PolyLine extends Command {
	int n;
	short[] x;
	short[] y;
	int pos;
	static Paint p = DefaultPaint();
	int neededLength = 2;

	private static Paint DefaultPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.STROKE);
		p.setStrokeCap(Paint.Cap.ROUND);
		p.setStrokeJoin(Paint.Join.ROUND);
		return p;
	}

	public PolyLine(DisplayState state) {
		super(state);
	}

	@Override
    public boolean haveFullData(MyBuffer buffer) {
	    if (buffer.length < neededLength)
	        return false;
	    neededLength = 2+(0xFFFF&buffer.getShort(0))*4+1;
        return buffer.length >= neededLength;
    }

    @Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
	    n = buffer.getShort(0);
	    x = new short[n];
        y = new short[n];
	    for (int i=0;i<n;i++) {
	        x[i] = buffer.getShort(2+4*i);
            y[i] = buffer.getShort(2+4*i+2);
        }
		return state;
	}
	
	@Override
	public void draw(Canvas c) {
        if (n<2)
            return;
		p.setColor(state.foreColor);
		p.setStrokeWidth(state.thickness);
		p.setStrokeCap(state.rounded ? Paint.Cap.ROUND : Paint.Cap.SQUARE);
		p.setStrokeJoin(state.rounded ? Paint.Join.ROUND : Paint.Join.MITER);
		for (int i=1;i<n; i++) {
		    c.drawLine(x[i-1], y[i-1], x[i], y[i], p);
        }
	}
}
