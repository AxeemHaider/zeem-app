package org.octabyte.zeem.Camera.camfrags;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.octabyte.zeem.Camera.CameraDrawing;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.Utils;

import static org.octabyte.zeem.Camera.CameraActivity.isCameraEditingPanel;
import static org.octabyte.zeem.Camera.CameraActivity.isCameraActivity;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmp;


/**
 * Created by Azeem on 6/19/2017.
 */

public class Editing extends Fragment implements View.OnClickListener {

    private Context context;

    private ImageView btnCameraNext;

    private RelativeLayout lyInitialEditingTop;
    private RelativeLayout lyInitialEditingBottom;

    private Activity mActivity;

    private Boolean isStatusMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isStatusMode = getArguments().getBoolean("ARG_IS_STATUS_MODE");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_cam_editing, container, false);
        context = view.getContext();

        isCameraEditingPanel = true;
        isCameraActivity = false;

        lyInitialEditingTop = view.findViewById(R.id.lyInitialEditingTop);
        lyInitialEditingBottom = view.findViewById(R.id.lyInitialEditingBottom);

        /*btnCameraNext = (ImageView) view.findViewById(R.id.btnCameraDone);
            btnCameraNext.setOnClickListener(this);*/
        ImageView btnBackToCamera = view.findViewById(R.id.btnBackToCamera);
        btnBackToCamera.setOnClickListener(this);

        ImageView btnWriteText = view.findViewById(R.id.btnWriteText);
            btnWriteText.setOnClickListener(this);

        ImageView btnDrawLine = view.findViewById(R.id.btnDrawLine);
            btnDrawLine.setOnClickListener(this);

        ImageView btnCameraEffect = view.findViewById(R.id.btnCameraEffects);
            btnCameraEffect.setOnClickListener(this);

        return view;
    }

   /* @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageView pictureView = (ImageView) getActivity().findViewById(R.id.pictureView);
        CameraDrawing cameraDrawing = (CameraDrawing) getActivity().findViewById(R.id.cameraDrawing);
        Bitmap pictureBitmap = ((BitmapDrawable)pictureView.getDrawable()).getBitmap();
        cameraDrawing.setLayoutParams(new RelativeLayout.LayoutParams(pictureBitmap.getWidth(), pictureBitmap.getHeight()));
    }*/

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = getActivity();
    }

    private void camEffect(){
        Fragment cameraEffect = new Effect();
        Bundle args = new Bundle();
        args.putBoolean("ARG_IS_STATUS_MODE", isStatusMode);
        cameraEffect.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.cameraLayout, cameraEffect,"cameraEffect");
        fragmentTransaction.commit();
        isCameraEditingPanel = false;
    }

    private void writeText(){
        Fragment cameraWriting = new Writing();
        Bundle args = new Bundle();
        args.putBoolean("ARG_IS_STATUS_MODE", isStatusMode);
        cameraWriting.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.cameraLayout, cameraWriting,"cameraWriting");
        fragmentTransaction.commit();
        isCameraEditingPanel = false;
    }
    private void cameraDone(){}

    private void backToCamera(){
        AlertDialog.Builder newDialog = new AlertDialog.Builder(context);
        newDialog.setTitle("Discard Photo ?");
        newDialog.setMessage("If you go back now, you will lose your photo and drawing.");
        newDialog.setPositiveButton("Discard", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                isCameraEditingPanel = false;
                isCameraActivity = true;
                originalBmp = null;

                mActivity.finish();
                startActivity(mActivity.getIntent());
            }
        });
        newDialog.setNegativeButton("Keep", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                dialog.cancel();
            }
        });
        newDialog.show();
    }

    private void camDrawing(){

        if (isStatusMode) {
            final ImageView pictureView = getActivity().findViewById(R.id.pictureView);
            final CameraDrawing cameraDrawingView = getActivity().findViewById(R.id.cameraDrawing);
            cameraDrawingView.post(new Runnable() {
                @Override
                public void run() {
                    RelativeLayout.LayoutParams mParams;
                    mParams = (RelativeLayout.LayoutParams) cameraDrawingView.getLayoutParams();
                    mParams.setMargins(0, (int) pictureView.getY(), 0, 0);
                    mParams.height = cameraDrawingView.getWidth();
                    cameraDrawingView.setLayoutParams(mParams);
                    cameraDrawingView.postInvalidate();
                }
            });
        }

        Fragment cameraDrawing = new Drawing();
        Bundle args = new Bundle();
        args.putBoolean("ARG_IS_STATUS_MODE", isStatusMode);
        cameraDrawing.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.cameraLayout, cameraDrawing,"cameraDrawing");
        fragmentTransaction.commit();
        isCameraEditingPanel = false;
    }

    public void hideLayout(){
        lyInitialEditingTop.setVisibility(View.INVISIBLE);
        lyInitialEditingBottom.setVisibility(View.INVISIBLE);
    }
    public void showLayout(){
        lyInitialEditingTop.setVisibility(View.VISIBLE);
        lyInitialEditingBottom.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnWriteText:
                writeText();
                break;
            case R.id.btnCameraDone:
                cameraDone();
                break;
            case R.id.btnBackToCamera:
                backToCamera();
                break;
            case R.id.btnDrawLine:
                camDrawing();
                break;
            case R.id.btnCameraEffects:
                camEffect();
                break;
        }
    }
}
