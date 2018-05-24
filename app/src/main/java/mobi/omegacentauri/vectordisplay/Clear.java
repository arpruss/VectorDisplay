package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.DisplayState;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import static java.lang.Math.max;

public class Clear extends Command {
	public Clear(DisplayState state) {
		super(state);
	}

	@Override
	public boolean needToClearHistory() { return true; }

	@Override
	public int fixedArgumentsLength() { return 0; }

	public static void clearCanvas(Canvas c, DisplayState state) {
		Paint paint = new Paint();
		paint.setColor(state.backColor);
		MainActivity.log( "clearing to "+state.backColor+ " "+c.getWidth()+" "+c.getHeight());
		c.drawRect(0,0,max(state.width,state.height),max(state.width,state.height), paint);
	}
	@Override
	public void draw(Canvas c) {
		clearCanvas(c, state);
	}
}
