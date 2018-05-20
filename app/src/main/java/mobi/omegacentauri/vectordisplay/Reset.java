package mobi.omegacentauri.vectordisplay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import mobi.omegacentauri.vectordisplay.DisplayState;

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
		Clear.clearCanvas(c, state);
	}
}
