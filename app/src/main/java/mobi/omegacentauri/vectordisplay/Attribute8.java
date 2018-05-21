package mobi.omegacentauri.vectordisplay;

import android.app.Activity;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

public class Attribute8 extends Command {
	public Attribute8(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 3;
	}

	@Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		switch((char)buffer.data[0]) {
			case 'h':
				state.hAlignText = (char)buffer.data[1];
				break;
			case 'v':
				state.vAlignText = (char)buffer.data[1];
				break;
			case 'b':
				state.bold = buffer.data[1] != 0;
				break;
			case 'o':
				state.opaqueTextBackground = buffer.data[1] != 0;
				break;
            case 'r':
                state.rotate = (char) buffer.data[1];
                break;
		}
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return false;
	}
}
