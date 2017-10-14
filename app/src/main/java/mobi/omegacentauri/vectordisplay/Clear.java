package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.DisplayState;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class Clear extends Command {
	public Clear(DisplayState state) {
		super(state);
	}

	@Override
	public void draw(Canvas c) {
		Paint paint = new Paint();
		paint.setColor(state.backColor);
		Log.v("VectorDisplay", "clearing to "+state.backColor+ " "+c.getWidth()+" "+c.getHeight());
		c.drawRect(0,0,c.getWidth(),c.getHeight(), paint);
	}
}
