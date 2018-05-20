package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

import android.app.Activity;

public class BackColor extends Command {
	public BackColor(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 4;
	}
	
	@Override 
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		state.backColor = buffer.getInteger(0,4);
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return false;
	}
}
