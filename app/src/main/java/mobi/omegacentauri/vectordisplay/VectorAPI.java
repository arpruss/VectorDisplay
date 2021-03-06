package mobi.omegacentauri.vectordisplay;

import android.app.Activity;
import android.util.Log;

import mobi.omegacentauri.vectordisplay.commands.*;

public class VectorAPI {
	public DisplayState state;
    private Class<? extends Command>[] map = new Class[256];
    private Command currentCommand = null;
    public MyBuffer buffer = new MyBuffer();
    private static final int TIMEOUT = 5000;
    private long commandStartTime;
    private Activity context;
    private byte lastChar = 0;
    public static final byte RESET_COMMAND = 'E';
	public static final byte INITIALIZE_COMMAND = 'H';
	public static final byte ATTRIBUTE32_COMMAND = 'B';
	public static final byte INITIALIZE_WITH_RESOLUTION_COMMAND = 'Z';

	public VectorAPI(Activity context) {
	    this.context = context;
		this.state = new DisplayState();
		// need to have capital letters
		for(int i=0;i<map.length;i++)
			map[i] = null;
        map['A'] = Attribute16.class;
        map[ATTRIBUTE32_COMMAND] = Attribute32.class;
        map['C'] = Clear.class;
        map['D'] = DeleteButton.class;
        map[RESET_COMMAND] = Reset.class;
        map['F'] = Update.class;
        map['G'] = FillTriangle.class;
        map[INITIALIZE_COMMAND] = Initialize.class;
        map['I'] = Circle.class;
        map['J'] = FillCircle.class;
        map['K'] = DrawBitmap.class; // TODO: untested!
		map['L'] = Line.class;
        map['M'] = PopupMessage.class;
		map['N'] = FillPoly.class;
        map['O'] = PolyLine.class;
        map['P'] = Point.class;
        map['Q'] = RoundedRectangle.class;
		map['R'] = FillRectangle.class;
		map['S'] = Arc.class;
		map['T'] = Text.class;
        map['U'] = AddButton.class;
		map['Y'] = Attribute8.class;
		map[INITIALIZE_WITH_RESOLUTION_COMMAND] = InitializeWithResolution.class; // TODO: untested
	}

	synchronized public Command parse(byte ch) {
		DisplayState newState;

		//Log.v("VectorDisplay", Integer.toHexString(ch));

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
			if (lastChar != 0 && (0xFF & ch) == (0xFF & (lastChar ^ 0xFF))) {
                Class<? extends Command> cl = map[lastChar & 0xFF];
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
			    if (map[ch & 0xFF] != null) {
			        lastChar = ch;
                }
                else {
//			    	MainActivity.log("Unparsed char "+Integer.toHexString(ch&0xFF));
			        lastChar = 0;
                }
			}
		}
		return null;
	}
	
	public static class MyBuffer {
		static final int MAX_BUFFER = 1024*256;
		public byte[] data = new byte[MAX_BUFFER];
		public int length = 0;
		public boolean lowEndian = true;
		public static final char[] CP437 = {
				0x0000,0x0001,0x0002,0x0003,0x0004,0x0005,0x0006,0x0007,0x0008,0x0009,0x000a,0x000b,0x000c,0x000d,0x000e,0x000f,
				0x0010,0x0011,0x0012,0x0013,0x0014,0x0015,0x0016,0x0017,0x0018,0x0019,0x001a,0x001b,0x001c,0x001d,0x001e,0x001f,
				0x0020,0x0021,0x0022,0x0023,0x0024,0x0025,0x0026,0x0027,0x0028,0x0029,0x002a,0x002b,0x002c,0x002d,0x002e,0x002f,
				0x0030,0x0031,0x0032,0x0033,0x0034,0x0035,0x0036,0x0037,0x0038,0x0039,0x003a,0x003b,0x003c,0x003d,0x003e,0x003f,
				0x0040,0x0041,0x0042,0x0043,0x0044,0x0045,0x0046,0x0047,0x0048,0x0049,0x004a,0x004b,0x004c,0x004d,0x004e,0x004f,
				0x0050,0x0051,0x0052,0x0053,0x0054,0x0055,0x0056,0x0057,0x0058,0x0059,0x005a,0x005b,0x005c,0x005d,0x005e,0x005f,
				0x0060,0x0061,0x0062,0x0063,0x0064,0x0065,0x0066,0x0067,0x0068,0x0069,0x006a,0x006b,0x006c,0x006d,0x006e,0x006f,
				0x0070,0x0071,0x0072,0x0073,0x0074,0x0075,0x0076,0x0077,0x0078,0x0079,0x007a,0x007b,0x007c,0x007d,0x007e,0x007f,
				0x00c7,0x00fc,0x00e9,0x00e2,0x00e4,0x00e0,0x00e5,0x00e7,0x00ea,0x00eb,0x00e8,0x00ef,0x00ee,0x00ec,0x00c4,0x00c5,
				0x00c9,0x00e6,0x00c6,0x00f4,0x00f6,0x00f2,0x00fb,0x00f9,0x00ff,0x00d6,0x00dc,0x00a2,0x00a3,0x00a5,0x20a7,0x0192,
				0x00e1,0x00ed,0x00f3,0x00fa,0x00f1,0x00d1,0x00aa,0x00ba,0x00bf,0x2310,0x00ac,0x00bd,0x00bc,0x00a1,0x00ab,0x00bb,
				0x2591,0x2592,0x2593,0x2502,0x2524,0x2561,0x2562,0x2556,0x2555,0x2563,0x2551,0x2557,0x255d,0x255c,0x255b,0x2510,
				0x2514,0x2534,0x252c,0x251c,0x2500,0x253c,0x255e,0x255f,0x255a,0x2554,0x2569,0x2566,0x2560,0x2550,0x256c,0x2567,
				0x2568,0x2564,0x2565,0x2559,0x2558,0x2552,0x2553,0x256b,0x256a,0x2518,0x250c,0x2588,0x2584,0x258c,0x2590,0x2580,
				0x03b1,0x00df,0x0393,0x03c0,0x03a3,0x03c3,0x00b5,0x03c4,0x03a6,0x0398,0x03a9,0x03b4,0x221e,0x03c6,0x03b5,0x2229,
				0x2261,0x00b1,0x2265,0x2264,0x2320,0x2321,0x00f7,0x2248,0x00b0,0x2219,0x00b7,0x221a,0x207f,0x00b2,0x25a0,0x00a0,
		};
		
		void clear() {
			length = 0;
		}
		
		boolean put(byte b) {
			if (length < MAX_BUFFER) {
				data[length++] = b;
				return true;
			}
			else {
				return false;
			}
		}
		
		public boolean checksum() {
			int sum = 0;
			for (int i = 0; i< length -1; i++) {
				sum = ((data[i] & 0xFF) + sum) & 0xFF;
			}
			sum ^= 0xFF;
//			Log.v("VectorDisplay", "checksum "+data[length-1]+" vs "+sum);
			return data[length -1] == (byte)sum;
		}

		public short getShort(int start) {
			if (lowEndian)
				return (short) ((data[start] & 0xFF) | (data[start+1] << 8));
			else
				return (short) ((data[start+1] & 0xFF) | (data[start] << 8));
		}

		public int getInt(int start) {
			if (lowEndian)
				return (data[start] & 0xFF) | ((data[start+1] & 0xFF) << 8) |
						((data[start+2] & 0xFF) << 16) | ((data[start+3] & 0xFF) << 24);
			else
				return (data[start+3] & 0xFF) | ((data[start+2] & 0xFF) << 8) |
						((data[start+1] & 0xFF) << 16) | ((data[start] & 0xFF) << 24);
		}

		String getString(int start, DisplayState state) {
			return getString(start, length -start, state);
		}
		
		public String getString(int start, int length, DisplayState state) {
			if (state.cp437) {
				char[] out = new char[length];
				for (int i=0; i<length; i++)
					out[i] = CP437[0xFF & (int)data[start+i]];
				return new String(out);
			}
			try
            {
				return new String(data, start, length);
			}
			catch(Exception e) {
			    Log.e("VectorDisplay", "error in decoding text");
				return "";
			}
		}

		public byte getByte(int i) {
			return data[i];
		}

        public float getFixed16(int i) { // unsigned
			return (0xFFFF&getShort(i)) / 256f;
        }

        public int getColor565(int i) {
			int c = 0xFFFF & getShort(i);
			int r = ((c & ((1<<5)-1)) * 255 + (1<<4)) / ((1<<5)-1);
			int g =( ((c>>5) & ((1<<6)-1)) * 255 + (1<<5)) / ((1<<6)-1);
			int b = ((c>>11) * 255 + (1<<4)) / ((1<<5)-1);
			return (0xFF<<24)|(r<<16)|(g<<8)|b;
        }

		public float getFixed32(int i) {
			return getInt(i) / 65536f;
		}
	}
}
