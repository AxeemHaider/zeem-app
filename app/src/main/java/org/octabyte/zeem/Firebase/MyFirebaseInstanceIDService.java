package org.octabyte.zeem.Firebase;

import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import org.octabyte.zeem.API.UserTask;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {


    @Override
    public void onTokenRefresh() {

        //For registration of token
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //To displaying token on logcat
        Log.w("TOKEN: ", refreshedToken);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
        final Long userId = sharedPreferences.getLong("userId", 0L);

        if (!userId.equals(0L)){

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("firebaseToken", refreshedToken);
            editor.apply();

            Handler handler = new Handler(this.getMainLooper());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    // Update token for this user
                    UserTask<Void> userTask = new UserTask<>(userId);
                    userTask.setFirebaseToken(refreshedToken);
                    userTask.execute("refreshFirebaseToken");

                }
            };

            handler.post(runnable);

        }

    }

}