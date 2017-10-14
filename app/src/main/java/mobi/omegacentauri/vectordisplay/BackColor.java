package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.VectorAPI.DisplayState;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class BackColor extends Command {
	public BackColor(DisplayState state) {
		super(state);
	}
	
	@Override 
	public DisplayState parse(Buffer buffer, byte c) {
		buffer.put(c);
		if (buffer.length() >= 4) {
			state.backColor = buffer.getInteger(0,4);
			return state;
		}
		return null;
	}
	
	@Override
	public DisplayState parse(Buffer buffer) {
		return null;
	}

	@Override
	public boolean doesDraw() {
		return false;
	}
}
