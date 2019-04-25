package org.octabyte.zeem.Home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import org.octabyte.zeem.R;

/**
 * Created by Azeem on 8/1/2017.
 */

public class LoadingFeedItemView extends FrameLayout {

    private static final int VIEW_TYPE_LOADER_IMAGE = 5;
    private static final int VIEW_TYPE_LOADER_AUDIO = 6;
    private static final int VIEW_TYPE_LOADER_VIDEO = 7;
    private static final int VIEW_TYPE_LOADER_CARD = 8;

    private SendingProgressView vSendingProgress;
    private View vProgressBg;

    private OnLoadingFinishedListener onLoadingFinishedListener;

    public LoadingFeedItemView(Context context, int viewType) {
        super(context);
        init(viewType);
    }

    public LoadingFeedItemView(Context context, AttributeSet attrs, int viewType) {
        super(context, attrs);
        init(viewType);
    }

    public LoadingFeedItemView(Context context, AttributeSet attrs, int defStyleAttr, int viewType) {
        super(context, attrs, defStyleAttr);
        init(viewType);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LoadingFeedItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int viewType) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(viewType);
    }

    private void init(int viewType) {

        switch (viewType){
            case VIEW_TYPE_LOADER_IMAGE:
                LayoutInflater.from(getContext()).inflate(R.layout.view_feeditem_loader_image, this, true);
                break;
            case VIEW_TYPE_LOADER_AUDIO:
                LayoutInflater.from(getContext()).inflate(R.layout.view_feeditem_loader_audio, this, true);
                break;
            case VIEW_TYPE_LOADER_VIDEO:
                LayoutInflater.from(getContext()).inflate(R.layout.view_feeditem_loader_video, this, true);
                break;
            case VIEW_TYPE_LOADER_CARD:
                LayoutInflater.from(getContext()).inflate(R.layout.view_feeditem_loader_card, this, true);
                break;
        }

        initVariable();
    }
    private void initVariable(){
        vSendingProgress = findViewById(R.id.vSendingProgress);
        vProgressBg = findViewById(R.id.vProgressBg);
    }

    public void startLoading() {
        vSendingProgress.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                vSendingProgress.getViewTreeObserver().removeOnPreDrawListener(this);
                vSendingProgress.simulateProgress();
                return true;
            }
        });
        vSendingProgress.setOnLoadingFinishedListener(new SendingProgressView.OnLoadingFinishedListener() {
            @Override
            public void onLoadingFinished() {
                vSendingProgress.animate().scaleY(0).scaleX(0).setDuration(200).setStartDelay(100);
                vProgressBg.animate().alpha(0.f).setDuration(200).setStartDelay(100)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                vSendingProgress.setScaleX(1);
                                vSendingProgress.setScaleY(1);
                                vProgressBg.setAlpha(1);
                                if (onLoadingFinishedListener != null) {
                                    onLoadingFinishedListener.onLoadingFinished();
                                    onLoadingFinishedListener = null;
                                }
                            }
                        }).start();
            }
        });
    }

    public void setOnLoadingFinishedListener(OnLoadingFinishedListener onLoadingFinishedListener) {
        this.onLoadingFinishedListener = onLoadingFinishedListener;
    }

    public interface OnLoadingFinishedListener {
        void onLoadingFinished();
    }
}
