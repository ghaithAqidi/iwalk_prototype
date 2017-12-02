package company.dk.iwalk.com.iwalk_prototype.helper;

public class LocationObject {
    double longitude;
    double latitude;
    String city;

    public LocationObject(double longitude, double latitude, String city) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.city = city;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getCity() {
        return city;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "LocationObject{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                ", city='" + city + '\'' +
                '}';
    }

}
