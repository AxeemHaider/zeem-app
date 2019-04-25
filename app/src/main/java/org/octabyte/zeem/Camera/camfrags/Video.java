package org.octabyte.zeem.Camera.camfrags;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Camera.helper.SingleMediaScanner;
import org.octabyte.zeem.Utils.Utils;

import static org.octabyte.zeem.Camera.CameraActivity.TAG;
import static org.octabyte.zeem.Camera.CameraActivity.camCurrentMedia;
import static org.octabyte.zeem.Camera.CameraHandler.camMediaFile;

/**
 * Created by Azeem on 7/16/2017.
 */

public class Video extends Fragment{

    private Context context;
    private VideoView videoView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.frag_cam_video, container, false);
        context = mView.getContext();

        Boolean isStatusMode = getArguments().getBoolean("ARG_IS_STATUS_MODE");

        videoView = mView.findViewById(R.id.camVideoView);

        if (isStatusMode) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) videoView.getLayoutParams();
            layoutParams.height = Utils.getScreenWidth(context);
            videoView.setLayoutParams(layoutParams);
        }

        videoView.setVideoURI(Uri.parse(String.valueOf(camMediaFile)));
        MediaController mediaController = new MediaController(context);
        videoView.setMediaController(mediaController);
        videoView.start();

        try {
            if (camCurrentMedia.equals("GIF")) {
                mediaController.setVisibility(View.GONE);
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            //Crashlytics.logException(new RuntimeException("Fake Exception"));
            e.printStackTrace();
        }

        try {
            new SingleMediaScanner(context, camMediaFile);
            Toast.makeText(context, "Video Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "video fragment onResume");
        videoView.start();
    }
}
