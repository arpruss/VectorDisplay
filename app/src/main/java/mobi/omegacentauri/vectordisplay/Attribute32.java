package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.util.Log;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

public class Attribute32 extends Command {
	boolean resetView = false;

    public Attribute32(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 5;
	}

	@Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		switch((char)buffer.data[0]) {
			case 'b':
				state.backColor = buffer.getInteger(1, 4);
				break;
			case 'f':
				state.foreColor = buffer.getInteger(1, 4);
				break;
			case 'c':
                state.width = buffer.getInteger(0, 2);
                state.height = buffer.getInteger(2, 2);
 				break;
		}
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return resetView;
	}

	@Override
    public boolean needToResetView() { return resetView; }

    @Override
    public void draw(Canvas c) {
        Clear.clearCanvas(c, state);
    }
}
