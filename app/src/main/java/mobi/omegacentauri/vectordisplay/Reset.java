package mobi.omegacentauri.vectordisplay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import mobi.omegacentauri.vectordisplay.VectorAPI.DisplayState;

public class Reset extends Command {
	public Reset(DisplayState state) {
		super(state);
	}

	@Override
	public boolean needToClearHistory() { return true; }

	@Override
	public int fixedArgumentsLength() { return 0; }

	@Override
	public void draw(Canvas c) {
		state.reset();
		Paint paint = new Paint();
		paint.setColor(state.backColor);
		Log.v("VectorDisplay", "clearing to "+state.backColor+ " "+c.getWidth()+" "+c.getHeight());
		c.drawRect(0,0,c.getWidth(),c.getHeight(), paint);
	}
}
