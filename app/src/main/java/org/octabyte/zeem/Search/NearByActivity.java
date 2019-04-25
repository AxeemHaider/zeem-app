package org.octabyte.zeem.Search;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.octabyte.zeem.API.SearchTask;
import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.Home.PublicActivity;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.BottomNavigationSetup;
import org.octabyte.zeem.Utils.GPSTracker;
import org.octabyte.zeem.Utils.RippleBackground;
import org.octabyte.zeem.Utils.Utils;

import java.util.List;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

public class NearByActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout[] nbLayout = new LinearLayout[10];
    private ImageView[] nbUserPic = new ImageView[10];
    private ImageView[] nbUserBadge = new ImageView[10];
    private TextView[] nbUserFullName = new TextView[10];

    private BottomNavigationView bottomNavigation;

    private RippleBackground rippleBackground;

    private long mUserId;
    private String mode;
    private String profilePic;
    private int userBadge;
    private View vNoInternetView, vNothingFoundView;
    private ImageView ivAppbarUser;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private SharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        pref = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        mUserId = pref.getLong("userId", 123L);
        profilePic = pref.getString("profilePic", null);
        userBadge = pref.getInt("badge",0);

        initVariable();

        if (profilePic != null) {
            Glide.with(this).load(profilePic)
                    .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                    .apply(RequestOptions.circleCropTransform()).into(ivAppbarUser);
        }

        mode = getIntent().getStringExtra("ARG_NAV_SENDER_MODE_TYPE");

        BottomNavigationSetup.init(bottomNavigation, NearByActivity.this, 1, mode);

        checkRuntimePermission();
    }

    private void initVariable(){

        ImageView ivAppbarPublic = findViewById(R.id.ivAppbarPublic);
        ImageView ivAppbarPrivate = findViewById(R.id.ivAppbarPrivate);
        ImageView ivAppbarExplorer = findViewById(R.id.ivAppbarExplorer);
        ivAppbarUser = findViewById(R.id.ivAppbarUser);

        ivAppbarPublic.setOnClickListener(this);
        ivAppbarPrivate.setOnClickListener(this);
        ivAppbarExplorer.setOnClickListener(this);
        ivAppbarUser.setOnClickListener(this);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        rippleBackground = findViewById(R.id.rippleBackground);

        ImageView nbAppUserPic = findViewById(R.id.nbAppUserPic);
        ImageView nbAppUserBadge = findViewById(R.id.nbAppUserBadge);

        nbLayout[0] = findViewById(R.id.nbLayout1);
        nbUserPic[0] = nbLayout[0].findViewById(R.id.nbUserPic);
        nbUserBadge[0] = nbLayout[0].findViewById(R.id.nbUserBadge);
        nbUserFullName[0] = nbLayout[0].findViewById(R.id.nbUserName);

        nbLayout[1] = findViewById(R.id.nbLayout2);
        nbUserPic[1] = nbLayout[1].findViewById(R.id.nbUserPic);
        nbUserBadge[1] = nbLayout[1].findViewById(R.id.nbUserBadge);
        nbUserFullName[1] = nbLayout[1].findViewById(R.id.nbUserName);

        nbLayout[2] = findViewById(R.id.nbLayout3);
        nbUserPic[2] = nbLayout[2].findViewById(R.id.nbUserPic);
        nbUserBadge[2] = nbLayout[2].findViewById(R.id.nbUserBadge);
        nbUserFullName[2] = nbLayout[2].findViewById(R.id.nbUserName);

        nbLayout[3] = findViewById(R.id.nbLayout4);
        nbUserPic[3] = nbLayout[3].findViewById(R.id.nbUserPic);
        nbUserBadge[3] = nbLayout[3].findViewById(R.id.nbUserBadge);
        nbUserFullName[3] = nbLayout[3].findViewById(R.id.nbUserName);

        nbLayout[4] = findViewById(R.id.nbLayout5);
        nbUserPic[4] = nbLayout[4].findViewById(R.id.nbUserPic);
        nbUserBadge[4] = nbLayout[4].findViewById(R.id.nbUserBadge);
        nbUserFullName[4] = nbLayout[4].findViewById(R.id.nbUserName);

        nbLayout[5] = findViewById(R.id.nbLayout6);
        nbUserPic[5] = nbLayout[5].findViewById(R.id.nbUserPic);
        nbUserBadge[5] = nbLayout[5].findViewById(R.id.nbUserBadge);
        nbUserFullName[5] = nbLayout[5].findViewById(R.id.nbUserName);

        nbLayout[6] = findViewById(R.id.nbLayout7);
        nbUserPic[6] = nbLayout[6].findViewById(R.id.nbUserPic);
        nbUserBadge[6] = nbLayout[6].findViewById(R.id.nbUserBadge);
        nbUserFullName[6] = nbLayout[6].findViewById(R.id.nbUserName);

        nbLayout[7] = findViewById(R.id.nbLayout8);
        nbUserPic[7] = nbLayout[7].findViewById(R.id.nbUserPic);
        nbUserBadge[7] = nbLayout[7].findViewById(R.id.nbUserBadge);
        nbUserFullName[7] = nbLayout[7].findViewById(R.id.nbUserName);

        nbLayout[8] = findViewById(R.id.nbLayout9);
        nbUserPic[8] = nbLayout[8].findViewById(R.id.nbUserPic);
        nbUserBadge[8] = nbLayout[8].findViewById(R.id.nbUserBadge);
        nbUserFullName[8] = nbLayout[8].findViewById(R.id.nbUserName);

        nbLayout[9] = findViewById(R.id.nbLayout10);
        nbUserPic[9] = nbLayout[9].findViewById(R.id.nbUserPic);
        nbUserBadge[9] = nbLayout[9].findViewById(R.id.nbUserBadge);
        nbUserFullName[9] = nbLayout[9].findViewById(R.id.nbUserName);

        // Init user values
        Glide.with(this).load(profilePic).apply(RequestOptions.circleCropTransform()).into(nbAppUserPic);
        nbAppUserBadge.setImageResource(Utils.getBadgePic(userBadge));

        // Hide all layouts
        for (int i=0; i < 10; i++){
            hideUserLayout(nbLayout[i]);
        }

    }

    private void setupNearByAdapter(){
        hideNoInternet();

        if (Utils.isNetworkAvailable(this)) {
            showLoading();

            // Set relation for nearBy friend
            String relation = "PUBLIC";
            if (mode.equals("PRIVATE"))
                relation = "FRIEND";
            else if (mode.equals("PUBLIC"))
                relation = "PUBLIC";

            SearchTask<List<User>> filterUser = new SearchTask<>(mUserId);
            filterUser.setRelation(relation);

            Double lat = 0.0;
            Double lng = 0.0;
            GPSTracker gps = new GPSTracker(this);
            if (gps.canGetLocation()){
                lat = gps.getLatitude();
                lng = gps.getLongitude();
            }else{
                showSettingForGPS();
                return;
            }
            filterUser.setLatitude(lat);
            filterUser.setLongitude(lng);

            filterUser.execute("nearBy");

            filterUser.setListener(new SearchTask.Response<List<User>>() {
                @Override
                public void response(List<User> response) {
                    hideLoading();

                    if (response != null){

                        if (response.size() > 0){

                            for (int i=0; i < response.size(); i++){

                                nbUserBadge[i].setImageResource(Utils.getBadgePic(response.get(i).getBadge()));
                                nbUserFullName[i].setText(Utils.capitalize(response.get(i).getFullName()));
                                final int finalI = i;
                                Glide.with(NearByActivity.this).load(Utils.bucketURL + response.get(i).getProfilePic())
                                        .apply(RequestOptions.circleCropTransform())
                                        .listener(new RequestListener<Drawable>() {
                                            @Override
                                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                return false;
                                            }

                                            @Override
                                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                animateUserLayout(nbLayout[finalI]);
                                                return false;
                                            }
                                        })
                                        .into(nbUserPic[i]);

                            } // end for loop

                        }else {
                            Log.w("APIDebuggung", "null response in NearByActivity -> setupNearByAdapter");
                            showNothingFound();
                        }

                    }else{
                        Log.w("APIDebuggung", "null response in NearByActivity -> setupNearByAdapter");
                        showNothingFound();
                    }
                }
            });

        }else {
            showNoInternet();
        }
    }

    private void showSettingForGPS(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_location_setting_title);
        builder.setMessage(R.string.dialog_location_setting);
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void checkRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[1])){
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_location_title);
                builder.setMessage(R.string.permission_location_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(NearByActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(NearByActivity.this, "Location permission is required", Toast.LENGTH_SHORT).show();
                        NearByActivity.super.onBackPressed();
                    }
                });
                builder.show();
            } else if (pref.getBoolean("location_permission",false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_required_title);
                builder.setMessage(R.string.permission_required_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(NearByActivity.this, "Location permission is required", Toast.LENGTH_SHORT).show();
                        NearByActivity.super.onBackPressed();
                    }
                });
                builder.show();
            }  else {
                //just request the permission
                ActivityCompat.requestPermissions(this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("location_permission",true);
            editor.apply();

        } else {
            //You already have the permission, just go ahead.
            setupNearByAdapter();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if(allgranted){

                setupNearByAdapter();

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[0])
                    ||ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[1])){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_location_title);
                builder.setMessage(R.string.permission_location_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(NearByActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(NearByActivity.this, "Location permission is required", Toast.LENGTH_SHORT).show();
                        NearByActivity.super.onBackPressed();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(),"Permission is required!",Toast.LENGTH_LONG).show();
                NearByActivity.this.finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) == PackageManager.PERMISSION_GRANTED) {

                //Got Permission
                setupNearByAdapter();

            }
        }
    }

    private void hideUserLayout(LinearLayout layout){
        layout.setScaleX(0);
        layout.setScaleY(0);
    }

    private void animateUserLayout(LinearLayout layout){
        layout.animate()
                .scaleX(1.f).scaleY(1.f)
                .setInterpolator(new OvershootInterpolator())
                .setDuration(400)
                .setStartDelay(200)
                .start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.getMenu().getItem(1).setChecked(true);
    }

    private void showLoading(){
        rippleBackground.startRippleAnimation();
    }
    private void hideLoading(){
        rippleBackground.stopRippleAnimation();
    }
    private void showNoInternet(){
        if (vNoInternetView == null) {
            vNoInternetView = ((ViewStub) findViewById(R.id.vsNearByNoInternet)).inflate();
            Button btnInternetRetry = findViewById(R.id.btnInternetRetry);
            btnInternetRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkRuntimePermission();
                }
            });
        }else{
            vNoInternetView.setVisibility(View.VISIBLE);
        }

        rippleBackground.setVisibility(View.GONE);
    }
    private void hideNoInternet(){
        rippleBackground.setVisibility(View.VISIBLE);

        if (vNoInternetView != null)
            vNoInternetView.setVisibility(View.GONE);
    }
    private void showNothingFound(){
        if (vNothingFoundView == null) {
            vNothingFoundView = ((ViewStub) findViewById(R.id.vsNearByNothingFound)).inflate();
            TextView title = findViewById(R.id.tvNothingFoundTitle);
            TextView message = findViewById(R.id.tvNothingFoundMessage);
            TextView messageLine2 = findViewById(R.id.tvNothingFoundMessageLine2);
            title.setText(R.string.nothing_found_title_in_nearby);
            message.setText(R.string.nothing_found_message_in_nearby);
            messageLine2.setText(R.string.nothing_found_message2_in_nearby);
        }else{
            vNothingFoundView.setVisibility(View.VISIBLE);
        }
        rippleBackground.setVisibility(View.GONE);
    }
    private void hideNothingFound(){
        if (vNothingFoundView != null)
            vNothingFoundView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivAppbarPublic:
                startActivity(new Intent(this, PublicActivity.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.ivAppbarPrivate:
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                break;
            case R.id.ivAppbarExplorer:
                final Intent intent = new Intent(this, DiscoverActivity.class);
                int[] startingLocation = new int[2];
                v.getLocationOnScreen(startingLocation);
                intent.putExtra(DiscoverActivity.ARG_DRAWING_START_LOCATION, startingLocation[1]);
                intent.putExtra("ARG_NAV_SENDER_MODE_TYPE", mode);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            case R.id.ivAppbarUser:
                int[] startingLocation2 = new int[2];
                v.getLocationOnScreen(startingLocation2);
                startingLocation2[0] += v.getWidth() / 2;
                ProfileActivity.startUserProfileFromLocation(startingLocation2, NearByActivity.this, String.valueOf(mUserId));
                overridePendingTransition(0, 0);
                break;
        }
    }
}
