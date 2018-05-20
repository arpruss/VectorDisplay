package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

import android.app.Activity;
import android.graphics.Canvas;

public class ForeColor extends Command {
	public ForeColor(DisplayState state) {
		super(state);
	}

    @Override
    public int fixedArgumentsLength() {
        return 4;
    }

    @Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		state.foreColor = buffer.getInteger(0,4);
		return state;
	}

	@Override
	public void draw(Canvas c) {
	}
	
	@Override
	public boolean doesDraw() {
		return false;
	}
}
