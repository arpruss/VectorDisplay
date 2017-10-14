package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;
import mobi.omegacentauri.vectordisplay.VectorAPI.DisplayState;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

public class Text extends Command {
	int x1,y1;
	String text;
	
	public Text(DisplayState state) {
		super(state);
	}
	
	@Override 
	public DisplayState parse(Buffer buffer, byte c) {
		if (buffer.length() >= 4 && c == 0) {
			x1 = buffer.getInteger(0, 2);
			y1 = buffer.getInteger(2, 2);
			text = buffer.getString(4);
			return state;
		}
		else {
			buffer.put(c);		
			return null;
		}
	}
	
	@Override
	public DisplayState parse(Buffer buffer) {
		return null;
	}

	@Override
	public void draw(Canvas c) {
		Paint p = new Paint();
		p.setStyle(Style.FILL);
		p.setColor(state.foreColor);
		
		p.setTextAlign(state.align == 'l' ? Align.LEFT : state.align == 'r' ? Align.RIGHT : Align.CENTER );
		p.setTextSize(state.scale(c, 0, state.textSize, false).y);
		Coords xy = state.scale(c, x1, y1, false);
		c.drawText(text, xy.x, xy.y, p);
	}
}
