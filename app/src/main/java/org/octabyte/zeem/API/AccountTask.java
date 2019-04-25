package org.octabyte.zeem.API;

import android.os.AsyncTask;

import com.appspot.octabyte_zeem.zeem.Zeem;
import com.appspot.octabyte_zeem.zeem.ZeemRequest;
import com.appspot.octabyte_zeem.zeem.ZeemRequestInitializer;
import com.appspot.octabyte_zeem.zeem.model.User;
import com.appspot.octabyte_zeem.zeem.model.UserInfoHolder;
import com.google.android.gms.common.api.Api;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;

import org.octabyte.zeem.Utils.Utils;

import java.io.IOException;
import java.util.List;

import static org.octabyte.zeem.Utils.Utils.ApiKey;

public class AccountTask<T> extends AsyncTask<String, Void, T> {

    private Response<T> response;

    private Long phone;
    private Double lat, lng;
    private String username, fullName;
    private UserInfoHolder userInfoHolder;

    public AccountTask() {
    }

    public AccountTask(Long phone) {
        this.phone = phone;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T doInBackground(String... strings) {
        String action = strings[0];

        // Create Api service
        Zeem.Builder builder = new Zeem.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        //builder.setZeemRequestInitializer(requestInitializer);
        Zeem zeem = builder.build();

        try {
            switch (action){
                case "checkRegistration":
                    return (T) zeem.account().checkRegistration(phone).setKey(ApiKey).execute();
                case "registerUser":
                    return (T) zeem.account().registerUser(lat, lng, userInfoHolder).setKey(ApiKey).execute();
                case "checkUsername":
                    return (T) zeem.account().checkUsername(username, fullName).setKey(ApiKey).execute();
                case "getAppInfo":
                    return (T) zeem.account().getAppInfo().setKey(ApiKey).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();

            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(T t) {
        try {
            response.response(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListener(Response<T> response) {
        this.response = response;
    }

    public interface Response<T>{
        void response(T response);
    }

    /*private ZeemRequestInitializer requestInitializer = new ZeemRequestInitializer(ApiKey){
        @Override
        protected void initializeZeemRequest(ZeemRequest<?> request) throws IOException {
            super.initializeZeemRequest(request);

            request.getRequestHeaders().set("X-Android-Package", Utils.appPackageName);
            request.getRequestHeaders().set("X-Android-Cert", Utils.appSignature);
        }
    };*/

    public void setUserInfoHolder(UserInfoHolder userInfoHolder) {
        this.userInfoHolder = userInfoHolder;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

}