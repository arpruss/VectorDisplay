package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import android.graphics.Canvas;

public class Command {
	VectorAPI.DisplayState state;
	
	public Command(VectorAPI.DisplayState state) {
		this.state = state;
	}
	
	public VectorAPI.DisplayState parse(Buffer buffer, byte c) {
		return state;
	}

	public VectorAPI.DisplayState parse(Buffer buffer) {
		return state;
	}
	
	public void draw(Canvas c) {
	}

	public boolean doesDraw() { // does this type actually do anything when show() is called?
		return true;
	}
}
