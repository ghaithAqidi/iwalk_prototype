package company.dk.iwalk.com.iwalk_prototype.helper;


public class Result {
    float min;
    float max;
    float standardDiviation;
    double distance;
    double longitude,latitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    String activity;

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }
    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getStandardDiviation() {
        return standardDiviation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public void setStandardDiviation(float standardDiviation) {
        this.standardDiviation = standardDiviation;
    }

    public String toString(){
        return "min: " + this.min + " max: " + this.max + " Standard Diviation: " + this.standardDiviation;
    }

}
