package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

import static mobi.omegacentauri.vectordisplay.commands.RoundedRectangle.rect;

public class Arc extends Command {
	short x, y, r;
	float startAngle;
	float endAngle;
	RectF oval = new RectF();
	boolean filled;
	static Paint filledPaint = DefaultFilledPaint();
	static Paint strokePaint = DefaultStrokePaint();

	private static Paint DefaultFilledPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.FILL);
        p.setStrokeWidth(0);
		return p;
	}

	private static Paint DefaultStrokePaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.STROKE);
		return p;
	}

	public Arc(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 15;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		x = buffer.getShort(0);
		y = buffer.getShort(2);
		r = buffer.getShort(4);
		startAngle = buffer.getFixed32(6);
		endAngle = buffer.getFixed32(10);
		filled = buffer.data[14] != 0;

		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		oval.left = x-r;
		oval.right = x+r;
		oval.top = y-r;
		oval.bottom = y+r;

		if (filled) {
			filledPaint.setColor(state.foreColor);
			c.drawArc(oval, startAngle, endAngle-startAngle, true, filledPaint);
		}
		else {
			strokePaint.setColor(state.foreColor);
			strokePaint.setStrokeWidth(state.thickness);
			strokePaint.setStrokeCap(state.rounded ? Paint.Cap.ROUND : Paint.Cap.SQUARE);
			strokePaint.setStrokeJoin(state.rounded ? Paint.Join.ROUND : Paint.Join.MITER);
			filledPaint.setColor(state.foreColor);
			c.drawArc(oval, startAngle, endAngle-startAngle, false, strokePaint);
		}
	}
}
