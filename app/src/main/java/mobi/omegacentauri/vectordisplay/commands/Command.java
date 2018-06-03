package mobi.omegacentauri.vectordisplay.commands;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Command {
	public DisplayState state;

	public boolean errorState;

	public Command(DisplayState state) {
		this.errorState = false;
        try {
            this.state = (DisplayState)state.clone();
        } catch (CloneNotSupportedException e) {
        }
    }

	public boolean haveStringArgument() { return false; }

	public int fixedArgumentsLength() { return 0; }

	public DisplayState parseArguments(Activity context, Buffer buffer) {
		return state;
	}

	public DisplayState parse(Activity context, Buffer buffer) {
		if (!haveFullData(buffer))
			return null;
		if (buffer.checksum()) {
			return parseArguments(context, buffer);
		}
		else {
			Log.e( "VectorDisplay","bad checksum "+this.getClass());
			errorState = true;
			return null;
		}
	}

	public boolean haveFullData(Buffer buffer) {
		if (! haveStringArgument()) {
			if (buffer.length() < fixedArgumentsLength()+1)
				return false;
		}
		else {
			if (buffer.length() <= fixedArgumentsLength()+1 ||
					buffer.getByte(buffer.length()-2) != 0)
				return false;
		}
		return true;
	}

	public void draw(Canvas c) {
	}

	public boolean doesDraw() { // does this type actually do anything when show() is called?
		return true;
	}

	public boolean needToClearHistory() { return false; }

	// returns true if we need to wait for onLayout() before proceeding with rendering
	public boolean handleCommand(Handler h) { return false; }

	static void sendAck(Handler h, byte command) {
		Message msg = h.obtainMessage(MainActivity.ACK);
		msg.arg1 = command;
		h.sendMessage(msg);
	}
}
