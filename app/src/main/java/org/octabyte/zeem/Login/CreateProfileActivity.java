package org.octabyte.zeem.Login;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.User;
import com.appspot.octabyte_zeem.zeem.model.UserInfoHolder;
import com.appspot.octabyte_zeem.zeem.model.Username;
import org.octabyte.zeem.API.AccountTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.CloudStorage;
import org.octabyte.zeem.Utils.GPSTracker;
import org.octabyte.zeem.Utils.ScalingUtilities;
import org.octabyte.zeem.Utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 9/7/2017.
 */

public class CreateProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private String[] permissionsRequired = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};
    String[] storagePermission = Arrays.copyOfRange(permissionsRequired, 0, 2);

    private boolean permissionsGranted = false;

    private static final String TAG = "CreateProfileActivity";
    private static final int RESULT_PICK_IMAGE = 137;
    private static final int PIC_CROP = 13777;

    private EditText
            etUsername,
            etFullName,
            etStatus,
            etEmail;

    private TextView tvDob;

    private LinearLayout
            llProfileTop,
            llUserSuggestion;
    private TextView
            tvSuggestion1;
    private TextView tvSuggestion2;
    private TextView tvSuggestion3;

    private ImageView ivProfilePic;

    private SharedPreferences sharedPreferences;

    private Boolean userAvailable = false;

    private String defaultUsername, localPicPath;

    private String status = "A new age of social media!";
    private String email;
    private int dob;
    private String profilePic;
    private View vLoadingView;
    private String gender = "M";
    private Calendar calendar;
    private Button btnCreateProfile;
    private List<Long> userContactList = new ArrayList<>();
    private String firebaseToken;
    private int progressMsgIndex;
    private Timer timer;
    private ProgressDialog progressDialog;
    private String defaultUserPic = "PIC/default_user_pic.jpg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_create_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        initVariable();
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        // Set Full Name
        String fullName = sharedPreferences.getString("fullName", "");
        etFullName.setText(fullName);
        // Find a username by default and set into etUserName
        //defaultUsername();

        String readContactPermission = Manifest.permission.READ_CONTACTS;
        // Fetch user contacts
        if(ActivityCompat.checkSelfPermission(this, readContactPermission) == PackageManager.PERMISSION_GRANTED) {
            fetchContacts();
        }

        if(ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) == PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = true;
        }

        // Show Suggestion to user
        userSuggestion();
    }
    private void initVariable(){
        ivProfilePic = findViewById(R.id.ivProfilePic);
        etUsername = findViewById(R.id.etUsername);
        etFullName = findViewById(R.id.etFullName);
        etStatus = findViewById(R.id.etStatus);
        etEmail = findViewById(R.id.etEmail);
        tvDob = findViewById(R.id.tvDob);
        llProfileTop = findViewById(R.id.llProfileTop);
        llUserSuggestion = findViewById(R.id.llUserSuggestion);
        tvSuggestion1 = findViewById(R.id.tvSuggestion1);
        tvSuggestion2 = findViewById(R.id.tvSuggestion2);
        tvSuggestion3 = findViewById(R.id.tvSuggestion3);
        TextView tvChangePhoto = findViewById(R.id.tvChangePhoto);
        btnCreateProfile = findViewById(R.id.btnCreateProfile);

        btnCreateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (permissionsGranted) {
                    createProfile();
                }else {
                    checkRuntimePermission();
                }
            }
        });

        tvSuggestion1.setOnClickListener(this);
        tvSuggestion2.setOnClickListener(this);
        tvSuggestion3.setOnClickListener(this);
        tvChangePhoto.setOnClickListener(this);
        ivProfilePic.setOnClickListener(this);

        calendar = Calendar.getInstance();

        tvDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(CreateProfileActivity.this, dateListener, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            /*calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);*/
            String dataFormat = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
            tvDob.setText(dataFormat);
            dataFormat = dataFormat.replaceAll("-", "");
            dob = Integer.parseInt(dataFormat);
        }

    };


    private void fetchContacts(){

        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                return new CursorLoader(CreateProfileActivity.this, CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                while(cursor.moveToNext()){
                    String phoneNumber = cursor.getString( cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER ) );

                    phoneNumber = phoneNumber.replaceAll("[^\\d]", "");

                    if (phoneNumber.length() >= 10){
                        phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                        userContactList.add(Long.valueOf(phoneNumber));
                    }

                }

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });

    }

    private void checkRuntimePermission(){
        if(ActivityCompat.checkSelfPermission(this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[1])){
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_storage_title);
                builder.setMessage(R.string.permission_storage_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CreateProfileActivity.this,storagePermission,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        CreateProfileActivity.this.finish();
                    }
                });
                builder.show();
            } else if (sharedPreferences.getBoolean("storage_permission",false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_storage_title);
                builder.setMessage(R.string.permission_storage_msg);
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
                        CreateProfileActivity.this.finish();
                    }
                });
                builder.show();
            }  else {
                //just request the permission
                ActivityCompat.requestPermissions(this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("storage_permission",true);
            editor.apply();
        } else {
            permissionsGranted = true;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                allgranted = true;
            }

            if(allgranted){

                permissionsGranted = true;

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[1])){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_storage_title);
                builder.setMessage(R.string.permission_storage_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CreateProfileActivity.this,storagePermission,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        CreateProfileActivity.this.finish();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(),"Permission is required!",Toast.LENGTH_LONG).show();
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
                permissionsGranted = true;

            }
        }else if (requestCode == RESULT_PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            CropImage.activity(selectedImage)
                    .setAspectRatio(1,1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(CreateProfileActivity.this);

        }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                localPicPath = resultUri.getPath();
                Glide.with(CreateProfileActivity.this).load(localPicPath)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivProfilePic);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(CreateProfileActivity.this, "Unable to crop your photo", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void userSuggestion(){
        etUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){

                    // Get username
                    String username = etUsername.getText().toString();
                    String fullName = etFullName.getText().toString();

                    // set userAvailable FALSE that user not able to
                    // create profile before checking user name
                    userAvailable = false;
                    // send username text to server for checking or suggestions
                    AccountTask<Username> checkUsername = new AccountTask<>();
                    checkUsername.setUsername(username);
                    checkUsername.setFullName(fullName);
                    checkUsername.execute("checkUsername");
                    checkUsername.setListener(new AccountTask.Response<Username>() {
                        @Override
                        public void response(Username response) {
                            if (response != null){
                                if (response.getAvailable()){ // If username is available
                                    hideSuggestionLayout();
                                    userAvailable = true;
                                }else{ // Username is not available show suggestion
                                    if (response.getSuggestions().size() == 3){
                                        tvSuggestion1.setText(response.getSuggestions().get(0));
                                        tvSuggestion2.setText(response.getSuggestions().get(1));
                                        tvSuggestion3.setText(response.getSuggestions().get(2));
                                        showSuggestionLayout();
                                    }else if (response.getSuggestions().size() == 2){
                                        tvSuggestion1.setText(response.getSuggestions().get(0));
                                        tvSuggestion2.setText(response.getSuggestions().get(1));
                                        showSuggestionLayout();
                                    }else if (response.getSuggestions().size() == 1){
                                        tvSuggestion1.setText(response.getSuggestions().get(0));
                                        showSuggestionLayout();
                                    }
                                }
                            }else{
                                Log.w("APIDebugging", "null response");
                            }
                        }
                    });

                }
            }
        });
    }

    private void showSuggestionLayout(){
        llProfileTop.setVisibility(View.GONE);
        llUserSuggestion.setVisibility(View.VISIBLE);
    }
    private void hideSuggestionLayout(){
        userAvailable = true;
        llProfileTop.setVisibility(View.VISIBLE);
        llUserSuggestion.setVisibility(View.GONE);
    }

    private void pickImageFromGallery(){
        if (permissionsGranted) {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, RESULT_PICK_IMAGE);
        } else {
            checkRuntimePermission();
        }
    }
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.genderMale:
                if (checked)
                    gender = "M";
                    defaultUserPic = "PIC/default_user_pic.jpg";
                    break;
            case R.id.genderFemale:
                if (checked)
                    gender = "F";
                    defaultUserPic = "PIC/default_female_user_pic.jpg";
                    break;
        }
    }

    private void createProfile(){

        if (!Utils.isNetworkAvailable(this)){
            Toast.makeText(this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
            return;
        }

        if (tvDob.getText().toString().isEmpty()){
            Toast.makeText(this, "Please select date of birth", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCreateProfile.setEnabled(false);

        showLoading();

        String username = etUsername.getText().toString().toLowerCase();
        String fullName = etFullName.getText().toString();
        status = etStatus.getText().toString();
        email = etEmail.getText().toString();

        // check username is not empty and user is available
        if(username.isEmpty() || !userAvailable){
            Toast.makeText(this, "Select username", Toast.LENGTH_SHORT).show();
            return;
        }else if(fullName.isEmpty()){
            Toast.makeText(this, "Your Full Name", Toast.LENGTH_SHORT).show();
            return;
        }else if(email.isEmpty()){
            Toast.makeText(this, "Provide your email", Toast.LENGTH_SHORT).show();
            return;
        }else if(status.isEmpty()){
            status = "New Here";
        }

        String fileNameGlobalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.PIC);

        // Send data to server to create profile first create user object
        // Getting phone number from shared Pref.
        Long phone = sharedPreferences.getLong("phone",1234567L);
        User user = new User();
        user.setProfilePic(fileNameGlobalSource);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPhone(phone);

        if (localPicPath == null && fileNameGlobalSource == null){
            user.setProfilePic(defaultUserPic);
        }

        firebaseToken = FirebaseInstanceId.getInstance().getToken();
        user.setFirebaseToken(firebaseToken);

        // Get user lat and lng
        Double lat = 0.0;
        Double lng = 0.0;
        GPSTracker gps = new GPSTracker(this);
        if (gps.canGetLocation()){
            lat = gps.getLatitude();
            lng = gps.getLongitude();
        }

        registerUser(lat, lng, user);

        // upload file to cloud storage
        if (localPicPath != null && fileNameGlobalSource != null){
            CloudStorage cloudStorage = new CloudStorage(this);
            cloudStorage.execute(localPicPath, fileNameGlobalSource);
        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void registerUser(Double lat, Double lng, User user){

        /*List<Long> tempContactList = new ArrayList<>();
        tempContactList.add(3151101492L);*/

        HashSet<Long> hashSet = new HashSet<>(userContactList);
        userContactList.clear();
        userContactList.addAll(hashSet);

        if (userContactList.size() > 1000)
            userContactList = new ArrayList<>(userContactList.subList(0, 1000));

        // Removing self number
        userContactList.remove(user.getPhone());

        // Create userInfoHolder object
        UserInfoHolder userInfoHolder = new UserInfoHolder();
        userInfoHolder.setUser(user);
        userInfoHolder.setContactList(userContactList);

        AccountTask<User> account = new AccountTask<>();
        account.setLat(lat);
        account.setLng(lng);
        account.setUserInfoHolder(userInfoHolder);
        account.execute("registerUser");
        account.setListener(new AccountTask.Response<User>() {
            @Override
            public void response(User user) {
                if (user != null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("fullName", user.getFullName());
                    editor.putString("username", user.getUsername());
                    editor.putString("firebaseToken", firebaseToken);
                    editor.putLong("userId", user.getUserId());
                    editor.putLong("phone", user.getPhone());
                    editor.putString("profilePic", Utils.bucketURL + user.getProfilePic());
                    editor.putInt("badge", user.getBadge());
                    editor.putString("status", status);
                    editor.putString("email", email);
                    editor.putString("gender", gender);
                    editor.putInt("dob", dob);
                    editor.putBoolean("isLogin", true);
                    editor.apply();

                    updateProfile(user.getUserId());
                }else{
                    Log.w("APIDebugging", "null response while creating user account - CreateProfileActivity->registerUser");
                }
            }
        });
    }

    private void updateProfile(Long userId){
        UserTask<Void> userTask = new UserTask<>(userId);
        userTask.setStatus(status);
        userTask.setEmail(email);
        userTask.setDob(dob);
        userTask.setGender(gender);
        userTask.execute("updateProfile");
        userTask.setListener(new UserTask.Response<Void>() {
            @Override
            public void response(Void response) {
                progressDialog.dismiss();
                Intent intent = new Intent(CreateProfileActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvSuggestion1:
                etUsername.setText(tvSuggestion1.getText().toString());
                hideSuggestionLayout();
                break;
            case R.id.tvSuggestion2:
                etUsername.setText(tvSuggestion2.getText().toString());
                hideSuggestionLayout();
                break;
            case R.id.tvSuggestion3:
                etUsername.setText(tvSuggestion3.getText().toString());
                hideSuggestionLayout();
                break;
            case R.id.tvChangePhoto:
                pickImageFromGallery();
                break;
            case R.id.ivProfilePic:
                pickImageFromGallery();
                break;
        }
    }

    private void showLoading(){
        final String[] progressMsgs = {"Please wait...", "Creating profile...", "Finding friends...",
                                "Setup home page...", "Just few more sec...", "Almost done...", "Finishing up..."};
        progressMsgIndex = 0;

        //ProgressDialog.show(this,"","Processing...", true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(progressMsgs[progressMsgIndex]);
        progressDialog.show();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressMsgIndex < 6){
                            progressMsgIndex++;
                            progressDialog.setMessage(progressMsgs[progressMsgIndex]);
                        }else{
                            try {
                                timer.cancel();
                                timer.purge();
                                timer = null;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        },10000, 10000);

        /*btnCreateProfile.setVisibility(View.GONE);
        vLoadingView = ((ViewStub) findViewById(R.id.vsProfileLoading)).inflate();*/
    }
    private void hideLoading(){
        vLoadingView.setVisibility(View.GONE);
    }

}
