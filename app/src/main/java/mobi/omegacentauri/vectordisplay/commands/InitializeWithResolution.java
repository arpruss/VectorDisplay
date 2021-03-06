package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.RecordAndPlay;
import mobi.omegacentauri.vectordisplay.VectorAPI;

public class InitializeWithResolution extends Command {
	boolean valid = false;

	public InitializeWithResolution(DisplayState state) {
		super(state);
	}

	@Override
	public boolean needToClearHistory() { return true; }

	@Override
	public int fixedArgumentsLength() { return 16; } // endianness selector 0x1234, w [2 bytes], h [2 bytes], pixelAspectRatio [4 bytes], future-proofing [6 bytes]

    @Override
    public DisplayState parseArguments(Activity context, VectorAPI.MyBuffer buffer) {
		int e = 0xFFFF & buffer.getShort(0);

		if (e == 0x1234) {
			valid = true;
		}
		else if (e == 0x3412) {
			buffer.lowEndian = ! buffer.lowEndian;
			valid = true;
		}

		if (valid) {
			state.reset();
		}

		state.width = buffer.getShort(2);
		state.height = buffer.getShort(4);
		state.pixelAspectRatio = buffer.getFixed32(6);

	    return state;
    }

	@Override
	public void draw(Canvas c) {
		if (valid) {
			Clear.clearCanvas(c, state);
			Log.v("VectorDisplay", "+Clearing canvas");
		}
	}

	@Override
	public boolean handleCommand(Handler h) {
		if (valid) {
			Reset.doReset(h, state);
			sendAck(h, VectorAPI.INITIALIZE_WITH_RESOLUTION_COMMAND, RecordAndPlay.LAYOUT_DELAY);
		}
		return true;
	}
}
