package yoko.puyo.mygame;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Insets;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private final String TAG = "abc";
    private GameBoardView mSurfaceView;

    private SensorManager sensorManager;
    private Sensor accel;
    private Sensor mag;
    private static final int MATRIX_SIZE = 16;
    private float[] accelVal = new float[3];    //センサーの値
    private float[] magVal = new float[3];      //センサーの値

    public static int tate = 0;                 //何度傾いているか
    public static int yoko = 0;                 //何度傾いているか

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mSurfaceView = new GameBoardView(this);
//        setContentView(mSurfaceView);

        setContentView(R.layout.activity_main);
        SurfaceView sv = findViewById(R.id.plate);
        mSurfaceView = new GameBoardView(this, sv);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this, accel);
        sensorManager.unregisterListener(this, mag);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //第3引数はデータ取得間隔　SensorManager.SENSOR_DELAY_FASTEST、NORMALなど
        sensorManager.registerListener(this,accel,100000);
        sensorManager.registerListener(this,mag, 100000);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[]  inR = new float[MATRIX_SIZE];
        float[] outR = new float[MATRIX_SIZE];
        float[]    I = new float[MATRIX_SIZE];
        float[] orVal = new float[3];

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER :
                accelVal = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD :
                magVal = event.values.clone();
                break;
        }
        if (magVal != null && accelVal != null) {
            //加速度センサーの値と地磁気センサーの値からinRとIを計算しinRからoutRを計算しoutRからorValを計算
            SensorManager.getRotationMatrix(inR, I, accelVal, magVal);
            SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X,SensorManager.AXIS_Y, outR);
            SensorManager.getOrientation(outR, orVal);

            tate = rad2Deg(orVal[1]);
            yoko = rad2Deg(orVal[2]);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }
    int rad2Deg(float rad) {
        return (int)Math.toDegrees(rad);
    }
}
