package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

import android.app.Activity;
import android.graphics.Canvas;

public class Attribute16 extends Command {
	boolean resetView = false;

    public Attribute16(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 3;
	}

	@Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		switch((char)buffer.data[0]) {
			case 'b':
				state.backColor = buffer.getColor565(1);
				break;
			case 'k':
				state.textBackColor = buffer.getColor565(1);
				break;
			case 'f':
				state.foreColor = buffer.getColor565(1);
				break;
            case 'a':
                state.pixelAspectRatio = buffer.getFixed16(1);
                resetView = true;
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
