package org.octabyte.zeem.Camera.camutil;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Camera.helper.OnSwipeTouchListener;

/**
 * Created by Azeem on 6/19/2017.
 */

public class CameraListener extends OnSwipeTouchListener {
    private ImageView pictureBtn, videoBtn, gifBtn;
    public static String activeBtn = "picture";
    public static String btnFlow = "right";
    private static float resMoveLeft, resActivate, resDeactivate;
    private final float deviceDiff = 40f;

    private final String TAG = "com.azeem.Debug";

    public CameraListener(Context ctx, ImageView pictureBtn, ImageView videoBtn, ImageView gifBtn){
        super(ctx);

        Context context = ctx;
        this.pictureBtn = pictureBtn;
        this.videoBtn = videoBtn;
        this.gifBtn = gifBtn;

        Resources resources = context.getResources();
        resMoveLeft = resources.getDimensionPixelSize(R.dimen.camera_circle_move_left);
        resActivate = 2.2f;
        resDeactivate = 0.45f;
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.btnCapturePicture:
                if(activeBtn.equals("picture")){
                    clicked();
                }else if(btnFlow.equals("middle")){
                    activatePictureBtn();
                    btnFlow = "right";
                    activeBtn = "picture";
                }
                break;
            case R.id.btnCaptureVideo:
                if(activeBtn.equals("video")){
                    clicked();
                }else if(btnFlow.equals("right")){
                    activateVideoBtn();
                    btnFlow = "middle";
                    activeBtn = "video";
                }else if(btnFlow.equals("left")){
                    activateVideoBtnRight();
                    btnFlow = "middle";
                    activeBtn = "video";
                }
                break;
            case R.id.btnCaptureGif:
                if(activeBtn.equals("gif")){
                    clicked();
                }else if(btnFlow.equals("right")){
                    activateDirectGifBtn();
                    btnFlow = "left";
                    activeBtn = "gif";
                }else if(btnFlow.equals("middle")){
                    activateGifBtn();
                    btnFlow = "left";
                    activeBtn = "gif";
                }
        }
    }

    protected void clicked(){}

    public void onSwipeRight(){
        if(btnFlow.equals("middle")){
            activatePictureBtn();
            activeBtn = "picture";
            btnFlow = "right";
        }else if(btnFlow.equals("left")){
            activateVideoBtnRight();
            activeBtn = "video";
            btnFlow = "middle";
        }
    }
    public void onSwipeLeft(){
        Log.i(TAG,"onSwipeLeft");
        if(btnFlow.equals("right")){
            Log.i(TAG,"right");
            activateVideoBtn();
            activeBtn = "video";
            btnFlow = "middle";
        }else if(btnFlow.equals("middle")){
            Log.i(TAG,"middle");
            activateGifBtn();
            activeBtn = "gif";
            btnFlow = "left";
        }
    }

    public void activateVideoBtn(){
        Log.i(TAG,"activateVideoBtn");
        ObjectAnimator pictureLeft = ObjectAnimator.ofFloat(pictureBtn, "translationX", 0, -resMoveLeft);
        ObjectAnimator pictureDeactiveX = ObjectAnimator.ofFloat(pictureBtn, "scaleX", resDeactivate);
        ObjectAnimator pictureDeactiveY = ObjectAnimator.ofFloat(pictureBtn, "scaleY", resDeactivate);
        ObjectAnimator videoLeft = ObjectAnimator.ofFloat(videoBtn, "translationX", 0, -resMoveLeft);
        ObjectAnimator videoActiveX = ObjectAnimator.ofFloat(videoBtn, "scaleX", resActivate);
        ObjectAnimator videoActiveY = ObjectAnimator.ofFloat(videoBtn, "scaleY", resActivate);
        ObjectAnimator gifLeft = ObjectAnimator.ofFloat(gifBtn, "translationX", -(resMoveLeft-deviceDiff));
        AnimatorSet anim = new AnimatorSet();
        anim.playTogether(pictureLeft, pictureDeactiveX, pictureDeactiveY, videoLeft, videoActiveX, videoActiveY, gifLeft);
        anim.setDuration(300);
        anim.start();
    }
    private void activateGifBtn(){
        pictureBtn.setVisibility(View.INVISIBLE);
        ObjectAnimator videoLeft = ObjectAnimator.ofFloat(videoBtn, "translationX", -resMoveLeft, -(resMoveLeft+resMoveLeft));
        ObjectAnimator videoActiveX = ObjectAnimator.ofFloat(videoBtn, "scaleX", 1);
        ObjectAnimator videoActiveY = ObjectAnimator.ofFloat(videoBtn, "scaleY", 1);
        ObjectAnimator gifLeft = ObjectAnimator.ofFloat(gifBtn, "translationX", -(resMoveLeft-deviceDiff), -(resMoveLeft+resMoveLeft-deviceDiff));
        ObjectAnimator gifActiveX = ObjectAnimator.ofFloat(gifBtn, "scaleX", resActivate);
        ObjectAnimator gifActiveY = ObjectAnimator.ofFloat(gifBtn, "scaleY", resActivate);
        AnimatorSet anim = new AnimatorSet();
        anim.playTogether(videoLeft, videoActiveX, videoActiveY, gifLeft, gifActiveX, gifActiveY);
        anim.setDuration(300);
        anim.start();
    }
    public void activateDirectGifBtn(){
        pictureBtn.setVisibility(View.INVISIBLE);
        ObjectAnimator pictureLeft = ObjectAnimator.ofFloat(pictureBtn, "translationX", 0, -resMoveLeft);
        ObjectAnimator pictureDeactiveX = ObjectAnimator.ofFloat(pictureBtn, "scaleX", resDeactivate);
        ObjectAnimator pictureDeactiveY = ObjectAnimator.ofFloat(pictureBtn, "scaleY", resDeactivate);
        ObjectAnimator videoLeft = ObjectAnimator.ofFloat(videoBtn, "translationX", 0, -(resMoveLeft+resMoveLeft));
        ObjectAnimator gifLeft = ObjectAnimator.ofFloat(gifBtn, "translationX", 0, -(resMoveLeft+resMoveLeft-deviceDiff));
        ObjectAnimator gifActiveX = ObjectAnimator.ofFloat(gifBtn, "scaleX", resActivate);
        ObjectAnimator gifActiveY = ObjectAnimator.ofFloat(gifBtn, "scaleY", resActivate);
        AnimatorSet anim = new AnimatorSet();
        anim.playTogether(pictureLeft, pictureDeactiveX, pictureDeactiveY, videoLeft, gifLeft, gifActiveX, gifActiveY);
        anim.setDuration(300);
        anim.start();
    }
    private void activateVideoBtnRight(){
        pictureBtn.setVisibility(View.VISIBLE);
        ObjectAnimator videoLeft = ObjectAnimator.ofFloat(videoBtn, "translationX", -(resMoveLeft+resMoveLeft), -resMoveLeft);
        ObjectAnimator videoActiveX = ObjectAnimator.ofFloat(videoBtn, "scaleX", resActivate);
        ObjectAnimator videoActiveY = ObjectAnimator.ofFloat(videoBtn, "scaleY", resActivate);
        ObjectAnimator gifLeft = ObjectAnimator.ofFloat(gifBtn, "translationX", -(resMoveLeft+resMoveLeft-deviceDiff), -(resMoveLeft-deviceDiff));
        ObjectAnimator gifActiveX = ObjectAnimator.ofFloat(gifBtn, "scaleX", 1);
        ObjectAnimator gifActiveY = ObjectAnimator.ofFloat(gifBtn, "scaleY", 1);
        AnimatorSet anim = new AnimatorSet();
        anim.playTogether(videoLeft, videoActiveX, videoActiveY, gifLeft, gifActiveX, gifActiveY);
        anim.setDuration(300);
        anim.start();
    }
    private void activatePictureBtn(){
        ObjectAnimator pictureLeft = ObjectAnimator.ofFloat(pictureBtn, "translationX", -resMoveLeft, 0);
        ObjectAnimator pictureDeactiveX = ObjectAnimator.ofFloat(pictureBtn, "scaleX", 1);
        ObjectAnimator pictureDeactiveY = ObjectAnimator.ofFloat(pictureBtn, "scaleY", 1);
        ObjectAnimator videoLeft = ObjectAnimator.ofFloat(videoBtn, "translationX", -resMoveLeft, 0);
        ObjectAnimator videoActiveX = ObjectAnimator.ofFloat(videoBtn, "scaleX", 1);
        ObjectAnimator videoActiveY = ObjectAnimator.ofFloat(videoBtn, "scaleY", 1);
        ObjectAnimator gifLeft = ObjectAnimator.ofFloat(gifBtn, "translationX", -(resMoveLeft-deviceDiff), 0);
        ObjectAnimator gifActiveX = ObjectAnimator.ofFloat(gifBtn, "scaleX", 1);
        ObjectAnimator gifActiveY = ObjectAnimator.ofFloat(gifBtn, "scaleY", 1);
        AnimatorSet anim = new AnimatorSet();
        anim.playTogether(pictureLeft, pictureDeactiveX, pictureDeactiveY, videoLeft, videoActiveX, videoActiveY, gifLeft, gifActiveX, gifActiveY);
        anim.setDuration(300);
        anim.start();
    }

}
