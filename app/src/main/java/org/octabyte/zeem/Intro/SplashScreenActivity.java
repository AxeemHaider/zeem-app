package org.octabyte.zeem.Intro;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONObject;
import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.InstantChat.InstantChatActivity;
import org.octabyte.zeem.Intro.WelcomeActivity;
import org.octabyte.zeem.Login.CreateProfileActivity;
import org.octabyte.zeem.Notification.NotificationActivity;
import org.octabyte.zeem.Profile.ProfileActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.SinglePost.SinglePostActivity;
import org.octabyte.zeem.Utils.ScalingUtilities;
import org.octabyte.zeem.Utils.Utils;

import java.util.Map;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;
import static org.octabyte.zeem.Profile.ProfileActivity.ARG_REVEAL_START_LOCATION;
import static org.octabyte.zeem.Profile.ProfileActivity.USER_PROFILE_ID;

public class SplashScreenActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private String[] permissionsRequired = new String[]{
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);

        boolean isActive = sharedPreferences.getBoolean("isActiveUser", true);
        boolean isLogin = sharedPreferences.getBoolean("isLogin", false);

        if (isActive) {
            if (isLogin) {

                checkRuntimePermission();

            }else{
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(this, BlockUserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }


    }

    private void initializeApplication(){
        boolean isNotification = false;

        try {
            Bundle bundle = getIntent().getExtras();

            if (bundle != null &&
                    (bundle.getString("notificationType") != null || bundle.getString("senderToken") != null)
                    ) {

                //here can get notification message
                String notificationType = "UPDATE_APP";
                String profilePic = "no picture";
                String userId = "123";
                String mode = "PRIVATE";
                String postSafeKey = "not available";
                Boolean isAnonymous = false;

                String senderToken = "Not Found";
                String userFullName = "Not Found";
                String chatAnonymous = "chat_anonymous";
                Long messageId = null;

                try {
                    notificationType = bundle.getString("notificationType") != null ? bundle.getString("notificationType") : "UPDATE_APP";
                    profilePic = bundle.getString("profilePic") != null ? bundle.getString("profilePic") : "no picture";
                    userId = bundle.getString("userId") != null ? bundle.getString("userId") : "123";
                    mode = bundle.getString("mode") != null ? bundle.getString("mode") : "PRIVATE";
                    postSafeKey = bundle.getString("postSafeKey") != null ? bundle.getString("postSafeKey") : "not available";
                    isAnonymous = bundle.getString("anonymous") != null && Boolean.parseBoolean(bundle.getString("anonymous"));

                    senderToken = bundle.getString("senderToken") != null ? bundle.getString("senderToken") : "Not Found";
                    userFullName = bundle.getString("userFullName") != null ? bundle.getString("userFullName") : "Not Found";
                    chatAnonymous = bundle.getString("chatAnonymous") != null ? bundle.getString("chatAnonymous") : "chat_anonymous";

                    messageId = bundle.getString("messageId") != null ? Long.parseLong(bundle.getString("messageId")) : 0L;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Activity intent
                Intent intent = null;

                switch (notificationType){

                    case "INSTANT_CHAT":

                        if (isAnonymous){
                            profilePic = "anonymous";
                            userFullName = "Anonymous Person";
                        }

                        intent = new Intent(this, InstantChatActivity.class);
                        intent.putExtra("ARG_INSTANT_CHAT_SENDER_TOKEN", senderToken);
                        intent.putExtra("ARG_INSTANT_CHAT_MESSAGE_ID", messageId);
                        intent.putExtra("ARG_INSTANT_CHAT_MESSAGE_TEXT", "Instant Chat!");
                        intent.putExtra("ARG_INSTANT_CHAT_USER_PIC", profilePic);
                        intent.putExtra("ARG_INSTANT_CHAT_USER_NAME", userFullName);
                        intent.putExtra("ARG_INSTANT_CHAT_ANONYMOUS", chatAnonymous);
                        break;

                    case "POST_LIKE":
                    case "POST_COMMENT":
                    case "TAG_POST":
                    case "LIST_POST":
                    case "POST_MENTION":
                    case "COMMENT_MENTION":
                    case "REPORT_SPAM":
                    case "REPORT_ANTI_RELIGION":
                    case "REPORT_SEXUAL_CONTENT":
                    case "REPORT_OTHER":

                        intent = new Intent(this, SinglePostActivity.class);
                        intent.putExtra("ARG_SINGLE_POST_SAFE_KEY", postSafeKey);
                        intent.putExtra("ARG_SINGLE_POST_POST_MODE", mode);
                        break;

                    case "FOLLOWER":
                    case "NEW_FRIEND":
                    case "COMMENT_LIKE":

                        intent = new Intent(this, ProfileActivity.class);
                        int[] location = new int[2];
                        location[0] = 0;
                        location[1] = 0;
                        intent.putExtra(ARG_REVEAL_START_LOCATION, location);
                        intent.putExtra(USER_PROFILE_ID, userId);
                        break;

                    case "FRIEND_REQUEST":

                        intent = new Intent(this, NotificationActivity.class);
                        intent.putExtra("ARG_NAV_SENDER_MODE_TYPE", mode);
                        intent.putExtra("ARG_NOTIFICATION_FOR_REQUEST", true);
                        break;

                    case "UPDATE_APP":
                        final String appPackageName = getPackageName();
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                }

                // opening activity
                if (intent == null)
                    intent = new Intent(this, HomeActivity.class);
                else
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                isNotification = true;

                startActivity(intent);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (!isNotification) {

            Boolean isLogin = sharedPreferences.getBoolean("isLogin", false);

            if (isLogin) {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }else{
                Intent intent = new Intent(this, WelcomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        }
    }

    private void checkRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[2])){
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_required_title);
                builder.setMessage(R.string.permission_required_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SplashScreenActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (sharedPreferences.getBoolean("required_permission",false)) {
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
                    }
                });
                builder.show();
            }  else {
                //just request the permission
                ActivityCompat.requestPermissions(this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("required_permission",true);
            editor.apply();
        } else {
            initializeApplication();
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

                initializeApplication();

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[2])){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_required_title);
                builder.setMessage(R.string.permission_required_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(SplashScreenActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(),"Permissions are required!",Toast.LENGTH_LONG).show();
                SplashScreenActivity.this.finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, permissionsRequired[2]) == PackageManager.PERMISSION_GRANTED) {

                //Got Permission
                initializeApplication();

            }
        }
    }

}
