package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

public class PopupMessage extends Command {
    private String text;

    public PopupMessage(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 0;
	}

	@Override
	public boolean haveStringArgument() {
		return true;
	}

	@Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		text = buffer.getString(0, buffer.length()-2);
		return state;
	}

    @Override
    public void handleCommand(Handler h) {
        Message msg = h.obtainMessage(MainActivity.TOAST);
        Bundle b = new Bundle();
        b.putString(MainActivity.KEY_LABEL, text);
        msg.setData(b);
        h.sendMessage(msg);
    }

    @Override
    public boolean doesDraw() { // does this type actually do anything when show() is called?
        return false;
    }
}
