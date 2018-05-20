package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.DisplayState;

import android.content.Context;
import android.util.Log;

public class Attribute16 extends Command {
	public Attribute16(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 3;
	}

	@Override
	public DisplayState parseArguments(Context context, Buffer buffer) {
		switch((char)buffer.data[0]) {
			case 's':
				state.textSize = buffer.getInteger(1, 2);
				Log.v("VectorDisplay", ""+state.textSize);
				break;
		}
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return false;
	}
}
