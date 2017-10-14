package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.VectorAPI.DisplayState;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class TextStyle extends Command {
	int x1,y1;
	
	public TextStyle(DisplayState state) {
		super(state);
	}
	
	@Override 
	public DisplayState parse(Buffer buffer, byte c) {
		buffer.put(c);
		if (buffer.length() >= 3) {
			state.align = (char)buffer.data[0];
			state.textSize = buffer.getInteger(1, 2);
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
