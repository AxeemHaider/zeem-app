package org.octabyte.zeem.Utils;

import android.content.Context;
import android.util.Log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

class GCStorage {

    private Context mContext;
    private Properties properties;
    private static Storage storage;

    private static final String PROJECT_ID_PROPERTY = "project.id";
    private static final String APPLICATION_NAME_PROPERTY = "application.name";
    private static final String ACCOUNT_ID_PROPERTY = "account.id";

    private final String BUCKET_NAME = "octabyte-zeem";

    public GCStorage(Context context) {
        this.mContext = context;
    }

    /**
     * Uploads a file to a bucket. Filename and content type will be based on
     * the original file.
     *
     * @param filePath
     *            Absolute path of the file to upload
     * @param fileName
     *              Name of file with folder that need to store in cloud storage
     * @throws Exception
     * @return StorageObject once file is uploaded successfully
     */
    public StorageObject uploadFile(String filePath, String fileName)
            throws Exception {

        Storage storage = getStorage();


        String ext = getExt(filePath);
        if (ext != null){
            switch (ext){
                case "jpg":
                    ext = "image/jpeg";
                    break;
                case "mp4":
                    ext = "video/mp4";
                    break;
                default:
                    ext = null;
            }
        }

        StorageObject object = new StorageObject();

        if (ext != null)
            object.setContentType(ext);

        object.setCacheControl("max-age=[3600]");


        File file = new File(filePath);

        InputStream stream = new FileInputStream(file);
        try {
            String contentType = URLConnection
                    .guessContentTypeFromStream(stream);
            InputStreamContent content = new InputStreamContent(contentType,
                    stream);

            Storage.Objects.Insert insert = storage.objects().insert(
                    BUCKET_NAME, object, content);
            insert.setName(fileName);

            return insert.execute();
        } finally {
            stream.close();
        }
    }

    private String getExt(String filePath){
        int strLength = filePath.lastIndexOf(".");
        if(strLength > 0)
            return filePath.substring(strLength + 1).toLowerCase();
        return null;
    }

    public void uploadFile(InputStream stream, String filename)
            throws Exception {

        Storage storage = getStorage();

        StorageObject object = new StorageObject();
        object.setContentType("image/jpeg");
        object.setCacheControl("max-age=[3600]");

        try {
            String contentType = URLConnection
                    .guessContentTypeFromStream(stream);
            InputStreamContent content = new InputStreamContent(contentType,
                    stream);


            Storage.Objects.Insert insert = storage.objects().insert(
                    BUCKET_NAME, null, content);
            insert.setName(filename);

            StorageObject klsd = insert.execute();
            JSONObject jsonObject = new JSONObject(klsd);
            Log.i("CloudStorageDebug", String.valueOf(klsd));
            Log.i("CloudStorageDebug", String.valueOf(jsonObject.getString("id")));
        } finally {
            stream.close();
        }
    }

    public void downloadFile(String bucketName, String fileName, String destinationDirectory) throws Exception {

        File directory = new File(destinationDirectory);
        if(!directory.isDirectory()) {
            throw new Exception("Provided destinationDirectory path is not a directory");
        }
        File file = new File(directory.getAbsolutePath() + "/" + fileName);

        Storage storage = getStorage();

        Storage.Objects.Get get = storage.objects().get(bucketName, fileName);
        FileOutputStream stream = new FileOutputStream(file);
        try {
            get.executeAndDownloadTo(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * Deletes a file within a bucket
     *
     * @param bucketName
     *            Name of bucket that contains the file
     * @param fileName
     *            The file to delete
     * @throws Exception
     */
    public void deleteFile(String bucketName, String fileName)
            throws Exception {

        Storage storage = getStorage();

        storage.objects().delete(bucketName, fileName).execute();
    }

    /**
     * Creates a bucket
     *
     * @param bucketName
     *            Name of bucket to create
     * @throws Exception
     */
    public void createBucket(String bucketName) throws Exception {

        Storage storage = getStorage();

        Bucket bucket = new Bucket();
        bucket.setName(bucketName);

        storage.buckets().insert(
                getProperties().getProperty(PROJECT_ID_PROPERTY), bucket).execute();
    }

    /**
     * Deletes a bucket
     *
     * @param bucketName
     *            Name of bucket to delete
     * @throws Exception
     */
    public void deleteBucket(String bucketName) throws Exception {

        Storage storage = getStorage();

        storage.buckets().delete(bucketName).execute();
    }

    /**
     * Lists the objects in a bucket
     *
     * @param bucketName bucket name to list
     * @return Array of object names
     * @throws Exception
     */
    public List<String> listBucket(String bucketName) throws Exception {

        Storage storage = getStorage();

        List<String> list = new ArrayList<>();

        List<StorageObject> objects = storage.objects().list(bucketName).execute().getItems();
        if(objects != null) {
            for(StorageObject o : objects) {
                list.add(o.getName());
            }
        }

        return list;
    }

    /**
     * List the buckets with the project
     * (Project is configured in properties)
     *
     * @return
     * @throws Exception
     */
    public List<String> listBuckets() throws Exception {

        Storage storage = getStorage();

        List<String> list = new ArrayList<>();

        List<Bucket> buckets = storage.buckets().list(getProperties().getProperty(PROJECT_ID_PROPERTY)).execute().getItems();
        if(buckets != null) {
            for(Bucket b : buckets) {
                list.add(b.getName());
            }
        }

        return list;
    }

    private Properties getProperties() throws Exception {

        if (properties == null) {
            properties = new Properties();
            InputStream stream = mContext.getAssets().open("cloudstorage.properties");
            try {
                properties.load(stream);
            } catch (IOException e) {
                throw new RuntimeException(
                        "cloudstorage.properties must be present in classpath",
                        e);
            } finally {
                stream.close();
            }
        }
        return properties;
    }

    private Storage getStorage() throws Exception {

        if (storage == null) {

            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            List<String> scopes = new ArrayList<>();
            scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);

            Credential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(
                            getProperties().getProperty(ACCOUNT_ID_PROPERTY))
                    .setServiceAccountPrivateKeyFromP12File(getPrivateKeyFile())
                    .setServiceAccountScopes(scopes).build();

            storage = new Storage.Builder(httpTransport, jsonFactory,
                    credential).setApplicationName(
                    getProperties().getProperty(APPLICATION_NAME_PROPERTY))
                    .build();
        }

        return storage;
    }

    private File getPrivateKeyFile() {
        File f = new File(mContext.getCacheDir() + "/my_private_key.p12");
        if (!f.exists())
            try {

                InputStream is = mContext.getAssets().open("OctaByte-Zeem-06fee172ad9d.p12");
                        FileOutputStream fos = new FileOutputStream(f);
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1)
                    fos.write(buffer, 0, read);

                fos.flush();
                is.close();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        Log.e("FILE", "FETCHED FILE:: " + f.getAbsolutePath() + " with data: " + f.length());
        return f;
    }
}