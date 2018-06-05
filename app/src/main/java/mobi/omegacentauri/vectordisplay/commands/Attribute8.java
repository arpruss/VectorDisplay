package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

public class Attribute8 extends Command {
	public Attribute8(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 2;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
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
			case 'n':
				state.rounded = buffer.data[1] != 0;
				break;
			case 'o':
				state.opaqueTextBackground = buffer.data[1] != 0;
				break;
            case 'r':
                state.rotate = (char) buffer.data[1];
                break;
			case 'i':
				state.cp437 = buffer.data[1] != 0;
				break;
			case 'c':
				state.continuousUpdate = buffer.data[1] != 0;
//                Log.v("VectorDisplay", "cupdate "+state.continuousUpdate);
				break;
		}
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return false;
	}
}
