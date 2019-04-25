package org.octabyte.zeem.Utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import org.octabyte.zeem.R;

public class FocusView extends LinearLayout {
    private Bitmap bitmap;

    private float centerX, centerY, radius;

    public FocusView(Context context) {
        super(context);

        centerX = 0;
        centerY = 0;
        radius = 0;

    }

    public FocusView(Context context, AttributeSet attrs) {
        super(context, attrs);

        centerX = 0;
        centerY = 0;
        radius = 0;

    }

    public FocusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        centerX = 0;
        centerY = 0;
        radius = 0;

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FocusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        centerX = 0;
        centerY = 0;
        radius = 0;

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (bitmap == null) {
            createWindowFrame();
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private void createWindowFrame() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(bitmap);

        RectF outerRectangle = new RectF(0, 0, getWidth(), getHeight());

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.focus_view_background));
        osCanvas.drawRect(outerRectangle, paint);

        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        osCanvas.drawCircle(centerX, centerY, radius, paint);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }

    public void setFocusParams(float centerX, float centerY, float radius){
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }
}
