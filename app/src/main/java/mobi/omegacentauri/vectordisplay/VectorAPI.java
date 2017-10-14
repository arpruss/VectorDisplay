package mobi.omegacentauri.vectordisplay;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/*
 * parentheses indicate number of bytes of data
 * 
 * From microcontroller to Android:
 * C : clear
 * L x1(2) y1(2) x2(2) y2(2) : draw line from (x1,y1) to (x2,y2)
 * R x1(2) y1(2) x2(2) y2(2) : draw rectangle from (x1,y1) to (x2,y2)
 * P x(2) y(2) : draw point at (x,y)
 * T x(2) y(2) text(up to 1020) : draw text at (x,y)
 * Z width(2) height(2) keep_aspect(1) : set screen coordinate sizes
 * S horizontal_align(1) size(2) : set text style; horizontal alignments: l/c/r
 * B rgba(4) : background color
 * F rgba(4) : foreground color
 * 
 * From Android to microcontroller:
 * T n(1) x(2) y(2) : number of touches and touch locations
 */

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

public class VectorAPI {
	DisplayState state;
	Map<Byte,Class<? extends Command>> map = new HashMap<Byte,Class<? extends Command>>();
	Command currentCommand = null;
	Buffer buffer = new Buffer();
	static final int TIMEOUT = 5000;
	long commandStartTime;
		
	public VectorAPI() {	
		state = new DisplayState();
		map.put((byte) 'C', Clear.class);
		map.put((byte) 'L', Line.class);
		map.put((byte) 'R', Rectangle.class);
		map.put((byte) 'T', Text.class);
		map.put((byte) 'P', Point.class);
		map.put((byte) 'S', TextStyle.class);
		map.put((byte) 'F', ForeColor.class);
		map.put((byte) 'B', BackColor.class);
		map.put((byte) 'Z', CoordinateSystem.class);
	}
	
	public Command parse(byte ch) {
		DisplayState newState;
		if (currentCommand != null && System.currentTimeMillis() < commandStartTime + TIMEOUT) {
			newState = currentCommand.parse(buffer, ch);
			if (newState != null) {
				Command c = currentCommand;
				currentCommand = null;
				state = newState;
				return c.doesDraw() ? c : null;
			}
		}
		else {
			Class<? extends Command> cl = map.get(ch);
			if (cl != null) {
				Log.v("VectorDisplay", cl.toString());
				Command c;
				try {
					c = (Command) cl.getConstructor(DisplayState.class).newInstance(state);
				} catch (Exception e) {
					Log.e("VectorDisplay", e.toString());
					c = null;
				}
				if (c != null) {
					currentCommand = c;
					commandStartTime = System.currentTimeMillis();
					buffer.clear();
					newState = currentCommand.parse(buffer);
					if (newState != null) {
						state = newState;
						currentCommand = null;
						return c.doesDraw() ? c : null;
					}
				}
			}
			else {
				Log.v("VectorDisplay", "unparsed "+ch);
			}
		}
		return null;
	}
	
	static class Buffer {
		static final int MAX_BUFFER = 1024;
		byte[] data = new byte[MAX_BUFFER];
		int inBuffer = 0;
		
		void clear() {
			inBuffer = 0;
		}
		
		void put(byte b) {
			if (inBuffer < MAX_BUFFER)
				data[inBuffer++] = b;
		}
		
		int length() {
			return inBuffer;
		}
		
		int getInteger(int start, int length) {
			try {
				int x = 0;
				int bits = 0;
				while (length-- > 0) {
					x = ((0xFF & (int)data[start++]) << bits) | x;
					bits += 8;
				}
				return x;
			}
			catch(Exception e) {
				return 0;
			}
		}
		
		String getString(int start) {
			return getString(start, inBuffer-start);
		}
		
		String getString(int start, int length) {
			try {
				return new String(data, start, length);
			}
			catch(Exception e) {
				return "";
			}
		}
	}
	
	static class DisplayState {
		int width = 84;
		int height = 48;
		boolean fit = true;
		int foreColor = Color.WHITE;
		int backColor = Color.BLACK;
		float thickness = 1f;
		int textSize = 12;
		char align = 'l';
		boolean bold = false;
		
		public Coords getScale(Canvas c) {
			int cw = c.getWidth();
			int ch = c.getHeight();
			if (fit) {
				float s;
				if (cw * height > ch * width) {
					s = ((float)ch)/height;
					Log.v("VectorDisplay", "s1 "+ch+" "+height+" "+s);
				}
				else {
					s = ((float)cw)/width;
					Log.v("VectorDisplay", "s2 "+cw+" "+width+" "+s);
				}
				return new Coords(s,s);
			}
			else
				return new Coords((float)cw/width, (float)ch/height);
		}
		
		public Coords scale(Canvas c, int x, int y, boolean centerInPixel) {
			Coords s = getScale(c);
			if (centerInPixel)
				return new Coords((x+0.5f) * s.x, (y+0.5f) * s.y);
			else
				return new Coords(x * s.x, y * s.y);
		}
		
		public Coords unscale(Canvas c, float x, float y) {
			Coords s = getScale(c);
			return new Coords(x/s.x,y/s.y);
		}

		public float getThickness(Canvas c) {
			Coords s = getScale(c);
			return (float) (s.y * thickness);
		}		
	}
}
