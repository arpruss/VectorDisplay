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
        MainActivity.log( "Attr32 "+(char)buffer.data[0]);
		switch((char)buffer.data[0]) {
            case 't':
                state.thickness = buffer.getInteger(1, 4)/65536f;
                break;
			case 'c':
                state.width = buffer.getInteger(1, 2);
                state.height = buffer.getInteger(3, 2);
 				break;
			case 's':
				state.textSize = buffer.getInteger(1, 4)/65536f;
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
