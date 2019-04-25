package org.octabyte.zeem.Camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.octabyte.zeem.Camera.camfrags.Drawing;
import org.octabyte.zeem.Camera.camutil.FragmentCommunicator;
import org.octabyte.zeem.Camera.helper.GeneralUtils;

import java.util.ArrayList;
import java.util.List;

import static org.octabyte.zeem.Camera.CameraActivity.TAG;

/**
 * Created by Azeem on 6/20/2017.
 */

public class CameraDrawing extends View {

    private FragmentCommunicator fragmentCommunicator;

    private Context mContext;

    private Path drawPath;
    private Paint drawPaint, canvasPaint, _paintSimple, _paintBlur;
    private int paintColor = 0xFFFFFFFF;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private float brushSize = 15;

    private Boolean drawingPath = false;
    private Boolean neonEffect = false;

    // Rainbow path variables
    private Boolean rainbowPath = false;
    private Paint mCirclePaint;
    private Path mCirclePath;
    private Paint mLinePaint;
    private Path mLinePath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 10;
    private int mCurrentColorIndex = 0;
    private List<Integer> mColoursList;

    public CameraDrawing(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setupDrawing();
    }

    private void setupDrawing(){
        Log.i(TAG, "setupDrawing");

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        drawPaint.setStrokeWidth(brushSize);

        _paintSimple = new Paint();
        _paintSimple.setAntiAlias(true);
        _paintSimple.setDither(true);
        _paintSimple.setColor(Color.argb(248, 255, 255, 255));
        _paintSimple.setStrokeWidth(brushSize);
        _paintSimple.setStyle(Paint.Style.STROKE);
        _paintSimple.setStrokeJoin(Paint.Join.ROUND);
        _paintSimple.setStrokeCap(Paint.Cap.ROUND);

        _paintBlur = new Paint();
        _paintBlur.set(_paintSimple);
        _paintBlur.setColor(paintColor);
        _paintBlur.setAlpha(235);
        _paintBlur.setStrokeWidth(brushSize+10);
        _paintBlur.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));

        setupRainbowPath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        if(drawingPath){
            if(neonEffect){
                canvas.drawPath(drawPath, _paintBlur);
                canvas.drawPath(drawPath, _paintSimple);
            }else if(rainbowPath){
                canvas.drawPath(mCirclePath, mCirclePaint);
                canvas.drawPath(mLinePath, mLinePaint);
            }else {
                    canvas.drawPath(drawPath, drawPaint);
            }
        }

    }

    public void drawEmoji(int x, int y, String src){
        drawCanvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(), GeneralUtils.getDrawableIdentifier(mContext, src)), x, y, null);
    }

    public void drawText(String mText, int yPosText, float mTextSize, int mTextPadding, int bgColor, int color){
        
        Paint paintRect = new Paint();
        paintRect.setColor(bgColor);
        paintRect.setStyle(Paint.Style.FILL_AND_STROKE);
        paintRect.setStrokeWidth(10);

        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(color);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(mTextSize);
        textPaint.setAntiAlias(true);

        int xPosText = (drawCanvas.getWidth() / 2);
        Rect bounds = new Rect();
        textPaint.getTextBounds(mText, 0, mText.length(), bounds);
        int height = bounds.height();

        Rect rect = new Rect(0, yPosText - mTextPadding, drawCanvas.getWidth(), yPosText + height + mTextPadding);
        drawCanvas.drawRect(rect, paintRect);
        drawCanvas.drawText(mText, xPosText, yPosText + height, textPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "onTouchEvent");
        if(drawingPath) {
            float touchX = event.getX();
            float touchY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    fragmentCommunicator.doAction("hideLayout");
                    if(rainbowPath){
                        touchStart(touchX, touchY);
                    }else {
                        drawPath.moveTo(touchX, touchY);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(rainbowPath){
                        touchMove(touchX, touchY);
                        mLinePaint.setColor(mColoursList.get(getColorIndex()));
                        mCirclePaint.setColor(mColoursList.get(getColorIndex()));
                    }else {
                        drawPath.lineTo(touchX, touchY);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    fragmentCommunicator.doAction("showLayout");
                    if(neonEffect){
                        drawCanvas.drawPath(drawPath, _paintBlur);
                        drawCanvas.drawPath(drawPath, _paintSimple);
                    }else if(rainbowPath) {
                        touchUp();
                    }else{
                        drawCanvas.drawPath(drawPath, drawPaint);
                    }
                    drawPath.reset();
                    break;
                default:
                    return false;
            }
            invalidate();
        }
        return true;
    }

    public void setColor(String newColor){
        Log.i(TAG, "setColor");
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
        drawPaint.setStrokeWidth(brushSize);
        _paintSimple.setStrokeWidth(brushSize);
        _paintBlur.setColor(paintColor);
        _paintBlur.setStrokeWidth(brushSize+10);
    }

    public void setBrushSize(float newSize){
        Log.i(TAG, "setBrushSize");

        brushSize=newSize;
        Log.i(TAG, "setBrushSize "+ brushSize);
        drawPaint.setStrokeWidth(brushSize);
        _paintSimple.setStrokeWidth(brushSize);
        _paintBlur.setStrokeWidth(brushSize+10);
        mLinePaint.setStrokeWidth((float) (brushSize * 2.5));
    }

    public float getBrushSize(){
        return brushSize;
    }

    public void startNew(){
        Log.i(TAG, "startNew");
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        invalidate();
    }
    public void drawingEraser(boolean isErase){
        neonEffect = false;
        rainbowPath = false;
        Boolean erase = isErase;
        if(erase) {
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }else {
            drawPaint.setXfermode(null);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.i(TAG, "onSizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    public void bindCommunicator(){
        FragmentManager fragmentManager = ((Activity)getContext()).getFragmentManager();
        Drawing cameraDrawing = (Drawing) fragmentManager.findFragmentByTag("cameraDrawing");
        fragmentCommunicator = cameraDrawing;
    }

    /**
     * Rainbow Path:
     * it choose automatically color and mix them
     * show as a rainbow color
     * single line draw
     */

    private void setupRainbowPath(){
        mColoursList = initRainbowColors();
        mCirclePath = new Path();
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setDither(true);
        mCirclePaint.setColor(getColorIndex());
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePath = new Path();
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);
        mLinePaint.setColor(getColorIndex());
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeWidth((float) (brushSize * 2.5));
    }
    private List<Integer> initRainbowColors(){
        final List<Integer> colorsList = new ArrayList<>();
        for (int r = 0; r < 100; r++) colorsList.add(Color.rgb(r * 255 / 100, 255, 0));
        for (int g = 100; g > 0; g--) colorsList.add(Color.rgb(255, g * 255 / 100, 0));
        for (int b = 0; b < 100; b++) colorsList.add(Color.rgb(255, 0, b * 255 / 100));
        for (int r = 100; r > 0; r--) colorsList.add(Color.rgb(r * 255 / 100, 0, 255));
        for (int g = 0; g < 100; g++) colorsList.add(Color.rgb(0, g * 255 / 100, 255));
        for (int b = 100; b > 0; b--) colorsList.add(Color.rgb(0, 255, b * 255 / 100));
        colorsList.add(Color.rgb(0, 255, 0));
        return colorsList;
    }
    private int getColorIndex(){
        int size = mColoursList.size();
        if(mCurrentColorIndex == size){
            mCurrentColorIndex = 0;
        }
        return mCurrentColorIndex++;
    }

    private void touchStart(float x, float y) {
        mLinePath.reset();
        mLinePath.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        mLinePath.reset();
        mLinePath.moveTo(x, y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mLinePath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            drawCanvas.drawPath(mLinePath, mLinePaint); // draw to canvas each move
            mCirclePath.reset();
            mCirclePath.addCircle(mX, mY, brushSize, Path.Direction.CW);
            drawCanvas.drawPath(mCirclePath, mCirclePaint); // draw to canvas each move
            mX = x;
            mY = y;
        }
    }
    private void touchUp() {
        mLinePath.lineTo(mX, mY);
        mCirclePath.reset();
        // commit the path to our offscreen
        drawCanvas.drawPath(mLinePath, mLinePaint);
        // kill this so we don't double draw
        mLinePath.reset();
    }

    //---------------- Ending of Rainbow setup ------------------------

    public void startDrawing(){
        drawingPath = true;
    }
    public void stopDrawing(){
        drawingPath = false;
    }
    public void simplePath(){
        neonEffect = false;
        rainbowPath = false;
        drawingEraser(false);
    }
    public void setNeonEffect(Boolean effect){
        rainbowPath = false;
        drawingEraser(false);
        neonEffect = effect;
    }
    public void setRainbowPath(Boolean effect){
        neonEffect = false;
        drawingEraser(false);
        rainbowPath = effect;
        if(rainbowPath)
            setupRainbowPath();
    }

}
