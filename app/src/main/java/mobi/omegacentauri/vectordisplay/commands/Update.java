package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.commands.Command;

public class Update extends Command {
	byte cmd;
	String label;

	public Update(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 0;
	}

	@Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		return state;
	}

	@Override
	public void handleCommand(Handler h) {
		Message msg = h.obtainMessage(MainActivity.INVALIDATE_VIEW);
		h.sendMessage(msg);
	}

	@Override
	public boolean doesDraw() { // does this type actually do anything when show() is called?
		return true;
	}
}