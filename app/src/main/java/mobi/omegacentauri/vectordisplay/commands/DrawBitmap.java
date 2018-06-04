package mobi.omegacentauri.vectordisplay.commands;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Arrays;

import mobi.omegacentauri.vectordisplay.DisplayState;
import mobi.omegacentauri.vectordisplay.VectorAPI.Buffer;

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
	static final byte FLAG_LOW_ENDIAN_BITMAP = 1;
	static final byte FLAG_HAVE_MASK = 2;

	private static Paint DefaultPaint() {
		Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
		p.setStyle(Paint.Style.STROKE);
		return p;
	}

	@Override
	public boolean haveFullData(Buffer buffer) {
		if (buffer.length() < 4)
			return false;
		int dataLength = buffer.getInteger(0,4);
		return buffer.length() >= dataLength+1+headerSize;
	}

	public DrawBitmap(DisplayState state) {
		super(state);
	}

	@Override
	public int fixedArgumentsLength() {
		return 6;
	}

	@Override
	public DisplayState parseArguments(Activity context, Buffer buffer) {
		depth = buffer.data[4];
		flags = buffer.data[5];
		x = (short)buffer.getInteger(6, 2);
		y = (short)buffer.getInteger(8, 2);
		w = (short)buffer.getInteger(10,2);
		h = (short)buffer.getInteger(12,2);

		int bitmapOffset;
		if (depth == MONOCHROME) {
			foreColor = buffer.getInteger(14, 4);
			backColor = buffer.getInteger(18, 4);
			bitmapOffset = 22;
		}
		else {
			bitmapOffset = 14;
		}

		int bitmapLength;
		int maskLength;
		if (depth>=8) {
			bitmapLength = depth * w * h;
		}
		else {
			bitmapLength = (w + 7) / 8 * h;
		}

		if ((flags & FLAG_HAVE_MASK) != 0) {
			maskLength = (w + 7) / 8 * h;
		}
		else {
			maskLength = 0;
		}

		if (bitmapLength > 0) {
			data = Arrays.copyOfRange(buffer.data, bitmapOffset, bitmapLength);
		}
		else {
			data = null;
		}

		if (maskLength > 0) {
			mask = Arrays.copyOfRange(buffer.data, bitmapOffset + bitmapLength, maskLength);
		}
		else {
			mask = null;
		}

		return state;
	}
	
	@Override
	public void draw(Canvas c) {
		if (data == null)
			return;

		Bitmap bitmap;
		int[] pixels = new int[ w*h ];
		boolean le = (flags & FLAG_LOW_ENDIAN_BITMAP) != 0;

		if (mask == null && depth == RGB565) {
			if (le)
				for (int i = 0; i < pixels.length; i++) {
					pixels[i] = (data[2*i] & 0xFF) | ((data[2*i+1] & 0xFF) << 8);
				}
			else
				for (int i = 0; i < pixels.length; i++) {
					pixels[i] = (data[2*i] & 0xFF) | ((data[2*i+1] & 0xFF) << 8);
				}
			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		}
		else {
			if (depth == MONOCHROME) {
				if (le)
                    for (int y = 0; y < h ; y++) {
                        int yw = y * w;
                        for (int x = 0; x < w; x++)
                            if ((data[yw + x / 8] & (1 << (x % 8))) != 0)
                                pixels[yw + x] = foreColor;
                            else
                                pixels[yw + x] = backColor;
                    }
				else
                    for (int y = 0; y < h ; y++) {
                        int yw = y * w;
                        for (int x = 0; x < w; x++)
                            if ((data[yw + x / 8] & (1 << (7 - x % 8))) != 0)
                                pixels[yw + x] = foreColor;
                            else
                                pixels[yw + x] = backColor;
                    }
			}
			else if (depth == GRAYSCALE) {
				for (int i = 0; i < pixels.length; i++) {
					int g = data[i] & 0xFF;
					pixels[i] = 0xFF000000 | (g<<16) | (g<<8) | g;
				}
			}
			else if (depth == RGB888) {
				if (le)
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
			else if (depth == RGBA8888) {
				if (le)
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
				return;
			}
			if (mask != null) {
				if (le) {
                    for (int y = 0; y < h; y++) {
                        int yw = y * w;
                        for (int x = 0; x < w; x++)
                            if ((mask[yw + x / 8] & (1 << (x % 8))) == 0)
                                pixels[yw + x] = 0;
                    }
				}
				else {
                    for (int y = 0; y < h; y++) {
                        int yw = y * w;
                        for (int x = 0; x < w; x++)
                            if ((mask[yw + x / 8] & (1 << (7 - (x % 8)))) == 0)
                                pixels[yw + x] = 0;
                    }
				}
			}
			bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		}

		bitmap.setPixels(pixels,0,w,0,0,w,h);
		c.drawBitmap(bitmap,x,y,null);
	}
}
