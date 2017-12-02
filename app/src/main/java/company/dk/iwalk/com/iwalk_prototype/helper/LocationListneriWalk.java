package company.dk.iwalk.com.iwalk_prototype.helper;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class LocationListneriWalk implements LocationListener {

    private Context context;
    private double longitude = 0;
    private double latitude = 0;
    private String cityName = "";



    public LocationListneriWalk(Context context){
        this.context = context;
    }
    @Override
    public void onLocationChanged(Location loc) {
        longitude = loc.getLongitude();
        latitude = loc.getLatitude();

        Log.e(TAG, " "+longitude);
        Log.e(TAG, " "+latitude);

        cityName = null;
        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LocationObject getCurrentLocationInformation(){
        return new LocationObject(longitude,latitude,cityName);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

}
