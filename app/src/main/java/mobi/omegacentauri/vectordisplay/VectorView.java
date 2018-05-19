package mobi.omegacentauri.vectordisplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Alexander_Pruss on 10/13/2017.
 */

public class VectorView extends View {
    RecordAndPlay record;
    boolean redraw = false;
    Bitmap bitmap;
    Canvas savedCanvas = null;

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (bitmap != null) {
            bitmap.recycle();
        }
        savedCanvas = new Canvas();
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        savedCanvas.setBitmap(bitmap);
        redraw = true;
    }

    public VectorView(Context c, RecordAndPlay r) {
        super(c);

        this.record = r;
        this.redraw = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (savedCanvas == null) {
            Log.v("VectorDisplay", "creating savedCanvas");
            onSizeChanged(0,0, canvas.getWidth(), canvas.getHeight());
        }
        if (redraw) {
            Log.v("VectorDisplay", "redrawing");
            record.redraw(savedCanvas);
            redraw = false;
        }
        else {
            Log.v("VectorDisplay", "drawing");
            record.draw(savedCanvas);
        }
        Log.v("VectorDisplay", "bitmap "+bitmap.getWidth()+" "+bitmap.getHeight());
        canvas.drawBitmap(bitmap, 0f, 0f, null);
    }
}
