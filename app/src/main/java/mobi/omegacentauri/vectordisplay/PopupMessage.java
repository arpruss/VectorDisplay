package mobi.omegacentauri.vectordisplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.widget.Toast;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.DisplayState;

public class PopupMessage extends Command {
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
	public DisplayState parseArguments(Context context, Buffer buffer) {
		String text = buffer.getString(0, buffer.length()-1);
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
		return null;
	}
}
