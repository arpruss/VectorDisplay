package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

public class RoundedRectangle extends Command {
	short x1,y1,x2,y2;
	static Paint filledPaint = DefaultFilledPaint();
	static Paint strokePaint = DefaultStrokePaint();
	static RectF rect = new RectF();
	private short r;
	private boolean filled;

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

	public RoundedRectangle(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 11;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		x1 = buffer.getShort(0);
		y1 = buffer.getShort(2);
		x2 = buffer.getShort(4);
		y2 = buffer.getShort(6);
		r = buffer.getShort(8);
		filled = buffer.data[10] != 0;

		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		float x1f,y1f,x2f,y2f;
		if (filled) {
			filledPaint.setColor(state.foreColor);
			rect.left = x1-0.5f;
			rect.right = x2+0.5f;
			rect.top = y1-0.5f;
			rect.bottom = y2+0.5f;
			c.drawRoundRect(rect, r, r, filledPaint);
		}
		else {
			strokePaint.setColor(state.foreColor);
			strokePaint.setStrokeWidth(state.thickness);
			strokePaint.setStrokeCap(state.rounded ? Paint.Cap.ROUND : Paint.Cap.SQUARE);
			strokePaint.setStrokeJoin(state.rounded ? Paint.Join.ROUND : Paint.Join.MITER);
			rect.left = x1;
			rect.right = x2;
			rect.top = y1;
			rect.bottom = y2;
			c.drawRoundRect(rect, r, r, strokePaint);
		}
	}
}
