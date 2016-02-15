package com.mobilesiri.compass;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private ImageView image,arrowIv;
    private float currentDegree = 0f; double arrowStarting;
    private SensorManager mSensorManager;
    TextView tvHeading;
    GPSTracker gps;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gps = new GPSTracker(this);
         sharedPref= getSharedPreferences("dirPref", 0);
         editor= sharedPref.edit();
        editor.putString("latidude", "");
        editor.putString("longitude", "");
        editor.apply();

        getLocation();
        image = (ImageView) findViewById(R.id.imageViewCompass);
        arrowIv=(ImageView) findViewById(R.id.arrow_IV);
        // TextView that will tell the user what degree is he heading

        tvHeading = (TextView) findViewById(R.id.tvHeading);



        // initialize your android device sensor capabilities

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),

                SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    float[] mGravity;
    float[] mGeomagnetic;

    @Override
    public void onSensorChanged(SensorEvent event) {
        float degree = Math.round(event.values[0]);


        tvHeading.setText( Float.toString(degree));



        // create a rotation animation (reverse turn degree degrees)

        RotateAnimation ra = new RotateAnimation(

                currentDegree,

                -degree,

                Animation.RELATIVE_TO_SELF, 0.5f,

                Animation.RELATIVE_TO_SELF,

                0.5f);
        RotateAnimation raArrow = new RotateAnimation(

                (float) arrowStarting,

                -degree,

                Animation.RELATIVE_TO_SELF, 0.5f,

                Animation.RELATIVE_TO_SELF,

                0.5f);


        ra.setDuration(210);

        ra.setFillAfter(true);

        image.startAnimation(ra);
        arrowIv.startAnimation(raArrow);

        currentDegree = -degree;
        arrowStarting=-degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    void getLocation()
    {
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            editor.putString("latitude", ""+latitude);
            editor.putString("longitude", ""+longitude);
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            // thhese four line to calculate angle from user to Mecca.
            double lonDelta = (longitude*(Math.PI/180) - 0.695096573227);
            double y = Math.sin(lonDelta) * Math.cos(latitude*(Math.PI/180));
            double x = Math.cos(0.373893159) * Math.sin(latitude*(Math.PI/180)) - Math.sin(0.373893159) * Math.cos(latitude*(Math.PI/180)) * Math.cos(lonDelta);
            double bearing =Math.toDegrees( Math.atan2(y, x));
            arrowStarting=bearing;

            Log.e("===="+bearing+"====",
                    "Your Location is Lat: " + latitude +
                            "\nLong: " + longitude+
                            "\n lat: "+latitude*(Math.PI/180)+
                            "\nlog: "+ longitude*(Math.PI/180)+
                            "\n lat: "+sharedPref.getString("latitude","")+"" +
                            " \nlog: "+ sharedPref.getString("longitude",""));
        }

        else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings

            if(sharedPref.getString("latitude","").equals("") || sharedPref.getString("latitude","").equals(""))
                gps.showSettingsAlert();
            else Toast.makeText(this,"If your location has changed press get location button",Toast.LENGTH_LONG).show();

        }
    }
}
