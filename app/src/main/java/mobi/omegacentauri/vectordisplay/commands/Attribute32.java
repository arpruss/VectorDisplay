package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Canvas;
import android.os.Handler;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.RecordAndPlay;
import mobi.omegacentauri.vectordisplay.VectorAPI;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

public class Attribute32 extends Command {
	boolean resetView = false;
	boolean ack = false;

    public Attribute32(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 5;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		switch((char)buffer.data[0]) {
            case 't':
                state.thickness = buffer.getFixed32(1);
                break;
			case 'c':
                state.width = buffer.getShort(1);
                state.height = buffer.getShort(3);
                ack = true;
                resetView = true;
 				break;
            case 'a':
                state.pixelAspectRatio = buffer.getFixed32(1);
                resetView = true;
                break;
			case 's':
				state.textSize = buffer.getFixed32(1);
				break;
			case 'b':
				state.backColor = buffer.getInt(1);
				break;
			case 'k':
				state.textBackColor = buffer.getInt(1);
				break;
			case 'F':
				state.textForeColor = buffer.getInt(1);
				break;
			case 'f':
				state.foreColor = buffer.getInt(1);
				break;
			case 'x':
				state.cursorX = buffer.getFixed32(1);
				break;
			case 'y':
				state.cursorY = buffer.getFixed32(1);
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
    public boolean handleCommand(Handler h) {
        if (resetView)
            MainActivity.sendResetViewMessage(h, state);
        if (ack)
			sendAck(h, VectorAPI.ATTRIBUTE32_COMMAND, RecordAndPlay.LAYOUT_DELAY);
        return resetView;
	}
}
