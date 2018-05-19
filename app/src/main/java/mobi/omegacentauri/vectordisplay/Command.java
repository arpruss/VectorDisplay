package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

import android.content.Context;
import android.graphics.Canvas;

public class Command {
	VectorAPI.DisplayState state;

	public boolean errorState;

	public Command(VectorAPI.DisplayState state) {
		this.errorState = false;
		this.state = state;
	}

	public boolean haveStringArgument() { return false; }

	public int fixedArgumentsLength() { return 0; }

	public VectorAPI.DisplayState parseArguments(Context context, Buffer buffer) {
		return state;
	}

	final public VectorAPI.DisplayState parse(Context context, Buffer buffer) {
		if (! haveStringArgument()) {
			if (buffer.length() < fixedArgumentsLength()+1)
				return null;
		}
		else {
			if (buffer.length() <= fixedArgumentsLength()+1 ||
					buffer.getByte(buffer.length()-2) != 0)
				return null;
		}
		if (buffer.checksum())
			return parseArguments(context, buffer);
		else {
			errorState = true;
			return null;
		}
	}

	public void draw(Canvas c) {
	}

	public boolean doesDraw() { // does this type actually do anything when show() is called?
		return true;
	}

	public boolean needToClearHistory() { return false; }
}
