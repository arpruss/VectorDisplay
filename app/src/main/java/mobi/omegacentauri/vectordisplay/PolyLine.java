package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

public class PolyLine extends Command {
	Path path;
	int n;
	short[] x;
	short[] y;
	int pos;
	static Paint p = DefaultPaint();

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
        if (n<2)
            return;
		p.setColor(state.foreColor);
		p.setStrokeWidth(state.thickness);
		for (int i=1;i<n; i++) {
		    c.drawLine(x[i-1], y[i-1], x[i], y[i], p);
        }
	}
}
