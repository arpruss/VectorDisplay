package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.DisplayState;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

public class CoordinateSystem extends Clear {
	public CoordinateSystem(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 4;
	}
	
	@Override 
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		state.width = buffer.getInteger(0, 2);
		state.height = buffer.getInteger(2, 2);
		Log.v("VectorDisplay", "coordinate "+state.width+" "+state.height);
		return state;
	}
	
	@Override
    public boolean needToResetView() { return true; }
}
