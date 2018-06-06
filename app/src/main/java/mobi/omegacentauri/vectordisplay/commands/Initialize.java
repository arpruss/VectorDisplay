package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Handler;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.RecordAndPlay;
import mobi.omegacentauri.vectordisplay.VectorAPI;

public class Initialize extends Command {
	boolean valid = false;

	public Initialize(DisplayState state) {
		super(state);
	}

	@Override
	public boolean needToClearHistory() { return true; }

	@Override
	public int fixedArgumentsLength() { return 4; } // endianness selector: send 0x1234 ; wait for rest

    @Override
    public DisplayState parseArguments(Activity context, VectorAPI.MyBuffer buffer) {
		int e = 0xFFFF&buffer.getShort(0);

		if (e == 0x1234) {
			valid = true;
		}
		else if (e == 0x3412) {
			buffer.lowEndian = ! buffer.lowEndian;
			valid = true;
		}

		if (valid)
	    	state.reset();

	    return state;
    }

	@Override
	public void draw(Canvas c) {
		if (valid) {
			state.reset();
			Clear.clearCanvas(c, state);
		}
	}

	@Override
	public boolean handleCommand(Handler h) {
		if (valid) {
			state.reset();
			Reset.doReset(h, state);
			sendAck(h, VectorAPI.INITIALIZE_COMMAND, RecordAndPlay.LAYOUT_DELAY);
		}
		return valid;
	}
}
