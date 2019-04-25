package org.octabyte.zeem.Publish;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import org.octabyte.zeem.Utils.GPSTracker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

class LocationTask extends AsyncTask<Void, Void, List<Address>> {

    private Context context;

    private UserLocationTracker userLocationTracker;

    public LocationTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<Address> doInBackground(Void... voids) {

        Double lat = 0.0;
        Double lng = 0.0;
        GPSTracker gps = new GPSTracker(context);
        if (gps.canGetLocation()){
            lat = gps.getLatitude();
            lng = gps.getLongitude();
        }

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            return geocoder.getFromLocation(lat, lng, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Address> addresses) {
        if (addresses != null){
            userLocationTracker.onLocationTrack(addresses);
        }
    }

    public void setUserLocationTracker(UserLocationTracker userLocationTracker){
        this.userLocationTracker = userLocationTracker;
    }

    public interface UserLocationTracker{
        void onLocationTrack(List<Address> addresses);
    }
}
