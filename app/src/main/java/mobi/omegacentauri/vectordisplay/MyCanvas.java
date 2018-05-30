package mobi.omegacentauri.vectordisplay;

import android.graphics.Canvas;
import android.graphics.Matrix;

public class MyCanvas extends Canvas {
    int prevW=-1;
    int prevH=-1;
    int prevR=-1;
    Matrix matrix = new Matrix();

    public MyCanvas() {
        super();
    }

    public void transform(DisplayState state) {
        if (state.rotate == prevR && state.width == prevW && state.height == prevH)
            return;
        if (state.width <= 0 || state.height <= 0)
            return;
        matrix.setScale((float)(getWidth())/(state.width), (float)(getHeight())/(state.height));
        matrix.preTranslate(0.5f,0.5f);
        matrix.postRotate(state.rotate * 90, getWidth() / 2f, getHeight() / 2f);
        if ((state.rotate & 3) == 1) {
            float delta = (getHeight()-getWidth())/2f;
            matrix.postTranslate(-delta, -delta);
        }
        else if ((state.rotate & 3) == 3) {
            float delta = (getHeight()-getWidth())/2f;
            matrix.postTranslate(delta, delta);
        }
        prevW = state.width;
        prevH = state.height;
        prevR = state.rotate;
        setMatrix(matrix);
    }
}
