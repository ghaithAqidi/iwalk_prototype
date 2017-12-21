package company.dk.iwalk.com.iwalk_prototype;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import company.dk.iwalk.com.iwalk_prototype.helper.LocationListneriWalk;
import company.dk.iwalk.com.iwalk_prototype.helper.LocationObject;
import company.dk.iwalk.com.iwalk_prototype.helper.Result;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import static company.dk.iwalk.com.iwalk_prototype.R.id.time;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float[] valuesExtracted;
    private LocationListneriWalk myLocationListner;
    int sampleCounter1 = 0;
    int sampleCounter2 = 0;
    boolean startCounter2 = false;
    float[] sampleArray1 = new float[128];
    float[] sampleArray2 = new float[128];
    ArrayList<Result> resultArray1 = new ArrayList<>();
    ArrayList<Result> resultArray2 = new ArrayList<>();
    ArrayList<LocationObject> locationObjects1 = new ArrayList<>();
    ArrayList<LocationObject> locationObjects2 = new ArrayList<>();
    double[] differnceInDistance1 = new double[128];
    double[] differnceInDistance2 = new double[128];
    LocationObject loc = null;
    File testFile;
    boolean isRecording = true;
    ArrayList<Instances> dataArray = new ArrayList<>();
    int samplesToRecord = 1;
    int lazyCounter = 0;
    int lazyCounterMAX = 5;
    boolean isLazy = false;
    int samplesTillNotLazy = 0;
    int samplesResetMax = 8;
    int innerCounter = 1;
    int minutes = 0;
    TextView textTimer;
    int numberOfsecondsSnoozing;

    String[] perms = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION", "android.permission.INTERNET", "android.hardware.location.gps"};

    int permsRequestCode = 200;


    public LocationObject getLatestLocation() {
        return myLocationListner.getCurrentLocationInformation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(perms, permsRequestCode);
        }

        final Button dnd = (Button) findViewById(R.id.dnd);
        dnd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isRecording) {
                    TextView minutesInput = (EditText) findViewById(R.id.lazyTimerSleep);
                    minutes = Integer.parseInt(minutesInput.getText().toString());
                    numberOfsecondsSnoozing = Integer.parseInt(minutesInput.getText().toString()) * 60;
                    textTimer = (TextView) findViewById(R.id.timerView);

                    new CountDownTimer(numberOfsecondsSnoozing * 1000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            if (numberOfsecondsSnoozing <= 0) {
                                this.cancel();
                            } else {
                                innerCounter--;

                                textTimer.setText(minutes + ":" + innerCounter);

                                numberOfsecondsSnoozing--;
                                if (innerCounter == 0) {
                                    innerCounter = 59;
                                    minutes -= 1;
                                }
                                isRecording = false;
                                dnd.setText("App is snoozing, click to activate");
                            }


                        }

                        public void onFinish() {
                            textTimer.setText("The app is active now.");
                            isRecording = true;
                            dnd.setText("Give me a break");
                        }

                    }.start();


                } else {
                    //it means we are snoozing, so we reset the counter and get active!
                    numberOfsecondsSnoozing = 0;
                    textTimer.setText("The app is active now.");
                    isRecording = true;
                    dnd.setText("Distrub me, I want to be active!");
                    textTimer = (TextView) findViewById(R.id.timerView);
                }


            }
        });

        AssetManager assetMgr = this.getAssets();
        try {
            ObjectInputStream ois = new ObjectInputStream(assetMgr.open("treeFinal.model"));
            J48 cls = (J48) ois.readObject();
            ois.close();
        } catch (Exception e) {

        }


        //collect data:

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);


        Criteria locationCritera = new Criteria();
        String providerName = locationManager.getBestProvider(locationCritera,
                true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (providerName != null)
                locationManager.getLastKnownLocation(providerName);

        }
        myLocationListner = new LocationListneriWalk(this);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                0, myLocationListner);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 0, myLocationListner);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isRecording) {
            loc = myLocationListner.getCurrentLocationInformation();
            record(sensorEvent);
            if (resultArray2.size() == samplesToRecord) {
                TextView tvId = (TextView) findViewById(R.id.current_activity);
                tvId.setText(resultArray2.size() + " samples logged");
                printData();
                resultArray1 = new ArrayList<>();
                resultArray2 = new ArrayList<>();
            }
        }
    }

    public void record(SensorEvent sensorEvent) {
        float sample = doMath(sensorEvent);
        if (sampleCounter1 < 128) {
            sampleArray1[sampleCounter1] = sample;
            if (sampleCounter1 == 0) {
                differnceInDistance1[sampleCounter1] = 0;
                locationObjects1.add(sampleCounter1, loc);
            } else {
                LocationObject earlier = locationObjects1.get(sampleCounter1 - 1);
                differnceInDistance1[sampleCounter1] = distance(earlier.getLatitude(), loc.getLatitude(),
                        earlier.getLongitude(), loc.getLongitude());
                locationObjects1.add(sampleCounter1, loc);
            }

            sampleCounter1++;

            if (sampleCounter1 == 64) {
                startCounter2 = true;
            }

        } else {
            differnceInDistance1[0] = 0;
            locationObjects1.add(sampleCounter1, loc);
            resultArray1.add(calcualteStandardDiviation(sampleArray1, differnceInDistance1, loc.getLongitude(), loc.getLatitude()));
            sampleCounter1 = 0;
            sampleArray1[sampleCounter1] = sample;
        }

        if (startCounter2) {
            if (sampleCounter2 < 128) {
                sampleArray2[sampleCounter2] = sample;
                locationObjects2.add(sampleCounter2, getLatestLocation());
                if (sampleCounter2 == 0) {
                    differnceInDistance2[sampleCounter2] = 0;
                    locationObjects2.add(sampleCounter2, loc);
                } else {
                    LocationObject earlier = locationObjects2.get(sampleCounter2 - 1);
                    differnceInDistance2[sampleCounter2] = distance(earlier.getLatitude(), loc.getLatitude(),
                            earlier.getLongitude(), loc.getLongitude());
                    locationObjects2.add(sampleCounter2, loc);
                }
                sampleCounter2++;
            } else {
                differnceInDistance2[0] = 0;
                locationObjects2.add(sampleCounter2, loc);
                resultArray2.add(calcualteStandardDiviation(sampleArray2, differnceInDistance2, loc.getLongitude(), loc.getLatitude()));
                sampleCounter2 = 0;
                sampleArray2[sampleCounter2] = sample;
            }
        }
    }

    public float doMath(SensorEvent sensorEvent) {
        valuesExtracted = sensorEvent.values;
        float sample = (float) Math.sqrt(valuesExtracted[0] * valuesExtracted[0] + valuesExtracted[1] * valuesExtracted[1] + valuesExtracted[2] * valuesExtracted[2]);
        return sample;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public Result calcualteStandardDiviation(float[] sampleArray1, double[] distanceArray, double longitude, double latitude) {
        Result res = new Result();
        int N = 128;
        float min = (float) Double.POSITIVE_INFINITY;
        float max = (float) Double.NEGATIVE_INFINITY;
        float sum = 0;
        for (int i = 0; i < N; i++) {
            if (sampleArray1[i] < min) {
                min = sampleArray1[i];
            }
            if (sampleArray1[i] > max) {
                max = sampleArray1[i];
            }
            sum += sampleArray1[i];
        }
        float mean = sum / N;
        float summedDifference = 0;
        float standarDeviation = 0;
        for (int i = 0; i < N; i++) {
            summedDifference = summedDifference + ((sampleArray1[i] - mean) * (sampleArray1[i] - mean));
            standarDeviation = (float) Math.sqrt(summedDifference / (N - 1));
        }

        res.setMax(max);
        res.setMin(min);
        res.setStandardDiviation(standarDeviation);
        TextView tvId = (TextView) findViewById(R.id.current_activity);

        double accumulator = 0;
        //disntance calculations:
        for (int x = 0; x < 128; x++) {
            accumulator += distanceArray[x];
        }
        res.setDistance(accumulator / 128);

        res.setLongitude(longitude);
        res.setLatitude(latitude);
        return res;
    }

    public void printData() {
        weka.core.FastVector atts, attvals;
        atts = new weka.core.FastVector();
        attvals = new weka.core.FastVector();
        //for holding the datapoints
        ArrayList<double[]> datas = new ArrayList<>();
        ArrayList<double[]> datas2 = new ArrayList<>();

        //create the attributes of the data
        weka.core.Attribute min = new weka.core.Attribute("min");
        atts.addElement(min);

        weka.core.Attribute max = new weka.core.Attribute("max");
        atts.addElement(max);

        weka.core.Attribute div = new weka.core.Attribute("standard deviation");
        atts.addElement(div);

        weka.core.Attribute distance = new weka.core.Attribute("distance");
        atts.addElement(distance);

        /*weka.core.Attribute longitude = new weka.core.Attribute("longitude");
        atts.addElement(longitude);

        weka.core.Attribute latitude = new weka.core.Attribute("latitude");
        atts.addElement(latitude);*/


        attvals.addElement("Stationary");
        attvals.addElement("Active");
        attvals.addElement("Transport");

        atts.addElement(new weka.core.Attribute("current activity", attvals));

        //initialize datasets
        weka.core.Instances data;

        data = new weka.core.Instances("MyRelation", atts, 0);
        //add datapoints to the dataset
        //System.out.println(resultArray1.size());
        for (int i = 0; i < resultArray1.size(); i++) {

            double[] vals = new double[5];
            vals[0] = (Double.parseDouble(Float.toString(resultArray1.get(i).getMin())));
            vals[1] = (Double.parseDouble(Float.toString(resultArray1.get(i).getMax())));
            vals[2] = (Double.parseDouble(Float.toString(resultArray1.get(i).getStandardDiviation())));
            vals[3] = resultArray1.get(i).getDistance();
            //vals[4] = resultArray1.get(i).getLongitude();
            //vals[5] = resultArray1.get(i).getLatitude();

            //add vals to datapoints
            datas.add(vals);
            //add datapoints to the print
            data.add(new weka.core.Instance(1.0, datas.get(i)));
        }
        resultArray1 = new ArrayList<>();
        for (int i = 0; i < resultArray2.size(); i++) {
            double[] vals = new double[5];
            vals[0] = (Double.parseDouble(Float.toString(resultArray2.get(i).getMin())));
            vals[1] = (Double.parseDouble(Float.toString(resultArray2.get(i).getMax())));
            vals[2] = (Double.parseDouble(Float.toString(resultArray2.get(i).getStandardDiviation())));
            vals[3] = resultArray2.get(i).getDistance();
           // vals[4] = resultArray2.get(i).getLongitude();
           // vals[5] = resultArray2.get(i).getLatitude();

            //add vals to datapoints
            datas2.add(vals);
            //add datapoints to the print
            data.add(new weka.core.Instance(1.0, datas2.get(i)));
        }

        resultArray2 = new ArrayList<>();
        //we get 4 here System.out.println(data.numInstances());

        classifyData(data);
    }

    public static double distance(double lat1, double lat2, double lon1, double lon2) {
        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    public Instances classifyData(Instances instances) {
        Instances labeled = null;
        try {
            AssetManager assetMgr = this.getAssets();

            ObjectInputStream ois = new ObjectInputStream(assetMgr.open("treeFinal.model"));
            J48 cls = (J48) ois.readObject();
            ois.close();

            testFile = new File(this.getFilesDir(), "test");

            // load unlabeled data
            Instances unlabeled = instances;

            // set class attribute
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);

            // create copy
            labeled = new Instances(unlabeled);
            // label instances
            for (int i = 0; i < unlabeled.numInstances(); i++) {
                double clsLabel = cls.classifyInstance(unlabeled.instance(i));
                labeled.instance(i).setClassValue(clsLabel);
                TextView tvId = (TextView) findViewById(R.id.current_activity);
                System.out.println(clsLabel);
                if (clsLabel == 0.0) {
                    tvId.setText("we are stationary");
                    isLazy = true;
                } else if (clsLabel == 1.0) {
                    tvId.setText("We are active");
                    isLazy = false;
                } else {
                    tvId.setText("We are transporting");
                    isLazy = false;
                }
            }
            unlabeled = null;
            TextView consoleID = (TextView) findViewById(R.id.console);

            if (isLazy) {
                lazyCounter++;
                switch (lazyCounter) {
                    case 1:
                        lazyCounter = 1;
                        consoleID.setText("hmmmmm, getting comfortable in that sofa?");
                        break;
                    case 2:
                        lazyCounter = 4;
                        consoleID.setText("Almost time to be active again...");
                        break;
                }
            }
            samplesTillNotLazy++;
            if (lazyCounter > lazyCounterMAX) {
                prompUser();
                lazyCounter = 0;
                //lazyCounterMAX = lazyCounterMAX-5;
                resultArray1 = new ArrayList<>();
                resultArray2 = new ArrayList<>();
                consoleID.setText("Time for a walk!!!");
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.dings);
                mp.start();
            }


        } catch (Exception e) {

        }

        return new Instances(labeled);
    }

    private void prompUser() {
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.dings);
        mp.start();
    }
}
