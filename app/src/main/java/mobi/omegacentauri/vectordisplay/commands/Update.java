package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

public class Update extends Command {
	byte cmd;
	String label;

	public Update(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 0;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		return state;
	}

	@Override
	public boolean doesDraw() { // does this type actually do anything when show() is called?
		return true;
	}
}
