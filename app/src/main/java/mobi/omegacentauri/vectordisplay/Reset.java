package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import mobi.omegacentauri.vectordisplay.DisplayState;

public class Reset extends Command {
	public Reset(DisplayState state) {
		super(state);
	}

	@Override
	public boolean needToClearHistory() { return true; }

	@Override
	public int fixedArgumentsLength() { return 0; }

    @Override
    public DisplayState parseArguments(Activity context, VectorAPI.Buffer buffer) {
	    state.reset();
	    return state;
    }

	@Override
	public void draw(Canvas c) {
		state.reset();
		Clear.clearCanvas(c, state);
	}

	@Override
	public void handleCommand(Handler h) {
		Message msg = h.obtainMessage(MainActivity.DELETE_ALL_COMMANDS);
		h.sendMessage(msg);
        msg = h.obtainMessage(MainActivity.ACK);
        h.sendMessage(msg);
        msg = h.obtainMessage(MainActivity.RESET_VIEW);
        Bundle b = new Bundle();
        b.putFloat(MainActivity.KEY_ASPECT, state.getAspectRatio());
        msg.setData(b);
        h.sendMessage(msg);
	}
}
