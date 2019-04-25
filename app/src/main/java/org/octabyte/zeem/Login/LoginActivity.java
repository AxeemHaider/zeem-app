package org.octabyte.zeem.Login;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appspot.octabyte_zeem.zeem.model.Registered;
import org.octabyte.zeem.API.AccountTask;
import org.octabyte.zeem.API.UserTask;
import org.octabyte.zeem.Home.HomeActivity;
import org.octabyte.zeem.Intro.BlockUserActivity;
import org.octabyte.zeem.R;
import org.octabyte.zeem.Utils.GPSTracker;
import org.octabyte.zeem.Utils.Utils;

import com.appspot.octabyte_zeem.zeem.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

/**
 * Created by Azeem on 9/6/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private String[] permissionsRequired = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    String[] contactPermission = Arrays.copyOfRange(permissionsRequired, 0, 2);

    private static final int MAIN_PERMISSION_CALLBACK_CONSTANT = 300;
    private static final int MAIN_REQUEST_PERMISSION_SETTING = 301;
    private String[] mainPermissionsRequired = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};
    String[] storagePermission = Arrays.copyOfRange(mainPermissionsRequired, 0, 2);


    private EditText etName, etPhone, etVerification, etCountryCode;
    private LinearLayout lyLoginForm, lyVerification;
    private SharedPreferences sharedPreferences;

    private View vLoadingView;
    private Button btnVerifyCode, btnLogin;
    private User registeredUser;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private String phoneNumber;
    private Timer timer;
    private TextView tvCodeTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        initVariable();

        setCountryCode();
    }
    private void initVariable(){
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etVerification = findViewById(R.id.etVerification);
        lyLoginForm = findViewById(R.id.lyLoginForm);
        lyVerification = findViewById(R.id.lyVerification);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        btnLogin = findViewById(R.id.btnLogin);
        etCountryCode = findViewById(R.id.etCountryCode);
        tvCodeTimer = findViewById(R.id.tvCodeTimer);

        tvCodeTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timerText = tvCodeTimer.getText().toString();
                if (timerText.equals(getString(R.string.re_send_code))){
                    Toast.makeText(LoginActivity.this, R.string.pin_code_resend, Toast.LENGTH_SHORT).show();
                    resendVerificationCode();

                    try {
                        if (timer != null){
                            timer.cancel();
                            timer = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        btnVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String verificationCode = etVerification.getText().toString();
                if (!verificationCode.isEmpty()) {

                    btnVerifyCode.setText(R.string.verifying);
                    btnVerifyCode.setEnabled(false);

                    verifyVerificationCode(verificationCode);

                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(etVerification.getWindowToken(), 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        tvCodeTimer.setVisibility(View.GONE);
                        if (timer != null){
                            timer.cancel();
                            timer = null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(LoginActivity.this, R.string.please_enter_verification_code, Toast.LENGTH_SHORT).show();
                } // end if else

            }
        });
    }

    private void setCountryCode(){
        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        if (manager != null) {
            CountryID = manager.getSimCountryIso().toUpperCase();
            String[] rl = this.getResources().getStringArray(R.array.CountryCodes);
            for (String aRl : rl) {
                String[] g = aRl.split(",");
                if (g[1].trim().equals(CountryID.trim())) {
                    CountryZipCode = g[0];
                    break;
                }
            }
        }

        etCountryCode.setText(CountryZipCode);
    }

    public void userLogin(View view){
        phoneNumber = etPhone.getText().toString();
        String fullName = etName.getText().toString();
        // Check phone field is not empty
        if(phoneNumber.isEmpty()){
            Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }
        if(fullName.isEmpty()){
            Toast.makeText(this, "Enter Your Name", Toast.LENGTH_SHORT).show();
            return;
        }


        try {
            phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();

            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();

            return;
        } catch (Exception e){
            e.printStackTrace();

            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show();

            return;
        }

        String countryCode = etCountryCode.getText().toString();

        phoneNumber = countryCode + phoneNumber;

        checkRuntimePermission();

        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etPhone.getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                builder.setTitle(R.string.permission_contacts_title);
                builder.setMessage(R.string.permission_contact_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this,contactPermission,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        verifyPhoneFromFireBase();
                    }
                });
                builder.show();
            }  else {
                //just request the permission
                ActivityCompat.requestPermissions(this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
            }

        } else {
            //You already have the permission, just go ahead.
            verifyPhoneFromFireBase();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean granted = false;
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                granted = true;
            }

            if(granted){

                verifyPhoneFromFireBase();

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[0])
                    ||ActivityCompat.shouldShowRequestPermissionRationale(this,permissionsRequired[1])){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_contacts_title);
                builder.setMessage(R.string.permission_contact_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this,contactPermission,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        verifyPhoneFromFireBase();
                    }
                });
                builder.show();
            } else {
                verifyPhoneFromFireBase();
            }
        }else if(requestCode == MAIN_PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                allgranted = true;
            }

            if(allgranted){

                openHomeActivity();

            } else if(ActivityCompat.shouldShowRequestPermissionRationale(this,mainPermissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,mainPermissionsRequired[1])){

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_storage_title);
                builder.setMessage(R.string.permission_storage_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this,storagePermission,MAIN_PERMISSION_CALLBACK_CONSTANT);
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
                verifyPhoneFromFireBase();

            }
        }else if (requestCode == MAIN_REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(this, mainPermissionsRequired[0]) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, mainPermissionsRequired[1]) == PackageManager.PERMISSION_GRANTED) {

                //Got Permission
                openHomeActivity();

            }
        }
    }

    public void changePhoneNumber(View view){
        lyVerification.setTranslationX(+Utils.getScreenWidth(this));
        lyVerification.setVisibility(View.GONE);
        lyLoginForm.setVisibility(View.VISIBLE);
        lyLoginForm.animate()
                .translationX(0)
                .setDuration(400)
                .start();

        btnLogin.setEnabled(true);
        btnLogin.setText(R.string.log_in);
    }

    private void resendVerificationCode(){

        tvCodeTimer.setVisibility(View.GONE);

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,             // OnVerificationStateChangedCallbacks
                mResendToken);
    }

    private void verifyPhoneFromFireBase(){

        if (!Utils.isNetworkAvailable(this)){
            Toast.makeText(this, R.string.error_in_network_connection, Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setText("Sending code...");
        btnLogin.setEnabled(false);
        showLoading();

        //initializing objects
        mAuth = FirebaseAuth.getInstance();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.

            //Getting the code sent by SMS
            String code = credential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code
            if (code != null) {
                etVerification.setText(code);
                //verifying the code
                signInWithPhoneAuthCredential(credential);

                showLoading();
                btnVerifyCode.setText(R.string.verifying);
                btnVerifyCode.setEnabled(false);

                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(etVerification.getWindowToken(), 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            try {
                tvCodeTimer.setVisibility(View.GONE);
                if (timer != null){
                    timer.cancel();
                    timer = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            //Log.w(TAG, "onVerificationFailed", e);

            hideLoading();
            btnLogin.setText(R.string.log_in);
            btnLogin.setEnabled(true);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                alertDialog.setTitle("Verification Failed");
                alertDialog.setMessage(R.string.invalid_phone_format);
                alertDialog.show();
            }else{
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                alertDialog.setTitle("Verification Failed");
                alertDialog.setMessage(e.getMessage());
                alertDialog.show();
            }

            try {
                if (timer != null){
                    timer.cancel();
                    timer = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void onCodeSent(String verificationId,
                PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.

            hideLoading();
            showVerificationForm();

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;

            startTimer();

            // ...
        }
    };

    private void verifyVerificationCode(String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");

                            btnVerifyCode.setText("Verified (just wait)");

                            String rPhoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                            checkRegistration(Long.valueOf(rPhoneNumber));

                            //FirebaseUser user = task.getResult().getUser();
                            // ...

                        } else {

                            hideLoading();
                            btnVerifyCode.setText(R.string.verify);
                            btnVerifyCode.setEnabled(true);

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                try {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                                    alertDialog.setTitle("Verification Failed");
                                    alertDialog.setMessage(R.string.verification_code_incorrect);
                                    alertDialog.show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(LoginActivity.this, "Unable to verify your phone", Toast.LENGTH_SHORT).show();
                                }

                            }else{
                                try {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                                    alertDialog.setTitle("Verification Failed");
                                    alertDialog.setMessage(task.getException().getMessage());
                                    alertDialog.show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(LoginActivity.this, "Unable to verify your phone", Toast.LENGTH_SHORT).show();
                                }
                            }

                            // Sign in failed, display a message and update the UI
                            /*Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }*/
                        }
                    }
                });
    }

    private void startTimer() {

        tvCodeTimer.setVisibility(View.VISIBLE);

        timer = new Timer();
        timer.schedule(new TimerTask() {

            int second = 60;

            @Override
            public void run() {
                if (second <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvCodeTimer.setText(R.string.re_send_code);
                            timer.cancel();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvCodeTimer.setText("Resending in 00:" + second--);
                        }
                    });
                }

            }
        }, 0, 1000);
    }

    private void showVerificationForm(){
        lyLoginForm.setTranslationX(-Utils.getScreenWidth(this));
        lyLoginForm.setVisibility(View.GONE);
        lyVerification.setVisibility(View.VISIBLE);
        lyVerification.setTranslationX(+Utils.getScreenWidth(this));
        lyVerification.animate()
                .translationX(0)
                .setDuration(400)
                .start();
    }

    private void checkRegistration(Long phone) {

        if (Utils.isNetworkAvailable(this)) {
            //showLoading();

            AccountTask<Registered> registration = new AccountTask<>(phone);
            registration.execute("checkRegistration");
            registration.setListener(new AccountTask.Response<Registered>() {
                @Override
                public void response(Registered registered) {
                    hideLoading();

                    if (registered != null) {
                        Log.w("APIDebugging", "Not a null response");
                        if (registered.getIsRegistered()) { // user is already registered
                            Log.w("APIDebugging", "user is already register");

                            registeredUser = registered.getUser();

                            checkRuntimeMainPermission();

                        } else { // user is not registered
                            Log.w("APIDebugging", "user is not registered");

                            String phoneNumber = etPhone.getText().toString();

                            try {
                                phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);
                            } catch (StringIndexOutOfBoundsException e) {
                                e.printStackTrace();

                                Toast.makeText(LoginActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();

                                return;
                            } catch (Exception e){
                                e.printStackTrace();

                                Toast.makeText(LoginActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();

                                return;
                            }

                            // Save phone number in shared pref.
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putLong("phone", Long.parseLong(phoneNumber));
                            editor.putString("fullName", etName.getText().toString());
                            editor.apply();
                            // Send user to create profile activity
                            Intent intent = new Intent(LoginActivity.this, CreateProfileActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    }else{
                        Log.w("APIDebugging", "null response in LoginActivity -> checkRegistration");
                    }
                }
            });

        }else {
            Toast.makeText(this, R.string.no_internet_connection_found, Toast.LENGTH_SHORT).show();
        }

    }


    public void openTermsAndCondition(View view) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.web_terms_and_condition)));
        startActivity(browserIntent);

    }

    private void openHomeActivity(){
        if (registeredUser != null) {
            if (registeredUser.getActive()) {
                // Get Firebase Token and refresh it in datastore
                final String refreshedToken = FirebaseInstanceId.getInstance().getToken();

                // get all information of that user and save it in shred pref. and send user to Home activity
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("fullName", registeredUser.getFullName());
                editor.putString("username", registeredUser.getUsername());
                editor.putString("firebaseToken", refreshedToken);
                editor.putLong("userId", registeredUser.getUserId());
                editor.putLong("phone", registeredUser.getPhone());
                editor.putString("profilePic", registeredUser.getProfilePic());
                editor.putInt("badge", registeredUser.getBadge());
                //editor.putString("gender", registered.getUser().getGender());
                editor.putBoolean("isLogin", true);
                editor.apply();


                // Update token for this user
                UserTask<Void> userTask = new UserTask<>(registeredUser.getUserId());
                userTask.setFirebaseToken(refreshedToken);
                userTask.execute("refreshFirebaseToken");

                // Send user to home activity
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isActiveUser", false);
                editor.apply();

                Intent intent = new Intent(this, BlockUserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "Unable to create profile for you", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRuntimeMainPermission(){
        if(ActivityCompat.checkSelfPermission(this, mainPermissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, mainPermissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, mainPermissionsRequired[2]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, mainPermissionsRequired[3]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,mainPermissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,mainPermissionsRequired[1])){
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.permission_storage_title);
                builder.setMessage(R.string.permission_storage_msg);
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this,storagePermission,MAIN_PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Toast.makeText(LoginActivity.this, "Storage Permission required!", Toast.LENGTH_LONG).show();
                        LoginActivity.this.finish();
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
                        startActivityForResult(intent, MAIN_REQUEST_PERMISSION_SETTING);
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
                ActivityCompat.requestPermissions(this,mainPermissionsRequired,MAIN_PERMISSION_CALLBACK_CONSTANT);
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("storage_permission",true);
            editor.apply();
        } else {
            openHomeActivity();
        }

    }

    private void showLoading(){
        if (vLoadingView == null)
            vLoadingView = ((ViewStub) findViewById(R.id.vsLoginLoading)).inflate();
        else
            vLoadingView.setVisibility(View.VISIBLE);
    }
    private void hideLoading(){
        if (vLoadingView != null) {
            vLoadingView.setVisibility(View.GONE);
        }
    }

}
