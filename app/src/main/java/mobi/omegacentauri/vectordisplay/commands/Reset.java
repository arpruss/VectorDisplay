package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.VectorAPI;
import mobi.omegacentauri.vectordisplay.commands.Clear;
import mobi.omegacentauri.vectordisplay.commands.Command;

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
	    buffer.lowEndian = false;
	    return state;
    }

	@Override
	public void draw(Canvas c) {
		state.reset();
		Clear.clearCanvas(c, state);
	}

	static public void doReset(Handler h, DisplayState state) {
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

	@Override
	public void handleCommand(Handler h) {
		doReset(h, state);
	}
}
