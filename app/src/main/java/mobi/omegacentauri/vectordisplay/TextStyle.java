package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.VectorAPI.DisplayState;

import android.content.Context;

public class TextStyle extends Command {
	int x1,y1;
	
	public TextStyle(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 3;
	}

	@Override
	public DisplayState parseArguments(Context context, Buffer buffer) {
		switch((char)buffer.data[0]) {
			case 'a':
				state.hAlign = (char)buffer.data[1];
				break;
			case 's':
				state.textSize = buffer.getInteger(1, 2);
				break;
		}
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return false;
	}
}
