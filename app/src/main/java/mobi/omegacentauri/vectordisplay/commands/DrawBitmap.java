package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.MainActivity;
import mobi.omegacentauri.vectordisplay.VectorAPI.MyBuffer;

public class DrawBitmap extends Command {
	short x,y,w,h;
	int length;
	byte[] data;
	byte[] mask;
	static final byte MONOCHROME=1;
	static final byte GRAYSCALE=8;
	static final byte RGB565=16;
	static final byte RGB888=24;
	static final byte RGBA8888=32;
	static final int headerSize = 14;
	int foreColor;
	int backColor;
	byte depth;
	byte flags;
	static final byte FLAG_LOW_ENDIAN_BITS = 1; // lsbit on left
	static final byte FLAG_HAVE_MASK = 2;
	static final byte FLAG_PAD_BYTE = 4;
	static final byte FLAG_LOW_ENDIAN_BYTES = 8; // lsbyte first
	static final byte FLAG_IMAGE_FILE=16; // standard Android supported image file
	int neededLength = 4;

	private static Paint DefaultPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.STROKE);
		return p;
	}

	@Override
	public boolean haveFullData(MyBuffer buffer) {
		if (buffer.length < neededLength)
			return false;

		if (neededLength <= 4) {
			neededLength = buffer.getInt(0)+1+headerSize;
		}

		return buffer.length >= neededLength;
	}

	public DrawBitmap(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 6;
	}

	@Override
	public DisplayState parseArguments(Activity context, MyBuffer buffer) {
		depth = buffer.data[4];
		flags = buffer.data[5];
		x = buffer.getShort(6);
		y = buffer.getShort(8);

		int bitmapLength;
		int maskLength;

		MainActivity.log("flags = "+flags+" depth = "+depth);
		if ((flags & FLAG_IMAGE_FILE)==0) {
			w = buffer.getShort(10);
			h = buffer.getShort(12);
			MainActivity.log("width = "+w+" height = "+h);
			if (depth>=8) {
				bitmapLength = (depth/8) * w * h;
			}
			else {
				bitmapLength =  ((flags & FLAG_PAD_BYTE) != 0) ? (w+7)/8*h : (w*h + 7) / 8;
			}

			if ((flags & FLAG_HAVE_MASK) != 0) {
				maskLength = (w*h + 7) / 8;
			}
			else {
				maskLength = 0;
			}

		}
		else {
			bitmapLength = buffer.getInt(10);
			maskLength = 0;
		}

		int bitmapOffset;
		if (depth == MONOCHROME) {
			foreColor = buffer.getInt(14);
			backColor = buffer.getInt(18);
			bitmapOffset = 22;
		}
		else {
			bitmapOffset = 14;
		}

		if (bitmapLength > 0) {
			data = Arrays.copyOfRange(buffer.data, bitmapOffset, bitmapOffset+bitmapLength);
		}
		else {
			data = null;
		}

		if (maskLength > 0) {
			mask = Arrays.copyOfRange(buffer.data, bitmapOffset + bitmapLength, bitmapOffset + bitmapLength+maskLength);
		}
		else {
			mask = null;
		}

		return state;
	}

	public Bitmap getArduinoBitmap() {
		Bitmap bitmap;

		boolean leBits = (flags & FLAG_LOW_ENDIAN_BITS) != 0;
		boolean leBytes = (flags & FLAG_LOW_ENDIAN_BYTES) != 0;

		if (mask == null && depth == RGB565) {
			short[] pixels = new short[ w*h ];
			if (leBytes)
				for (int i = 0; i < pixels.length; i++) {
					pixels[i] = (short) ( (data[2*i] & 0xFF) | ((data[2*i+1] & 0xFF) << 8));
				}
			else
				for (int i = 0; i < pixels.length; i++) {
					pixels[i] = (short) ((data[2*i] & 0xFF) | ((data[2*i+1] & 0xFF) << 8));
				}
			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
			bitmap.copyPixelsFromBuffer(ShortBuffer.wrap(pixels));
		}
		else {
			int[] pixels = new int[ w*h ];
			if (depth == MONOCHROME) {
				if ((flags & FLAG_PAD_BYTE) == 0){
					int offset = 0;
					if (leBits)
						for (int y = 0; y < h; y++) {
							int yw = y * w;
							for (int x = 0; x < w; x++) {
								if ((data[offset / 8] & (1 << (offset % 8))) != 0)
									pixels[yw + x] = foreColor;
								else
									pixels[yw + x] = backColor;
								offset++;
							}
						}
					else {
						for (int y = 0; y < h; y++) {
							int yw = y * w;
							for (int x = 0; x < w; x++) {
								if ((data[offset / 8] & (1 << (7 - offset % 8))) != 0)
									pixels[yw + x] = foreColor;
								else
									pixels[yw + x] = backColor;
								offset++;
							}
						}
					}
				}
				else {
					if (leBits)
						for (int y = 0; y < h; y++) {
							int yw = y * w;
							int yw1 = (w+7)/8 * w;
							for (int x = 0; x < w; x++) {
								if ((data[yw1 + x/8] & (1 << (x% 8))) != 0)
									pixels[yw + x] = foreColor;
								else
									pixels[yw + x] = backColor;
							}
						}
					else {
						for (int y = 0; y < h; y++) {
							int yw = y * w;
							int yw1 = (w+7)/8 * w;
							for (int x = 0; x < w; x++) {
								if ((data[yw1 + x/8] & (1 << (7 - x% 8))) != 0)
									pixels[yw + x] = foreColor;
								else
									pixels[yw + x] = backColor;
							}
						}
					}
				}
			}
			else if (depth == GRAYSCALE) {
				for (int i = 0; i < pixels.length; i++) {
					int g = data[i] & 0xFF;
					pixels[i] = 0xFF000000 | (g<<16) | (g<<8) | g;
				}
			}
			else if (depth == RGB888) {
				if (leBytes)
					for (int i = 0; i < pixels.length; i++) {
						int i3 = i*3;
						pixels[i] = 0xFF000000 | ((data[i3+2] & 0xFF) << 16) | ((data[i3+1] & 0xFF) << 8) | (data[i3] & 0xFF);
					}
				else
					for (int i = 0; i < pixels.length; i++) {
						int i3 = i*3;
						pixels[i] = 0xFF000000 | ((data[i3] & 0xFF) << 16) | ((data[i3+1] & 0xFF) << 8) | (data[i3+2] & 0xFF);
					}
			}
			else if (depth == RGB565) {
				if (leBytes)
					for (int i = 0; i < pixels.length; i++) {
						pixels[i] = rgb565to8888(data[2*i],data[2*i+1]);
					}
				else
					for (int i = 0; i < pixels.length; i++) {
						pixels[i] = rgb565to8888(data[2*i+1],data[2*i]);
					}
			}
			else if (depth == RGBA8888) {
				if (leBytes)
					for (int i = 0; i < pixels.length; i++) {
						int i4 = i*4;
						pixels[i] = ((data[i4+3] & 0xFF) << 24)  | ((data[i4+2] & 0xFF) << 16) | ((data[i4+1] & 0xFF) << 8) | (data[i4] & 0xFF);
					}
				else
					for (int i = 0; i < pixels.length; i++) {
						int i4 = i*4;
						pixels[i] = ((data[i4] & 0xFF) << 24)  | ((data[i4+1] & 0xFF) << 16) | ((data[i4+2] & 0xFF) << 8) | (data[i4+3] & 0xFF);
					}
			}
			else {
				return null;
			}

			if (mask != null) {
				if ((flags & FLAG_PAD_BYTE) == 0){
					int offset = 0;
					if (leBits)
						for (int y = 0; y < h; y++) {
							int yw = y * w;
							for (int x = 0; x < w; x++) {
								if ((mask[offset / 8] & (1 << (offset % 8))) == 0)
									pixels[yw + x] = 0;
								offset++;
							}
						}
					else {
						for (int y = 0; y < h; y++) {
							int yw = y * w;
							for (int x = 0; x < w; x++) {
								if ((mask[offset / 8] & (1 << (7 - offset % 8))) == 0)
									pixels[yw + x] = 0;
								offset++;
							}
						}
					}
				}
				else {
					if (leBits)
						for (int y = 0; y < h; y++) {
							int yw = y * w;
							int yw1 = (w+7)/8 * w;
							for (int x = 0; x < w; x++) {
								if ((mask[yw1 + x/8] & (1 << (x% 8))) == 0)
									pixels[yw + x] = foreColor;
							}
						}
					else {
						for (int y = 0; y < h; y++) {
							int yw = y * w;
							int yw1 = (w+7)/8 * w;
							for (int x = 0; x < w; x++) {
								if ((mask[yw1 + x/8] & (1 << (7 - x% 8))) == 0)
									pixels[yw + x] = foreColor;
							}
						}
					}
				}
			}

			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels,0,w,0,0,w,h);
			MainActivity.log(" pixel 0 0 = "+pixels[0]);
		}

		return bitmap;
	}
	
	@Override
	public void draw(Canvas c) {
		if (data == null)
			return;

		Bitmap bitmap;
		if ((flags & FLAG_IMAGE_FILE) == 0)
			bitmap = getArduinoBitmap();
		else
			bitmap = decodeBitmap();

		if (bitmap != null) {
		    MainActivity.log("drawing bitmap of size "+bitmap.getWidth()+" "+bitmap.getHeight()+" at "+x+" "+y);
			c.drawBitmap(bitmap, x, y, null);
			bitmap.recycle();
		}
	}

	private Bitmap decodeBitmap() {
		try {
			return BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		catch (Exception e) {
			return null;
		}
	}

	static private int rgb565to8888(byte low, byte high) {
		int c = (low&0xFF) | ((high&0xFF)<<8);
		return 0xFF000000 | ((((c>>11) & 0x1F) * 255 / 0x1F) << 16) | ((((c>>5) & 0x3F) * 255 / 0x3F) << 8) | ((c & 0x1F) * 255 / 0x1F);
	}
}
