package mobi.omegacentauri.vectordisplay;

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

import android.app.Activity;
import android.util.Log;

public class VectorAPI {
	public DisplayState state;
    private Map<Byte,Class<? extends Command>> map = new HashMap<Byte,Class<? extends Command>>();
    private Command currentCommand = null;
    private Buffer buffer = new Buffer();
    private static final int TIMEOUT = 5000;
    private long commandStartTime;
    private Activity context;
    private byte lastChar = 0;
		
	public VectorAPI(Activity context) {
	    this.context = context;
		this.state = new DisplayState();
		map.put((byte) 'C', Clear.class);
		map.put((byte) 'L', Line.class);
		map.put((byte) 'R', FillRectangle.class);
		map.put((byte) 'T', Text.class);
		map.put((byte) 'P', Point.class);
		map.put((byte) 'B', Attribute32.class);
		map.put((byte) 'A', Attribute16.class);
		map.put((byte) 'Y', Attribute8.class);
		map.put((byte) 'M', PopupMessage.class);
		map.put((byte) 'E', Reset.class);
        map.put((byte) 'I', Circle.class);
        map.put((byte) 'J', FillCircle.class);
        map.put((byte) 'U', AddButton.class);
		map.put((byte) 'D', DeleteButton.class);
		map.put((byte) 'O', PolyLine.class);
	}

	synchronized public Command parse(byte ch) {
		DisplayState newState;

		if (currentCommand != null && System.currentTimeMillis() < commandStartTime + TIMEOUT) {
			if (!buffer.put(ch)) {
				currentCommand = null;
				return null;
			}
			newState = currentCommand.parse(context, buffer);
			if (newState != null) {
				Command c = currentCommand;
				currentCommand = null;
				state = newState;
				return c;
			}
			else if (currentCommand.errorState) {
				currentCommand = null;
			    return null;
            }
		}
		else {
			if (lastChar != 0 && (0xFF & ch) == (lastChar ^ 0xFF)) {
                Class<? extends Command> cl = map.get(lastChar);
                lastChar = 0;
                if (cl != null) {
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
                    }
                }
			}
			else {
			    if ('A' <= ch && ch <= 'Z') {
			        lastChar = ch;
                }
                else {
			        lastChar = 0;
                }
			}
		}
		return null;
	}
	
	static class Buffer {
		static final int MAX_BUFFER = 1024*256;
		byte[] data = new byte[MAX_BUFFER];
		int inBuffer = 0;
		
		void clear() {
			inBuffer = 0;
		}
		
		boolean put(byte b) {
			if (inBuffer < MAX_BUFFER) {
				data[inBuffer++] = b;
				return true;
			}
			else {
				return false;
			}
		}
		
		int length() {
			return inBuffer;
		}

		boolean checksum() {
			int sum = 0;
			for (int i=0; i<inBuffer-1; i++) {
				sum = ((data[i] & 0xFF) + sum) & 0xFF;
			}
			sum ^= 0xFF;
			return data[inBuffer-1] == (byte)sum;
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

		public byte getByte(int i) {
			return data[i];
		}

        public float getFixed16(int i) {
			return getInteger(i, 2) / 256f;
        }

        public int getColor565(int i) {
			int c = getInteger(i, 2);
			int r = ((c & ((1<<5)-1)) * 255 + (1<<4)) / ((1<<5)-1);
			int g =( ((c>>5) & ((1<<6)-1)) * 255 + (1<<5)) / ((1<<6)-1);
			int b = ((c>>11) * 255 + (1<<4)) / ((1<<5)-1);
			return (0xFF<<24)|(r<<16)|(g<<8)|b;
        }

		public float getFixed32(int i) {
			return getInteger(i, 4) / 65536f;
		}
	}

}
