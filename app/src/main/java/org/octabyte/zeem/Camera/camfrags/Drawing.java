package org.octabyte.zeem.Camera.camfrags;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.octabyte.zeem.Camera.CameraDrawing;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Camera.camutil.FragmentCommunicator;
import org.octabyte.zeem.Camera.helper.CircularTextView;

/**
 * Created by Azeem on 6/23/2017.
 */

public class Drawing extends Fragment implements View.OnClickListener, FragmentCommunicator{

    private ImageView btnSimpleBrush, btnEraser, btnNeonEffect, btnRainbowPath;
    private CircularTextView currPaint;
    private float brushSize;

    private Context context;

    private RelativeLayout lyDrawingTop;
    private RelativeLayout lyDrawingBottom;

    private TextView btnCancelDrawing;
    private TextView btnDrawingDone;

    private CameraDrawing cameraDrawing;

    //private SeekBar sbBrushSize;
    private SeekBar sbBrushSize;

    private Boolean rainBowPath = false;

    private Boolean isStatusMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isStatusMode = getArguments().getBoolean("ARG_IS_STATUS_MODE");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_cam_drawing, container, false);
        context = view.getContext();

        lyDrawingTop = view.findViewById(R.id.lyDrawingTop);
        lyDrawingBottom = view.findViewById(R.id.lyDrawingBottom);

        btnCancelDrawing = view.findViewById(R.id.btnCancelDrawing);
            btnCancelDrawing.setOnClickListener(this);
        btnDrawingDone = view.findViewById(R.id.btnDrawingDone);
            btnDrawingDone.setOnClickListener(this);
        btnSimpleBrush = view.findViewById(R.id.btnSimpleBrush);
            btnSimpleBrush.setOnClickListener(this);
        btnEraser = view.findViewById(R.id.btnEraser);
            btnEraser.setOnClickListener(this);
        btnNeonEffect = view.findViewById(R.id.btnNeonEffect);
            btnNeonEffect.setOnClickListener(this);
        btnRainbowPath = view.findViewById(R.id.btnRainbowPath);
            btnRainbowPath.setOnClickListener(this);

        LinearLayout colorLayout = view.findViewById(R.id.colorLayout);
        currPaint = (CircularTextView) colorLayout.getChildAt(0);
        currPaint.setStrokeWidth(5);
        //currPaint.setImageDrawable(getResources().getDrawable(R.drawable.color_pressed));
        for(int i = 0; i < colorLayout.getChildCount(); i++){
                colorLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cameraDrawing.setColor(v.getTag().toString());
                        currPaint.setStrokeWidth(1);
                        currPaint = (CircularTextView) v;
                        currPaint.setStrokeWidth(5);
                    }
                });
        }

        sbBrushSize = view.findViewById(R.id.brushSize);

        sbBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brushSize = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(cameraDrawing != null)
                    cameraDrawing.setBrushSize(brushSize);
            }
        });

        return view;
    }

    @Override
    public void doAction(String action) {
        if(action.equals("hideLayout")){
            lyDrawingTop.setVisibility(View.INVISIBLE);
            lyDrawingBottom.setVisibility(View.INVISIBLE);
        }else if(action.equals("showLayout")){
            lyDrawingTop.setVisibility(View.VISIBLE);
            if(!rainBowPath)
                lyDrawingBottom.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cameraDrawing = getActivity().findViewById(R.id.cameraDrawing);
        cameraDrawing.startDrawing();
        cameraDrawing.bindCommunicator();
        if(cameraDrawing != null)
            sbBrushSize.setProgress((int) cameraDrawing.getBrushSize());
    }

    private void cancelDrawing(){
        if(cameraDrawing != null){
            AlertDialog.Builder newDialog = new AlertDialog.Builder(context);
            newDialog.setTitle("Discard Drawing ?");
            newDialog.setMessage("If you go back now, you will lose your drawing. To remove any specific thing use eraser.");
            newDialog.setPositiveButton("Remove", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    cameraDrawing.startNew();
                    cameraDrawing.stopDrawing();
                    gotoCameraEditing();
                }
            });
            newDialog.setNegativeButton("Keep", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        }else{
            Toast.makeText(context, "Wait! Setup Drawing", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawingDone(){
        cameraDrawing.stopDrawing();
        gotoCameraEditing();
    }

    private void simpleBrush(){
        btnSimpleBrush.setImageResource(R.drawable.simple_brush_on);
        btnEraser.setImageResource(R.drawable.erase_off);
        btnRainbowPath.setImageResource(R.drawable.glowing_off);
        btnNeonEffect.setImageResource(R.drawable.neon_off);

        rainBowPath = false;
        cameraDrawing.simplePath();
        lyDrawingBottom.setVisibility(View.VISIBLE);
    }
    private void eraser(){
        btnSimpleBrush.setImageResource(R.drawable.simple_brush_off);
        btnEraser.setImageResource(R.drawable.erase_on);
        btnRainbowPath.setImageResource(R.drawable.glowing_off);
        btnNeonEffect.setImageResource(R.drawable.neon_off);

        cameraDrawing.drawingEraser(true);
    }
    private void neonEffect(){
        btnSimpleBrush.setImageResource(R.drawable.simple_brush_off);
        btnEraser.setImageResource(R.drawable.erase_off);
        btnRainbowPath.setImageResource(R.drawable.glowing_off);
        btnNeonEffect.setImageResource(R.drawable.neon_on);

        rainBowPath = false;
        cameraDrawing.setNeonEffect(true);
        lyDrawingBottom.setVisibility(View.VISIBLE);
    }
    private void rainbowPath(){
        btnSimpleBrush.setImageResource(R.drawable.simple_brush_off);
        btnEraser.setImageResource(R.drawable.erase_off);
        btnRainbowPath.setImageResource(R.drawable.glowing_on);
        btnNeonEffect.setImageResource(R.drawable.neon_off);

        rainBowPath = true;
        cameraDrawing.setRainbowPath(true);
        lyDrawingBottom.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCancelDrawing:
                cancelDrawing();
                break;
            case R.id.btnDrawingDone:
                drawingDone();
                break;
            case R.id.btnSimpleBrush:
                simpleBrush();
                break;
            case R.id.btnEraser:
                eraser();
                break;
            case R.id.btnNeonEffect:
                neonEffect();
                break;
            case R.id.btnRainbowPath:
                rainbowPath();
                break;
        }
    }

    private void gotoCameraEditing(){
        Fragment cameraEditing = new Editing();
        Bundle args = new Bundle();
        args.putBoolean("ARG_IS_STATUS_MODE", isStatusMode);
        cameraEditing.setArguments(args);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.cameraLayout, cameraEditing,"cameraEditing");
        fragmentTransaction.commit();
    }
}
