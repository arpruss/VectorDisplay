package mobi.omegacentauri.vectordisplay.commands;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

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
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		switch((char)buffer.data[0]) {
			case 'b':
				state.backColor = buffer.getColor565(1);
				break;
			case 'k':
				state.textBackColor = buffer.getColor565(1);
				break;
			case 'F':
				state.textForeColor = buffer.getColor565(1);
				break;
			case 'f':
				state.foreColor = buffer.getColor565(1);
				break;
		}
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return resetView;
	}

    @Override
    public void draw(Canvas c) {
        Clear.clearCanvas(c, state);
    }
}
