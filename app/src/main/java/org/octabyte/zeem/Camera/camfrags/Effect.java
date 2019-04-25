package org.octabyte.zeem.Camera.camfrags;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.octabyte.zeem.R;
import org.octabyte.zeem.Camera.camutil.CameraFilters;
import org.octabyte.zeem.Camera.camutil.ThumbnailCallback;
import org.octabyte.zeem.Camera.camutil.ThumbnailItem;
import org.octabyte.zeem.Camera.camutil.ThumbnailsAdapter;
import org.octabyte.zeem.Camera.camutil.ThumbnailsManager;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.imageprocessors.subfilters.BrightnessSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.ContrastSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.SaturationSubfilter;
import com.zomato.photofilters.imageprocessors.subfilters.VignetteSubfilter;

import java.util.List;

import static org.octabyte.zeem.Camera.CameraActivity.TAG;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmp;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmpHeight;
import static org.octabyte.zeem.Camera.camutil.CameraUtils.originalBmpWidth;

/**
 * Created by Azeem on 6/23/2017.
 */

public class Effect extends Fragment implements ThumbnailCallback, View.OnClickListener {
    static {
        System.loadLibrary("NativeImageProcessor");
    }

    private Context context;

    private Activity activity;
    private RecyclerView thumbListView;
    private ImageView placeHolderImageView;
    private ImageView btnEffectPanel;
    private ImageView btnEffectCustomization;
    private SeekBar barBrightness, barContrast, barSaturation, barVignette;
    private int brightnessValue;
    private int contrastValue;
    private int saturationValue;
    private int vignetteValue;

    private View mView;

    private RelativeLayout lyCustomEffect;
    private RelativeLayout lyEffectPanel;
    private LinearLayout lyEffectCustomization;
    /*private int lastPosition = 0;
    private final int thumbColor = Color.parseColor("#50000000");;
    private final int thumbColorActive = Color.parseColor("#aaffcc");*/
    private Bitmap photoBmp;
    /*private Bitmap originalBmp;
    private int originalBmpWidth, originalBmpHeight;*/

    private TextView btnCancelCustomEffect;
    private TextView btnDoneCustomEffect;
    private TextView tvEffectInfo;

    private Boolean isStatusMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isStatusMode = getArguments().getBoolean("ARG_IS_STATUS_MODE");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.frag_cam_effect, container, false);
        context = mView.getContext();

        lyCustomEffect = mView.findViewById(R.id.lyCustomEffect);
        lyEffectPanel = mView.findViewById(R.id.lyEffectPanel);
        lyEffectCustomization = mView.findViewById(R.id.lyEffectCustomization);

        tvEffectInfo = mView.findViewById(R.id.tvEffectInfo);
        TextView btnCancelEffect = mView.findViewById(R.id.btnCancelEffect);
        btnCancelEffect.setOnClickListener(this);
        TextView btnEffectDone = mView.findViewById(R.id.btnEffectDone);
        btnEffectDone.setOnClickListener(this);
        btnCancelCustomEffect = mView.findViewById(R.id.btnCancelCustomEffect);
        btnCancelCustomEffect.setOnClickListener(this);
        btnDoneCustomEffect = mView.findViewById(R.id.btnDoneCustomEffect);
        btnDoneCustomEffect.setOnClickListener(this);
        ImageView btnEffectBrightness = mView.findViewById(R.id.btnEffectBrightness);
        btnEffectBrightness.setOnClickListener(this);
        ImageView btnEffectContrast = mView.findViewById(R.id.btnEffectContrast);
        btnEffectContrast.setOnClickListener(this);
        ImageView btnEffectSaturation = mView.findViewById(R.id.btnEffectSaturation);
        btnEffectSaturation.setOnClickListener(this);
        ImageView btnEffectVignette = mView.findViewById(R.id.btnEffectVignette);
        btnEffectVignette.setOnClickListener(this);
        btnEffectPanel = mView.findViewById(R.id.btnEffectPanel);
        btnEffectPanel.setOnClickListener(this);
        btnEffectCustomization = mView.findViewById(R.id.btnEffectCustomization);
        btnEffectCustomization.setOnClickListener(this);

        barBrightness = mView.findViewById(R.id.barBrightness);
        barBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Filter filter = new Filter();
                filter.addSubFilter(new BrightnessSubfilter(brightnessValue - 50));
                placeHolderImageView.setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(photoBmp, originalBmpWidth, originalBmpHeight, false)));
            }
        });
        barContrast = mView.findViewById(R.id.barContrast);
        barContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                contrastValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Filter filter = new Filter();
                filter.addSubFilter(new ContrastSubfilter(contrastValue));
                placeHolderImageView.setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(photoBmp, originalBmpWidth, originalBmpHeight, false)));
            }
        });
        barSaturation = mView.findViewById(R.id.barSaturation);
        barSaturation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                saturationValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Filter filter = new Filter();
                filter.addSubFilter(new SaturationSubfilter(saturationValue));
                placeHolderImageView.setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(photoBmp, originalBmpWidth, originalBmpHeight, false)));
            }
        });
        barVignette = mView.findViewById(R.id.barVignette);
        barVignette.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vignetteValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Filter filter = new Filter();
                filter.addSubFilter(new VignetteSubfilter(context, vignetteValue));
                placeHolderImageView.setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(photoBmp, originalBmpWidth, originalBmpHeight, false)));
            }
        });

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        initUIWidgets();
    }


    private void initUIWidgets() {
        thumbListView = mView.findViewById(R.id.filterThumbnails);
        placeHolderImageView = activity.findViewById(R.id.pictureView);

        /*final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        placeHolderImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.orignal, options));
        placeHolderImageView.setVisibility(View.VISIBLE);

        originalBmp = ((BitmapDrawable)placeHolderImageView.getDrawable()).getBitmap();
        originalBmpWidth = originalBmp.getWidth();
        originalBmpHeight = originalBmp.getHeight();*/

        initHorizontalList();
    }

    private void initHorizontalList() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        layoutManager.scrollToPosition(0);
        thumbListView.setLayoutManager(layoutManager);
        thumbListView.setHasFixedSize(true);
        bindDataToAdapter();
    }

    private void bindDataToAdapter() {
        final Context context = activity.getApplication();
        Handler handler = new Handler();
        Runnable r = new Runnable() {
            public void run() {

                Log.i(TAG, "width: "+ originalBmpWidth + " hright: "+ originalBmpHeight);

                ThumbnailItem t1 = new ThumbnailItem();
                ThumbnailItem t2 = new ThumbnailItem();
                ThumbnailItem t3 = new ThumbnailItem();
                ThumbnailItem t4 = new ThumbnailItem();
                ThumbnailItem t5 = new ThumbnailItem();
                ThumbnailItem t6 = new ThumbnailItem();
                ThumbnailItem t7 = new ThumbnailItem();
                ThumbnailItem t8 = new ThumbnailItem();
                ThumbnailItem t9 = new ThumbnailItem();
                ThumbnailItem t10 = new ThumbnailItem();
                ThumbnailItem t11 = new ThumbnailItem();
                ThumbnailItem t12 = new ThumbnailItem();
                ThumbnailItem t13 = new ThumbnailItem();
                ThumbnailItem t14 = new ThumbnailItem();
                ThumbnailItem t15 = new ThumbnailItem();
                ThumbnailItem t16 = new ThumbnailItem();
                ThumbnailItem t17 = new ThumbnailItem();
                ThumbnailItem t18 = new ThumbnailItem();
                ThumbnailItem t19 = new ThumbnailItem();
                ThumbnailItem t20 = new ThumbnailItem();
                ThumbnailItem t21 = new ThumbnailItem();
                ThumbnailItem t22 = new ThumbnailItem();
                ThumbnailItem t23 = new ThumbnailItem();
                ThumbnailItem t24 = new ThumbnailItem();
                ThumbnailItem t25 = new ThumbnailItem();
                ThumbnailItem t26 = new ThumbnailItem();
                ThumbnailItem t27 = new ThumbnailItem();
                ThumbnailItem t28 = new ThumbnailItem();
                ThumbnailItem t29 = new ThumbnailItem();
                ThumbnailItem t30 = new ThumbnailItem();
                ThumbnailItem t31 = new ThumbnailItem();


                t1.image = originalBmp;
                t2.image = originalBmp;
                t3.image = originalBmp;
                t4.image = originalBmp;
                t5.image = originalBmp;
                t6.image = originalBmp;
                t7.image = originalBmp;
                t8.image = originalBmp;
                t9.image = originalBmp;
                t10.image = originalBmp;
                t11.image = originalBmp;
                t12.image = originalBmp;
                t13.image = originalBmp;
                t14.image = originalBmp;
                t15.image = originalBmp;
                t16.image = originalBmp;
                t17.image = originalBmp;
                t18.image = originalBmp;
                t19.image = originalBmp;
                t20.image = originalBmp;
                t21.image = originalBmp;
                t22.image = originalBmp;
                t23.image = originalBmp;
                t24.image = originalBmp;
                t25.image = originalBmp;
                t26.image = originalBmp;
                t27.image = originalBmp;
                t28.image = originalBmp;
                t29.image = originalBmp;
                t30.image = originalBmp;
                t31.image = originalBmp;

                ThumbnailsManager.clearThumbs();
                t1.filterName = "Original";
                ThumbnailsManager.addThumb(t1); // Original Image

                t7.filter = CameraFilters.Favourite();
                t7.filterName = "Favourite";
                ThumbnailsManager.addThumb(t7);

                t18.filter = CameraFilters.Nice();
                t18.filterName = "Nice";
                ThumbnailsManager.addThumb(t18);

                t10.filter = CameraFilters.Fair();
                t10.filterName = "Fair";
                ThumbnailsManager.addThumb(t10);

                t4.filter = CameraFilters.YLight();
                t4.filterName = "YLight";
                ThumbnailsManager.addThumb(t4);

                t5.filter = CameraFilters.Morning();
                t5.filterName = "Morning";
                ThumbnailsManager.addThumb(t5);

                t13.filter = CameraFilters.Waldan();
                t13.filterName = "Waldan";
                ThumbnailsManager.addThumb(t13);

                t12.filter = CameraFilters.RiseUp();
                t12.filterName = "RiseUp";
                ThumbnailsManager.addThumb(t12);

                t20.filter = CameraFilters.Blue();
                t20.filterName = "Blue";
                ThumbnailsManager.addThumb(t20);

                t6.filter = CameraFilters.Inside();
                t6.filterName = "Inside";
                ThumbnailsManager.addThumb(t6);

                t8.filter = CameraFilters.Sun();
                t8.filterName = "Sun";
                ThumbnailsManager.addThumb(t8);

                t14.filter = CameraFilters.Nature();
                t14.filterName = "Nature";
                ThumbnailsManager.addThumb(t14);

                t16.filter = CameraFilters.Wow();
                t16.filterName = "Wow";
                ThumbnailsManager.addThumb(t16);

                t2.filter = CameraFilters.Welcome();
                t2.filterName = "Welcome";
                ThumbnailsManager.addThumb(t2);

                t25.filter = CameraFilters.B_W();
                t25.filterName = "B & W";
                ThumbnailsManager.addThumb(t25);

                t9.filter = CameraFilters.Low();
                t9.filterName = "Low";
                ThumbnailsManager.addThumb(t9);

                t22.filter = CameraFilters.Bright();
                t22.filterName = "Bright";
                ThumbnailsManager.addThumb(t22);

                t21.filter = CameraFilters.New(context);
                t21.filterName = "New";
                ThumbnailsManager.addThumb(t21);

                t23.filter = CameraFilters.Focus(context);
                t23.filterName = "Focus";
                ThumbnailsManager.addThumb(t23);

                t26.filter = CameraFilters.Round(context);
                t26.filterName = "Round";
                ThumbnailsManager.addThumb(t26);

                t3.filter = CameraFilters.Old();
                t3.filterName = "Old";
                ThumbnailsManager.addThumb(t3);

                t11.filter = CameraFilters.GreenOne();
                t11.filterName = "GreenOne";
                ThumbnailsManager.addThumb(t11);

                t15.filter = CameraFilters.Red();
                t15.filterName = "Red";
                ThumbnailsManager.addThumb(t15);

                t17.filter = CameraFilters.Jaman();
                t17.filterName = "Jaman";
                ThumbnailsManager.addThumb(t17);

                t19.filter = CameraFilters.Pro();
                t19.filterName = "Pro";
                ThumbnailsManager.addThumb(t19);

                t24.filter = CameraFilters.ColorDown();
                t24.filterName = "ColorDown";
                ThumbnailsManager.addThumb(t24);

                t27.filter = CameraFilters.getStarLitFilter();
                t27.filterName = "StarLite";
                ThumbnailsManager.addThumb(t27);

                t28.filter = CameraFilters.getAweStruckVibeFilter();
                t28.filterName = "Struck";
                ThumbnailsManager.addThumb(t28);

                t29.filter = CameraFilters.getBlueMessFilter();
                t29.filterName = "BlueMess";
                ThumbnailsManager.addThumb(t29);

                t30.filter = CameraFilters.getLimeStutterFilter();
                t30.filterName = "lime";
                ThumbnailsManager.addThumb(t30);

                t31.filter = CameraFilters.getNightWhisperFilter();
                t31.filterName = "Night";
                ThumbnailsManager.addThumb(t31);

                List<ThumbnailItem> thumbs = ThumbnailsManager.processThumbs(context);

                ThumbnailsAdapter adapter = new ThumbnailsAdapter(thumbs, Effect.this);
                thumbListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        };
        handler.post(r);
    }

    @Override
    public void onThumbnailClick(Filter filter) {
        placeHolderImageView.setImageBitmap(filter.processFilter(Bitmap.createScaledBitmap(originalBmp, originalBmpWidth, originalBmpHeight, false)));
    }

    private void cancelEffect(){
        placeHolderImageView.setImageBitmap(originalBmp);
        gotoCameraEditing();
    }
    private void effectDone(){
        gotoCameraEditing();
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

    private void cancelCustomEffect(){
        Log.i(TAG, "cancelCustomEffect");
        tvEffectInfo.setText("");
        lyEffectCustomization.setVisibility(View.VISIBLE);
        lyCustomEffect.setVisibility(View.GONE);
        btnCancelCustomEffect.setVisibility(View.GONE);
        btnDoneCustomEffect.setVisibility(View.GONE);
        barBrightness.setVisibility(View.GONE);
        barContrast.setVisibility(View.GONE);
        barSaturation.setVisibility(View.GONE);
        barVignette.setVisibility(View.GONE);
        placeHolderImageView.setImageBitmap(Bitmap.createScaledBitmap(photoBmp, originalBmpWidth, originalBmpHeight, false));
    }

    private void doneCustomEffect(){
        Log.i(TAG, "doneCustomEffect");
        tvEffectInfo.setText("");
        lyEffectCustomization.setVisibility(View.VISIBLE);
        lyCustomEffect.setVisibility(View.GONE);
        btnCancelCustomEffect.setVisibility(View.GONE);
        btnDoneCustomEffect.setVisibility(View.GONE);
        barBrightness.setVisibility(View.GONE);
        barContrast.setVisibility(View.GONE);
        barSaturation.setVisibility(View.GONE);
        barVignette.setVisibility(View.GONE);
    }

    private void effectPanel(){
        Log.i(TAG, "effectPanel");
        btnEffectCustomization.setImageResource(R.drawable.custom_effect_off);
        btnEffectPanel.setImageResource(R.drawable.effect_icon_on);
        tvEffectInfo.setText("");
        lyCustomEffect.setVisibility(View.GONE);
        lyEffectCustomization.setVisibility(View.GONE);
        btnCancelCustomEffect.setVisibility(View.GONE);
        btnDoneCustomEffect.setVisibility(View.GONE);
        lyEffectPanel.setVisibility(View.VISIBLE);
        barBrightness.setVisibility(View.GONE);
        barContrast.setVisibility(View.GONE);
        barSaturation.setVisibility(View.GONE);
        barVignette.setVisibility(View.GONE);
    }
    private void effectCustomization(){
        Log.i(TAG, "effectCustomization");
        btnEffectCustomization.setImageResource(R.drawable.custom_effect_on);
        btnEffectPanel.setImageResource(R.drawable.effect_icon_off);
        lyEffectCustomization.setVisibility(View.VISIBLE);
        lyCustomEffect.setVisibility(View.GONE);
        lyEffectPanel.setVisibility(View.GONE);
        //cancelCustomEffect();
    }
    private void effectBrightness(){
        Log.i(TAG, "effectBrightness");
        tvEffectInfo.setText("Brightness");
        lyCustomEffect.setVisibility(View.VISIBLE);
        btnCancelCustomEffect.setVisibility(View.VISIBLE);
        btnDoneCustomEffect.setVisibility(View.VISIBLE);
        lyEffectCustomization.setVisibility(View.GONE);
        lyEffectPanel.setVisibility(View.GONE);
        barBrightness.setVisibility(View.VISIBLE);
        barContrast.setVisibility(View.GONE);
        barSaturation.setVisibility(View.GONE);
        barVignette.setVisibility(View.GONE);

        photoBmp = ((BitmapDrawable)placeHolderImageView.getDrawable()).getBitmap();
    }
    private void effectContrast(){
        Log.i(TAG, "effectContrast");
        tvEffectInfo.setText("Contrast");
        lyCustomEffect.setVisibility(View.VISIBLE);
        btnCancelCustomEffect.setVisibility(View.VISIBLE);
        btnDoneCustomEffect.setVisibility(View.VISIBLE);
        lyEffectCustomization.setVisibility(View.GONE);
        lyEffectPanel.setVisibility(View.GONE);
        barBrightness.setVisibility(View.GONE);
        barContrast.setVisibility(View.VISIBLE);
        barSaturation.setVisibility(View.GONE);
        barVignette.setVisibility(View.GONE);

        photoBmp = ((BitmapDrawable)placeHolderImageView.getDrawable()).getBitmap();
    }
    private void effectSaturation(){
        Log.i(TAG, "effectSaturation");
        tvEffectInfo.setText("Saturation");
        lyCustomEffect.setVisibility(View.VISIBLE);
        btnCancelCustomEffect.setVisibility(View.VISIBLE);
        btnDoneCustomEffect.setVisibility(View.VISIBLE);
        lyEffectCustomization.setVisibility(View.GONE);
        lyEffectPanel.setVisibility(View.GONE);
        barBrightness.setVisibility(View.GONE);
        barContrast.setVisibility(View.GONE);
        barSaturation.setVisibility(View.VISIBLE);
        barVignette.setVisibility(View.GONE);

    }
    private void effectVignette(){
        Log.i(TAG, "effectVignette");
        tvEffectInfo.setText("Vignette");
        lyCustomEffect.setVisibility(View.VISIBLE);
        btnCancelCustomEffect.setVisibility(View.VISIBLE);
        btnDoneCustomEffect.setVisibility(View.VISIBLE);
        lyEffectCustomization.setVisibility(View.GONE);
        lyEffectPanel.setVisibility(View.GONE);
        barBrightness.setVisibility(View.GONE);
        barContrast.setVisibility(View.GONE);
        barSaturation.setVisibility(View.GONE);
        barVignette.setVisibility(View.VISIBLE);

        photoBmp = ((BitmapDrawable)placeHolderImageView.getDrawable()).getBitmap();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCancelEffect:
                cancelEffect();
                break;
            case R.id.btnEffectDone:
                effectDone();
                break;
            case R.id.btnCancelCustomEffect:
                cancelCustomEffect();
                break;
            case R.id.btnDoneCustomEffect:
                doneCustomEffect();
                break;
            case R.id.btnEffectPanel:
                effectPanel();
                break;
            case R.id.btnEffectCustomization:
                effectCustomization();
                break;
            case R.id.btnEffectBrightness:
                effectBrightness();
                break;
            case R.id.btnEffectContrast:
                effectContrast();
                break;
            case R.id.btnEffectSaturation:
                effectSaturation();
                break;
            case R.id.btnEffectVignette:
                effectVignette();
                break;
        }
    }

}
