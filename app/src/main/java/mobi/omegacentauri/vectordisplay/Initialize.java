package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
    public DisplayState parseArguments(Activity context, VectorAPI.Buffer buffer) {
		int e = buffer.getInteger(0, 2);

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
	public void handleCommand(Handler h) {
		if (valid)
			Reset.doReset(h, state);
	}
}
