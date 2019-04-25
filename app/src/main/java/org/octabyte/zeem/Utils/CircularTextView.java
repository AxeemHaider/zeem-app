package org.octabyte.zeem.Utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import org.octabyte.zeem.R;

/**
 * Created by Azeem on 8/16/2017.
 */


public class CircularTextView extends android.support.v7.widget.AppCompatTextView
{
    private float strokeWidth;
    private int strokeColor;
    private Paint strokePaint;
    private String colorValue;

    public CircularTextView(Context context) {
        super(context);
        setStrokeWidth(1);
        setStrokeColor("#ffffff");
    }

    public CircularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setStrokeWidth(1);
        setStrokeColor("#ffffff");

        strokePaint = new Paint();
        strokePaint.setColor(strokeColor);
        strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularTextView);
        try {
            colorValue = typedArray.getString(R.styleable.CircularTextView_solidColor);
            //setSolidColor(colorValue);
        }finally {
            typedArray.recycle();
        }

    }

    public CircularTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setStrokeWidth(1);
        setStrokeColor("#ffffff");
        //setSolidColor("#000000");
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        Paint circlePaint = new Paint();
        circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        String[] colors = colorValue.split(",");
        if(colors.length > 1){
            circlePaint.setShader(
                    new LinearGradient(0,0,getWidth(),0,Color.parseColor(colors[0]),Color.parseColor(colors[1]), Shader.TileMode.MIRROR)
            );
        }else{
            circlePaint.setColor(Color.parseColor(colors[0]));
        }

        int  h = this.getHeight();
        int  w = this.getWidth();

        int diameter = ((h > w) ? h : w);
        int radius = diameter/2;

        this.setHeight(diameter);
        this.setWidth(diameter);

        canvas.drawCircle(diameter / 2 , diameter / 2, radius, strokePaint);

        canvas.drawCircle(diameter / 2, diameter / 2, radius-strokeWidth, circlePaint);

    }

    public void setStrokeWidth(int dp)
    {
        float scale = getContext().getResources().getDisplayMetrics().density;
        strokeWidth = dp*scale;

    }

    private void setStrokeColor(String color)
    {
        strokeColor = Color.parseColor(color);
    }

}