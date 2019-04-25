package org.octabyte.zeem.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by Azeem on 8/5/2017.
 */

public class OnSwipeTouchListener implements View.OnTouchListener {

    private GestureDetector gestureDetector;
    private View v;

    public OnSwipeTouchListener(Context c) {
        gestureDetector = new GestureDetector(c, new GestureListener());
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        if(v == null)
            v = view;

        return gestureDetector.onTouchEvent(motionEvent);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onClick(v);
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            onDoubleClick(v);
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongClick(v);
            super.onLongPress(e);
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight(v);
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeDown(v);
                    } else {
                        onSwipeUp(v);
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    private void onSwipeRight(View v) {}

    protected void onSwipeLeft() {}

    private void onSwipeUp(View v) {}

    private void onSwipeDown(View v) {}

    public void onClick(View v) {}

    public void onDoubleClick(View v) {}

    public void onLongClick(View v) {}

}
