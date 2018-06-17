package mobi.omegacentauri.vectordisplay;

import android.graphics.Canvas;
import android.graphics.Matrix;

public class Transformation extends Matrix {
    int prevCanvasW=-1;
    int prevEmulatedW=-1;
    int prevCanvasH=-1;
    int prevEmulatedH=-1;
    byte prevRotate=-1;

    public Transformation() {
        super();
    }

    public void update(Canvas c, DisplayState state) {
        int width = c.getWidth();
        int height = c.getHeight();
        if (state.width == prevEmulatedW && state.height == prevEmulatedH &&
            state.rotate == prevRotate && width == prevCanvasW && height == prevCanvasH)
            return;

        if (state.width <= 1 || state.height <=1 || c.getWidth() <=1 || c.getHeight() <=1)
            return;

        setScale((float)(width)/(state.width), (float)(height)/(state.height));
        preTranslate(0.5f,0.5f);
        postRotate(state.rotate * 90, width / 2f, height / 2f);
        if ((state.rotate & 3) == 1) {
            float delta = (height-width)/2f;
            postTranslate(-delta, -delta);
        }
        else if ((state.rotate & 3) == 3) {
            float delta = (height-width)/2f;
            postTranslate(delta, delta);
        }
        prevEmulatedW = state.width;
        prevEmulatedH = state.height;
        prevRotate = state.rotate;
    }
}
