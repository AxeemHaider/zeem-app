package org.octabyte.zeem.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.model.StorageObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static org.octabyte.zeem.Home.HomeActivity.SHARED_PREFERENCE_FILE;

public class CloudStorage extends AsyncTask<String, Void, StorageObject> {

    private Context context;

    private SharedPreferences sharedPref;
    private Boolean trackUploadedFiles = true;

    public CloudStorage(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
    }

    public CloudStorage(Context context, Boolean trackUploadedFiles) {
        this.context = context;
        this.trackUploadedFiles = trackUploadedFiles;
        sharedPref = context.getSharedPreferences(SHARED_PREFERENCE_FILE, MODE_PRIVATE);
    }

    // params[0] - file path
    // params[1] - folder or file name
    @Override
    protected StorageObject doInBackground(String... params) {

        try {

            if (trackUploadedFiles) {
                InformerItem informerItem = new InformerItem(params[0], params[1]);

                // Check there is already some item in inform list or not
                String informerList = sharedPref.getString("cloudStorage", null);

                if (informerList == null){ // There is no item

                    // Create new Storage informer and save in shared pref.
                    StorageInformer storageInformer = new StorageInformer();
                    storageInformer.addInformerItem(informerItem);

                    // Convert into JSON string
                    String jsonString = convertObjectIntoString(storageInformer);
                    if (jsonString != null){
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("cloudStorage", jsonString);
                        editor.apply();
                    }


                }else { // There are some items already

                    StorageInformer storageInformer = convertStringIntoObject(informerList);
                    if (storageInformer != null){
                        List<InformerItem> newInformerList = storageInformer.getInformerItems();
                        newInformerList.add(informerItem);
                        storageInformer.setInformerItems(newInformerList);
                        // Convert into JSON string
                        String jsonString = convertObjectIntoString(storageInformer);
                        if (jsonString != null){
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("cloudStorage", jsonString);
                            editor.apply();
                        }
                    }

                }
            }

            // Upload file to Google Cloud Storage

            GCStorage gcStorage = new GCStorage(context);
            return gcStorage.uploadFile(params[0], params[1]);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(StorageObject response) {

        if (response != null){
            // check the response and inform app that this object is uploaded
            Log.i("CloudStorage", String.valueOf(response));

            // Get storage informer list from shared pref.
            String jsonString = sharedPref.getString("cloudStorage", null);
            if (jsonString != null){
                StorageInformer storageInformer = convertStringIntoObject(jsonString);
                if (storageInformer != null){
                    // Search file
                    List<InformerItem> informerList = storageInformer.getInformerItems();
                    if (informerList != null) {
                        for (InformerItem informerItem : informerList){
                            if (informerItem.getFileName().equals(response.getName())){
                                // remove this informer item from informer list
                                informerList.remove(informerItem);
                                break;
                            }
                        }

                        // set new list to storage informer
                        storageInformer.setInformerItems(informerList);
                    }

                    // check there is some items left in storage informer or not
                    if (storageInformer.getInformerItems().size() > 0) {

                        // Convert object into string
                        String storageInformerString = convertObjectIntoString(storageInformer);
                        if (storageInformerString != null) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("cloudStorage", storageInformerString);
                            editor.apply();
                        }

                    }else{ // There is no item left
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("cloudStorage", null);
                        editor.apply();
                    }


                }
            }

        }

    }

    private String convertObjectIntoString(StorageInformer storageInformer){
        JacksonFactory jacksonFactory = new JacksonFactory();
        try {
            return jacksonFactory.toString(storageInformer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private StorageInformer convertStringIntoObject(String jsonString){
        JacksonFactory jacksonFactory = new JacksonFactory();
        try {
            return jacksonFactory.fromString(jsonString, StorageInformer.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
