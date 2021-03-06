package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

public class AddButton extends Command {
	byte cmd;
	String label;

	public AddButton(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 1;
	}

	@Override
	public boolean haveStringArgument() {
		return true;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		cmd = buffer.data[0];
		label = buffer.getString(1, buffer.length-1-1, state);
		return state;
	}

	@Override
	public boolean handleCommand(Handler h) {
		Message msg = h.obtainMessage(MainActivity.ADD_COMMAND);
		Bundle b = new Bundle();
		b.putString(MainActivity.KEY_LABEL, label);
		b.putByte(MainActivity.KEY_COMMAND, cmd);
		msg.setData(b);
		h.sendMessage(msg);
		return true;
	}

	@Override
	public boolean doesDraw() { // does this type actually do anything when show() is called?
		return false;
	}
}
