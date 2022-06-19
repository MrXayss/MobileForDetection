package org.tensorflow.lite.examples.classification;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;


public class Main extends Activity implements SensorEventListener {


    private SensorManager mSensorManager;
    public int light;
    public int trafficid;
    TextView CompOrient,xshow,yshow,zshow,karman,trafficlight,rasst,idtraffic,status,accuracy;
    TextView tvLocationNet;
    Button  push_bottom;
    private LocationManager locationManager;
    public double Latitude,Longitude;
    private int a,b;
    MediaPlayer mPlayer,mLeft,mUp,mRight,success,error,down,mFindTraffic;
    int clickCount = 0;

    private Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CompOrient = (TextView) findViewById(R.id.Header);
        xshow = (TextView) findViewById(R.id.textView);
        yshow = (TextView) findViewById(R.id.textView2);
        zshow = (TextView) findViewById(R.id.textView3);
        karman = (TextView) findViewById(R.id.textView4);
        trafficlight = (TextView) findViewById(R.id.textView5);
        rasst = (TextView) findViewById(R.id.textView7);
        idtraffic = (TextView) findViewById(R.id.textView6);
        status = (TextView) findViewById(R.id.textView8);
        accuracy = (TextView) findViewById(R.id.textView10);а:
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        tvLocationNet = findViewById(R.id.tvLocationNet);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mPlayer= MediaPlayer.create(this, R.raw.turnongeo);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlay();
            }
        });
        mLeft = MediaPlayer.create(this, R.raw.left);
        mLeft.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mLeft.stop();
                try {
                    mLeft.prepare();
                    mLeft.seekTo(0);
                }
                catch (Throwable t) {

                }
            }
        });
        mUp = MediaPlayer.create(this, R.raw.up);
        mUp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mUp.stop();
                try {
                    mUp.prepare();
                    mUp.seekTo(0);
                }
                catch (Throwable t) {

                }
            }
        });
        mRight = MediaPlayer.create(this, R.raw.right);
        mRight.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mRight.stop();
                try {
                    mRight.prepare();
                    mRight.seekTo(0);
                }
                catch (Throwable t) {

                }
            }
        });
        success = MediaPlayer.create(this, R.raw.success);
        success.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                success.stop();
                try {
                    success.prepare();
                    success.seekTo(0);
                }
                catch (Throwable t) {

                }
            }
        });
        error = MediaPlayer.create(this, R.raw.error);
        error.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                error.stop();
                try {
                    error.prepare();
                    error.seekTo(0);
                }
                catch (Throwable t) {

                }
            }
        });
        down = MediaPlayer.create(this, R.raw.down);
        down.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                down.stop();
                try {
                    down.prepare();
                    down.seekTo(0);
                }
                catch (Throwable t) {

                }
            }
        });
        mFindTraffic = MediaPlayer.create(this, R.raw.findtraffic);
        mFindTraffic.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mFindTraffic.stop();
                try {
                    mFindTraffic.prepare();
                    mFindTraffic.seekTo(0);
                }
                catch (Throwable t) {

                }
            }
        });
        ArrayList<String> permissions_to_request = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions_to_request.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissions_to_request.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissions_to_request.size() > 0) {
            String[] _permissions = permissions_to_request.toArray(new String[permissions_to_request.size()]);
            ActivityCompat.requestPermissions(this,
                    _permissions,
                    1);
        }
        TimerTask task = new TimerTask() {
            public void run() {
                boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (enabled == false)
                {
                    mPlayer.start();
                }
                else {
                    JSONObject post_dict = new JSONObject();
                    try {
                        post_dict.put("Latitude", Latitude);
                        post_dict.put("Longitude", Longitude);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        String words1 = new SendLocation().execute(String.valueOf(post_dict)).get();
                        if (words1 == null)
                        {
                           error.start();
                        }
                        else
                        {
                            String[] words = words1.split("-");
                            light = Integer.parseInt(words[0]);
                            trafficid = Integer.parseInt(words[1]);
                        }
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Timer t = new Timer();
        t.scheduleAtFixedRate(task, 6000,5000);
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.mipmap.shed);
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickCount++;
                if (clickCount==5) {
                    imageView.setVisibility(View.INVISIBLE);
                }
            }
        } );
    }
    @Override
    protected void onStop() {
        android.os.Process.killProcess(android.os.Process.myPid());
        super.onStop();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 1,
                locationListener);
//        locationManager.requestLocationUpdates(
//                LocationManager.NETWORK_PROVIDER, 0, 1,
//                locationListener);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION ),
                SensorManager.SENSOR_DELAY_GAME);
    }
    private void stopPlay(){
        mPlayer.stop();
        try {
            mPlayer.prepare();
            mPlayer.seekTo(0);
        }
        catch (Throwable t) {
            Toast.makeText(this, t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void play(View view){

        mPlayer.start();
    }
    private LocationListener locationListener = new LocationListener() {


        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }


        @Override
        public void onProviderEnabled(String provider) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
            }
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderDisabled(String provider) {

        }


    };

    private void showLocation(Location location) {
        if (location == null)
            return;
        if (location.getProvider().equals(
                LocationManager.GPS_PROVIDER)){
//        if (location.getProvider().equals(
//                LocationManager.NETWORK_PROVIDER)){
            tvLocationNet.setText(formatLocation(location));
            accuracy.setText("Точность:"+String.valueOf(location.getAccuracy()));
            Latitude = location.getLatitude();
            Longitude = location.getLongitude();
        }
    }
    private String formatLocation(Location location)  {
        if (location == null)
            return "";
        return String.format(
                "Местоположение: lat = %1$f, lon = %2$f",
                location.getLatitude(), location.getLongitude());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {

        float degree = Math.round(event.values[0]);
        CompOrient.setText("Отклонение от севера: " + Float.toString(degree) + " градусов");
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION ) != null) {
            boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (enabled == true && light != 0 && trafficid !=0)
            {
                rasst.setText("Необходимое отклонение: "+light);
                idtraffic.setText("Айди светофора: "+trafficid);
                checkkarman(Math.round(event.values[1]),Math.round(event.values[0]));
                checkall();
            }
            else if(enabled == false)
            {
                status.setText("ВКЛ. ГЕО");
            }
            else{
                status.setText("ПОИСК");
            }
            xshow.setText("x: "+Math.round(event.values[0]));
            yshow.setText("y: "+Math.round(event.values[1]));
            zshow.setText("z: "+Math.round(event.values[2]));
        }

    }
    protected  void checkkarman(int a,int b)
    {
        if (-78 >= a && a >= -93)
        {
            karman.setText("На позиции");
            mUp.stop();
            down.stop();
            status.setText("НАВЕДЕНИЕ");
            checktraffic(b);
        }
        else if(-80 >= a)
        {
            karman.setText("Ниже телефон");
            down.start();
            status.setText("НАВЕДЕНИЕ");
            mFindTraffic.stop();
        }
        else if(a >= -78 && a<0)
        {
            karman.setText("Выше телефон");
            mUp.start();
            status.setText("НАВЕДЕНИЕ");
            mFindTraffic.stop();
        }
        else if (a >= 0)
        {
            mFindTraffic.start();
            status.setText("НАЙДЕН СВЕТОФОР");
            karman.setText("В кармане");
        }
    }
    protected  void checktraffic(int a)
    {
        if ((light - 5) <= a && a <= (light + 5))
        {
            trafficlight.setText("Есть");
        }
        else if((light - 5) < a)
        {
            trafficlight.setText("Левее");
            mLeft.start();
        }
        else if(a < (light + 5))
        {
            trafficlight.setText("Правее");
            mRight.start();
        }
        else
        {
            trafficlight.setText("Нет светофора");
        }
    }
    protected  void checkall()
    {
        if(trafficlight.getText()=="Есть" &&  karman.getText()=="На позиции") {
            success.start();
            Intent intent = new Intent(Main.this, ClassifierActivity.class);
            startActivity(intent);
            System.exit(0);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
