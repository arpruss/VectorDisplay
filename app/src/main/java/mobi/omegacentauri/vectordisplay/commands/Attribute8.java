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
				if (buffer.data[1] != 0)
					state.fontInfo |= DisplayState.FONT_BOLD;
				else
					state.fontInfo &= ~DisplayState.FONT_BOLD;
				break;
			case 'I':
				if (buffer.data[1] != 0)
					state.fontInfo |= DisplayState.FONT_ITALIC;
				else
					state.fontInfo &= ~DisplayState.FONT_ITALIC;
				break;
			case 'f':
				state.fontInfo = buffer.data[1];
				break;
			case 'F':
				state.fontInfo = (byte) ((state.fontInfo & DisplayState.FONT_TYPEFACE_MASK) | (buffer.data[1] & DisplayState.FONT_TYPEFACE_MASK));
				break;
			case 'n':
				state.rounded = buffer.data[1] != 0;
				break;
			case 'o':
				state.opaqueTextBackground = buffer.data[1] != 0;
				break;
            case 'r':
                state.rotate = (byte) buffer.data[1];
                break;
			case 'i':
				state.cp437 = buffer.data[1] != 0;
				break;
			case 'c':
				state.continuousUpdate = buffer.data[1] != 0;
//                Log.v("VectorDisplay", "cupdate "+state.continuousUpdate);
				break;
			case 'w':
				state.wrap = buffer.data[1] != 0;
		}
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return false;
	}
}
