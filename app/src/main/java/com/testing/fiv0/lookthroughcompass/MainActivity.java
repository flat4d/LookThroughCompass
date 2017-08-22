package com.testing.fiv0.lookthroughcompass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private static final String TAG = "LookThroughCompass";

    private TextView mTextView;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] gData = new float[3]; // accelerometer
    private float[] mData = new float[3]; // magnetometer
    private float[] rMat = new float[9];
    private float[] rMatTmp = new float[9];
    private float[] iMat = new float[9];
    private float[] orientation = new float[3];

    long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView)findViewById(R.id.text);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private static final float ALPHA = 0.5f; // if ALPHA = 1 OR 0, no filter applies.

    protected float[] lowPass( float[] input, float[] output ) {
      if ( output == null ) return input;
      for ( int i=0; i<input.length; i++ ) {
        output[i] = output[i] + ALPHA * (input[i] - output[i]);
      }
      return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      switch(event.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
          //gData = event.values.clone();
          gData = lowPass(event.values.clone(), gData);
          break;
        case Sensor.TYPE_MAGNETIC_FIELD:
          //mData = event.values.clone();
          mData = lowPass(event.values.clone(), mData);
          break;
        default:
      }

      if(SensorManager.getRotationMatrix( rMatTmp, iMat, gData, mData)){

        SensorManager.remapCoordinateSystem(rMatTmp, SensorManager.AXIS_X, SensorManager.AXIS_Z, rMat);
        SensorManager.getOrientation(rMat, orientation);
        if(event.timestamp - timestamp > 200000000){
          int currentAzimuth = (int)(Math.toDegrees(orientation[0]) + 360) % 360;
          Log.d(TAG,"Current Angle:" + String.valueOf(currentAzimuth));
          mTextView.setText(String.valueOf(currentAzimuth));
          timestamp = event.timestamp;
        }

        if(timestamp == -1) timestamp = event.timestamp;
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // not in use
    }
}
