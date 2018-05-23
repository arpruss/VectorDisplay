package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;

public class Command {
	DisplayState state;

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
		if (buffer.checksum())
			return parseArguments(context, buffer);
		else {
			Log.e( "VectorDisplay","bad checksum");
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

	public boolean needToResetView() { return false; }

	public void handleCommand(Handler h) {}
}
