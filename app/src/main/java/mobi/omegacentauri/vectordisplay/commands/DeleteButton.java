package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.commands.Command;

public class DeleteButton extends Command {
	byte cmd;

	public DeleteButton(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 1;
	}

	@Override
	public boolean haveStringArgument() {
		return false;
	}

	@Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		cmd = buffer.data[0];
		return state;
	}

	@Override
	public boolean doesDraw() { // does this type actually do anything when show() is called?
		return false;
	}

	@Override
	public boolean handleCommand(Handler h) {
		Message msg = h.obtainMessage(MainActivity.DELETE_COMMAND);
		Bundle b = new Bundle();
		b.putByte(MainActivity.KEY_COMMAND, cmd);
		msg.setData(b);
		h.sendMessage(msg);
		return true;
	}
}
