package org.octabyte.zeem.Profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.CloudStorage;
import org.octabyte.zeem.Utils.ScalingUtilities;
import org.octabyte.zeem.Utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;
import static org.octabyte.zeem.Profile.ProfileActivity.INSTANT_EDIT_PROFILE;
import static org.octabyte.zeem.Profile.ProfileActivity.INSTANT_PROFILE_LOCAL_PIC;

/**
 * Created by Azeem on 8/28/2017.
 */

public class EditProfileActivity extends AppCompatActivity {

    private static final int RESULT_PICK_IMAGE = 137;
    private Toolbar toolbar;

    private Long mUserId;

    private ImageView ivProfilePic;
    private EditText etUsername, etFullName, etStatus, etEmail;

    private SharedPreferences pref;

    private String localPicPath;
    private String gender = "M";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        pref = getSharedPreferences(SHARED_PREFERENCE_FILE,MODE_PRIVATE);
        mUserId = pref.getLong("userId", 123L);

        initVariable();

        setValues();

    }
    private void initVariable(){

        RadioGroup radioGender = findViewById(R.id.radioGender);
        radioGender.setVisibility(View.GONE);

        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView toolbarNavigationBack = findViewById(R.id.toolbarNavigationBack);
        Button btnCreateProfile = findViewById(R.id.btnCreateProfile);
        btnCreateProfile.setText("Update Profile");

        toolbarTitle.setText(R.string.edit_profile);
        toolbarNavigationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnCreateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editMyProfile();
            }
        });

        ivProfilePic = findViewById(R.id.ivProfilePic);
        etUsername = findViewById(R.id.etUsername);
        etFullName = findViewById(R.id.etFullName);
        etStatus = findViewById(R.id.etStatus);
        etEmail = findViewById(R.id.etEmail);
        TextView tvDob = findViewById(R.id.tvDob);
        tvDob.setVisibility(View.GONE);
        TextView tvChangePhoto = findViewById(R.id.tvChangePhoto);

        etUsername.setEnabled(false);
        ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
        tvChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromGallery();
            }
        });
    }

    private void setValues(){

        String userFullName = pref.getString("fullName", null);

        if (userFullName != null)
            etFullName.setText(Utils.capitalize(userFullName));

        etUsername.setText(pref.getString("username", null));
        etStatus.setText(pref.getString("status", null));
        etEmail.setText(pref.getString("email", null));

        Glide.with(this).load(pref.getString("profilePic", null))
                .apply(RequestOptions.placeholderOf(R.drawable.placeholder_user))
                .apply(RequestOptions.circleCropTransform()).into(ivProfilePic);

    }

    private void pickImageFromGallery(){
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_PICK_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            CropImage.activity(selectedImage)
                    .setAspectRatio(1,1)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .start(EditProfileActivity.this);

        }else if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                localPicPath = resultUri.getPath();
                Glide.with(EditProfileActivity.this).load(localPicPath)
                        .apply(RequestOptions.circleCropTransform())
                        .into(ivProfilePic);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(EditProfileActivity.this, "Unable to crop your photo", Toast.LENGTH_SHORT).show();
            }
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
                break;
            case R.id.genderFemale:
                if (checked)
                    gender = "F";
                break;
        }
    }

    private void editMyProfile(){

        if (Utils.isNetworkAvailable(this)) {

            Toast.makeText(this, R.string.your_profile_is_edit_successfully, Toast.LENGTH_SHORT).show();

            String fileNameGlobalSource = Utils.cloudStorageFileName(Utils.GC_FOLDER.PIC);

            String name = etFullName.getText().toString();
            String status = etStatus.getText().toString();
            String email = etEmail.getText().toString();

            // Update shared pref.
            SharedPreferences.Editor editor = pref.edit();

            if(localPicPath != null) {
                editor.putString("profilePic", Utils.bucketURL + fileNameGlobalSource);
            }

            editor.putString("fullName", name);
            editor.putString("status", status);
            editor.putString("email", email);
            editor.putString("gender", gender);
            editor.apply();

            UserTask<Void> updateProfile = new UserTask<>(mUserId);
            updateProfile.setFullName(name);
            updateProfile.setStatus(status);
            updateProfile.setEmail(email);

            if(localPicPath != null) {
                updateProfile.setProfilePic(fileNameGlobalSource);
            }

            updateProfile.execute("updateProfile");

            INSTANT_EDIT_PROFILE = true;

            if (localPicPath != null && fileNameGlobalSource != null){

                INSTANT_PROFILE_LOCAL_PIC = localPicPath;

                CloudStorage cloudStorage = new CloudStorage(this);
                cloudStorage.execute(localPicPath, fileNameGlobalSource);
            }

            super.onBackPressed();
        } else {
            Toast.makeText(this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
        }
    }

}
