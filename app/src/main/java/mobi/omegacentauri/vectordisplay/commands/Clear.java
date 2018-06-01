package mobi.omegacentauri.vectordisplay.commands;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.commands.Command;

import android.graphics.Canvas;
import android.graphics.Paint;

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
		c.drawRect(-0.5f,-0.5f,max(state.width,state.height),max(state.width,state.height), paint);
	}
	@Override
	public void draw(Canvas c) {
		clearCanvas(c, state);
	}
}
