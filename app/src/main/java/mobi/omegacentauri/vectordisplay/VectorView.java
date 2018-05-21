package mobi.omegacentauri.vectordisplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
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
    float aspectRatio = 4f/3f;

    @Override
    protected void onMeasure(int wspec, int hspec) {
        int w = View.MeasureSpec.getSize(wspec);
        int h = View.MeasureSpec.getSize(hspec);
        MainActivity.log( "onmeasure "+w+" "+h );
        if ((float)w/h >= aspectRatio) {
            w = (int) (h * aspectRatio);
        }
        else {
            h = (int) (w / aspectRatio);
        }
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (bitmap != null) {
            bitmap.recycle();
        }
        savedCanvas = new Canvas();
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        savedCanvas.setBitmap(bitmap);
        MainActivity.log( "size "+w+" "+h+" "+(double)w/h);
        redraw = true;
    }

    public VectorView(Context c, AttributeSet set) {
        super(c,set);

        this.redraw = true;
        this.record = ((MainActivity)c).record;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (savedCanvas == null) {
            MainActivity.log( "creating savedCanvas");
            onSizeChanged(0,0, canvas.getWidth(), canvas.getHeight());
        }
        if (redraw) {
            MainActivity.log( "redrawing");
            record.redraw(savedCanvas);
            redraw = false;
        }
        else {
            MainActivity.log( "drawing");
            record.draw(savedCanvas);
        }
        MainActivity.log( "bitmap "+bitmap.getWidth()+" "+bitmap.getHeight());
        canvas.drawBitmap(bitmap, 0f, 0f, null);
    }
}
