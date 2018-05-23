package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
            case 't':
                state.thickness = buffer.getFixed32(1);
                break;
			case 'c':
                state.width = buffer.getInteger(1, 2);
                state.height = buffer.getInteger(3, 2);
                resetView = true;
                Log.v("VectorDisplay", "resetView needed because of coordinate change to "+state.width+" "+state.height);
 				break;
            case 'a':
                state.pixelAspectRatio = buffer.getFixed32(1);
                resetView = true;
                break;
			case 's':
				state.textSize = buffer.getInteger(1, 4)/65536f;
				break;
			case 'b':
				state.backColor = buffer.getInteger(1, 4);
				break;
			case 'k':
				state.textBackColor = buffer.getInteger(1, 4);
				break;
			case 'f':
				state.foreColor = buffer.getInteger(1, 4);
				break;
		}
		return state;
	}
	
	@Override
	public boolean doesDraw() {
		return resetView;
	}

//	@Override
//    public boolean needToResetView() { return resetView; }

    @Override
    public void draw(Canvas c) {
        Clear.clearCanvas(c, state);
    }

    @Override
    public void handleCommand(Handler h) {
        if (resetView)
            MainActivity.sendResetViewMessage(h, state);
    }

}
