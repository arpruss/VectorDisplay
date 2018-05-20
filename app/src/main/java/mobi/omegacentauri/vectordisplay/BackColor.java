package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.DisplayState;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class BackColor extends Command {
	public BackColor(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 4;
	}
	
	@Override 
	public DisplayState parseArguments(Context context, Buffer buffer) {
		state.backColor = buffer.getInteger(0,4);
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return false;
	}
}
