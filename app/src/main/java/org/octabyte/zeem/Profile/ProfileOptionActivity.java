package org.octabyte.zeem.Profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Friend.FriendActivity;
import org.octabyte.zeem.List.ListActivity;
import org.octabyte.zeem.R;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 8/28/2017.
 */

public class ProfileOptionActivity extends AppCompatActivity{

    private static final String TAG = "DProfileOptionActivity";

    private Toolbar toolbar;
    private TextView setTvHowTag, setTvWhoTag;
    private Switch switchAnonymousTag;
    private Long mUserId;
    private SharedPreferences pref;
    private boolean howTagReady = false;
    private boolean whoTagReady = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_option);

        pref = getSharedPreferences(SHARED_PREFERENCE_FILE,MODE_PRIVATE);
        mUserId = pref.getLong("userId", 123L);

        initVariable();

    }
    private void initVariable(){
        //toolbar = (Toolbar) findViewById(R.id.toolbar);

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);

        toolbarTitle.setText(R.string.options);
        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Spinner spinnerHowTag = findViewById(R.id.spinnerHowTag);
        Spinner spinnerWhoTag = findViewById(R.id.spinnerWhoTag);
        setTvHowTag = findViewById(R.id.setTvHowTag);
        setTvWhoTag = findViewById(R.id.setTvWhoTag);
        switchAnonymousTag = findViewById(R.id.switchAnonymousTag);

        // Setup previous setting
        int howTagSetting = pref.getInt("settingHowTag", 0); // By default it's PUBLIC
        int whoTagSetting = pref.getInt("settingWhoTag", 0); // By default it's PUBLIC
        boolean anonymousTagSetting = pref.getBoolean("settingAnonymousTag", true);

        if(howTagSetting != 0)
            spinnerHowTag.setSelection(howTagSetting);

        if (whoTagSetting != 0)
            spinnerWhoTag.setSelection(whoTagSetting);

        if (!anonymousTagSetting)
            switchAnonymousTag.setChecked(anonymousTagSetting);

        // Setup Anonymous Tag switch
        setupAnonymousTag();

        spinnerHowTag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (howTagReady) {

                    // update preference file
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("settingHowTag", position);
                    editor.apply();

                    UserTask<Void> howTagShow = new UserTask<>(mUserId);
                    switch (position){
                        case 0:
                            setTvHowTag.setText(R.string.show_tag_public);
                            howTagShow.setShowTagPost("PUBLIC");
                            break;
                        case 1:
                            setTvHowTag.setText(R.string.show_tag_private);
                            howTagShow.setShowTagPost("PRIVATE");
                            break;
                        case 2:
                            setTvHowTag.setText(R.string.show_tag_never);
                            howTagShow.setShowTagPost("NEVER");
                            break;
                    }
                    howTagShow.execute("updateSetting");
                }else {
                    howTagReady = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerWhoTag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (whoTagReady) {

                    // update preference file
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt("settingWhoTag", position);
                    editor.apply();

                    UserTask<Void> whoCanTag = new UserTask<>(mUserId);
                    switch (position){
                        case 0:
                            setTvWhoTag.setText(R.string.who_tag_public);
                            whoCanTag.setPostTag("PUBLIC");
                            break;
                        case 1:
                            setTvWhoTag.setText(R.string.who_tag_friends);
                            whoCanTag.setPostTag("FRIEND");
                            break;
                        case 2:
                            setTvWhoTag.setText(R.string.who_tag_no_one);
                            whoCanTag.setPostTag("NO_ONE");
                            break;
                    }
                    whoCanTag.execute("updateSetting");

                } else {
                    whoTagReady = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupAnonymousTag(){
        switchAnonymousTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // update preference file
                SharedPreferences.Editor editor = pref.edit();

                UserTask<Void> anonymousTag = new UserTask<>(mUserId);
                if(switchAnonymousTag.isChecked()){
                    // If switch is on send data to server to ON this option
                    anonymousTag.setAnonymousTag(true);
                    editor.putBoolean("settingAnonymousTag", true);
                }else{
                    // If switch is on send data to server to OFF this option
                    anonymousTag.setAnonymousTag(false);
                    editor.putBoolean("settingAnonymousTag", false);
                }
                anonymousTag.execute("updateSetting");

                editor.apply();
            }
        });
    }

    public void settingInviteFriend(View view){
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "ZEEM");
            String strShareMessage = "\nHi, \nI'm using ZEEM, A new age of social media. Install this app and have fun!\n\n";
            strShareMessage = strShareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName();
            i.putExtra(Intent.EXTRA_TEXT, strShareMessage);
            i.setPackage("com.whatsapp");
            startActivity(Intent.createChooser(i, "Share via"));
        } catch(Exception e) {
            //e.toString();
            Toast.makeText(this, "Unable to open whatsapp", Toast.LENGTH_LONG).show();
        }
    }

    public void settingHowToUse(View view){

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.web_zeem)));
        startActivity(browserIntent);

    }

    public void settingSendFeedback(View view){

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.web_feedback)));
        startActivity(browserIntent);

    }

    public void settingTermsAndCondition(View view){

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.web_terms_and_condition)));
        startActivity(browserIntent);

    }

    public void settingContactUs(View view){

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.web_contact_us)));
        startActivity(browserIntent);

    }

    public void settingAppDetail(View view){

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }

    public void settingEditProfile(View view){
        startActivity(new Intent(this, EditProfileActivity.class));
    }
    public void settingCreateList(View view){
        startActivity(new Intent(this, ListActivity.class));
    }
    public void settingGetFriends(View view){
        Intent intent = new Intent(this, FriendActivity.class);
        intent.putExtra("REQUEST_MODE", "friends");
        startActivity(intent);
    }
    public void settingGetFollowers(View view){
        Intent intent = new Intent(this, FriendActivity.class);
        intent.putExtra("REQUEST_MODE", "followers");
        startActivity(intent);
    }
    public void settingGetFollowing(View view){
        Intent intent = new Intent(this, FriendActivity.class);
        intent.putExtra("REQUEST_MODE", "following");
        startActivity(intent);
    }
}
