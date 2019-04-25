package org.octabyte.zeem.Camera.camfrags;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Camera.camutil.FragmentCommunicator;
import org.octabyte.zeem.Camera.helper.CircularTextView;
import org.octabyte.zeem.Camera.helper.OnSwipeTouchListener;
import org.octabyte.zeem.Utils.Utils;

import static org.octabyte.zeem.Camera.CameraActivity.TAG;

/**
 * Created by Azeem on 6/22/2017.
 */

public class Writing extends Fragment implements View.OnClickListener, FragmentCommunicator {

    private Context context;

    private TextView writingText;
    private EditText writingEditText;
    private int _yDelta;
    private int canvasHeight;
    private int startingPoint = 0;

    private CircularTextView currPaint;

    private FragmentCommunicator fragmentCommunicator;

    private Boolean isStatusMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isStatusMode = getArguments().getBoolean("ARG_IS_STATUS_MODE");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_cam_writing, container, false);
        context = view.getContext();

        TextView btnWritingCancel = view.findViewById(R.id.btnWritingCancel);
        btnWritingCancel.setOnClickListener(this);
        TextView btnWritingDone = view.findViewById(R.id.btnWritingDone);
        btnWritingDone.setOnClickListener(this);

        LinearLayout colorLayout = view.findViewById(R.id.writingColorLayout);
        currPaint = (CircularTextView) colorLayout.getChildAt(0);
        currPaint.setStrokeWidth(5);

        for(int i = 0; i < colorLayout.getChildCount(); i++){
            colorLayout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int color = Color.parseColor(v.getTag().toString());
                    writingText.setBackgroundColor(color);
                    writingEditText.setBackgroundColor(color);
                    currPaint.setStrokeWidth(1);
                    currPaint = (CircularTextView) v;
                    currPaint.setStrokeWidth(5);
                }
            });
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        writingText = getActivity().findViewById(R.id.writingText);
        writingEditText = getActivity().findViewById(R.id.writingEditText);
        canvasHeight = getActivity().getWindow().getDecorView().getHeight() - 70;

        ImageView pictureView = getActivity().findViewById(R.id.pictureView);
        canvasHeight = pictureView.getHeight() + Utils.dpToPx(50);//  - 70

        RelativeLayout.LayoutParams pictureViewLayoutParams = (RelativeLayout.LayoutParams) pictureView.getLayoutParams();
        //startingPoint = pictureViewLayoutParams.topMargin;
        startingPoint = (int) pictureView.getY();

        if(startingPoint > 10){ // maybe greater than zero for safe 10 is used
            canvasHeight += 140;
        }

        Log.i("WritingDebug", String.valueOf(pictureView.getHeight() + 70));
        Log.i("WritingDebug", String.valueOf(getActivity().getWindow().getDecorView().getHeight() - 70));

        fragmentCommunicator = (FragmentCommunicator) getActivity();

        writeText();
    }

    private void writeText(){
        writingEditText.setVisibility(View.VISIBLE);
        writingEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(writingEditText, InputMethodManager.SHOW_IMPLICIT);
        writingText.setOnTouchListener(new OnSwipeTouchListener(context){
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                writingEditText.setVisibility(View.VISIBLE);
                writingEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(writingEditText, InputMethodManager.SHOW_IMPLICIT);
                Fragment cameraWriting = new Writing();
                FragmentManager fragmentManager = ((Activity) context).getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.cameraLayout, cameraWriting,"cameraWriting");
                fragmentTransaction.commit();
            }

            @Override
            public void onSwipeLeft() {
                v.setVisibility(View.GONE);
                writingEditText.setText("");
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                final int Y = (int) motionEvent.getRawY();
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        fragmentCommunicator.doAction("hideLayout");
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        _yDelta = Y - lParams.topMargin;
                        break;
                    case MotionEvent.ACTION_MOVE:

                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
                        if ((Y - _yDelta) > startingPoint && (Y - _yDelta) < canvasHeight) {
                            layoutParams.topMargin = Y - _yDelta;
                        }
                        v.setLayoutParams(layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        fragmentCommunicator.doAction("showLayout");
                        break;
                }
                return super.onTouch(v, motionEvent);
            }
        });
        writingEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Log.i(TAG,"Enter pressed");
                    writingDone();
                }
                return false;
            }
        });
    }

    private void writingCancel(){
        if(writingEditText != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(writingEditText.getWindowToken(), 0);
            
            writingEditText.setVisibility(View.GONE);
            gotoCameraEditing();
        }else {
            Toast.makeText(context, "Wait! Setup writing", Toast.LENGTH_SHORT).show();
        }
    }
    private void writingDone(){
        if(writingEditText != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(writingEditText.getWindowToken(), 0);

            String text = writingEditText.getText().toString();
            writingEditText.setVisibility(View.GONE);
            writingText.setText(text);
            writingText.setVisibility(View.VISIBLE);
            gotoCameraEditing();
        }else{
            Toast.makeText(context,"Wait! Setup writing", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnWritingCancel:
                writingCancel();
                break;
            case R.id.btnWritingDone:
                writingDone();
                break;
        }
    }


    @Override
    public void doAction(String action) {}

}
