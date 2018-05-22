package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

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
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		cmd = buffer.data[0];
		label = buffer.getString(1, buffer.length()-1-1);
		return state;
	}

	@Override
	public void handleCommand(Handler h) {
		Message msg = h.obtainMessage(MainActivity.ADD_COMMAND);
		Bundle b = new Bundle();
		b.putString(MainActivity.KEY_LABEL, label);
		b.putByte(MainActivity.KEY_COMMAND, cmd);
		msg.setData(b);
		h.sendMessage(msg);
	}

	@Override
	public boolean doesDraw() { // does this type actually do anything when show() is called?
		return false;
	}
}
