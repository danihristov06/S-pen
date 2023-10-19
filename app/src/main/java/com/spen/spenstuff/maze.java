package com.spen.spenstuff;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.samsung.android.sdk.penremote.AirMotionEvent;
import com.samsung.android.sdk.penremote.ButtonEvent;
import com.samsung.android.sdk.penremote.SpenEvent;
import com.samsung.android.sdk.penremote.SpenEventListener;
import com.samsung.android.sdk.penremote.SpenRemote;
import com.samsung.android.sdk.penremote.SpenUnit;
import com.samsung.android.sdk.penremote.SpenUnitManager;

public class maze extends AppCompatActivity {

    public ImageView drawingImageView;
    private Button mConnectButton;
    private Button mMotionButton;
    private SpenRemote mSpenRemote;
    private SpenUnitManager mSpenUnitManager;
    private boolean mIsMotionListening = false;
    private boolean buttonUp = true;
    private static final String TAG = "SpenRemoteSample";
    private float posX = 300;
    private float posY = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maze);
        configureBackBtn();

        drawingImageView = (ImageView) findViewById(R.id.Player);

        mMotionButton = findViewById(R.id.ConnectMaze);
        mSpenRemote = SpenRemote.getInstance();
        mSpenRemote.setConnectionStateChangeListener(new SpenRemote.ConnectionStateChangeListener() {
            @Override
            public void onChange(int i) {
                Toast.makeText(maze.this, "Connection State = " + i, Toast.LENGTH_SHORT).show();
            }
        });
        checkSdkInfo();

        mConnectButton = findViewById(R.id.ConnectMaze);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSpenRemote.isConnected()) {
                    connectToSpenRemote();
                    mConnectButton.setText("Disconnect");
                } else {
                    disconnectSpenRemote();
                    mConnectButton.setText("Connect");
                    mMotionButton.setText("Start - Motion");
                }
            }
        });

        mMotionButton = findViewById(R.id.getDataMaze);
        mMotionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSpenRemote.isConnected()) {
                    Log.e(TAG, "not connected!");
                    return;
                }

                if (!mIsMotionListening) {
                    SpenUnit airMotionUnit = mSpenUnitManager.getUnit(SpenUnit.TYPE_AIR_MOTION);
                    mSpenUnitManager.registerSpenEventListener(mAirMotionEventListener, airMotionUnit);
                    mMotionButton.setText("Stop - Motion");
                } else {
                    SpenUnit airMotionUnit = mSpenUnitManager.getUnit(SpenUnit.TYPE_AIR_MOTION);
                    mSpenUnitManager.unregisterSpenEventListener(airMotionUnit);
                    mMotionButton.setText("Start - Motion");
                }
                mIsMotionListening = !mIsMotionListening;
            }
        });
    }// end of onCreate
    private void configureBackBtn(){
        Button backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                finish();
            }
        });
    }
    private void checkSdkInfo() {
        Log.d(TAG, "VersionCode=" + mSpenRemote.getVersionCode());
        Log.d(TAG, "versionName=" + mSpenRemote.getVersionName());
        Log.d(TAG, "Support Button = " + mSpenRemote.isFeatureEnabled(SpenRemote.FEATURE_TYPE_BUTTON));
        Log.d(TAG, "Support Air motion = " + mSpenRemote.isFeatureEnabled(SpenRemote.FEATURE_TYPE_AIR_MOTION));
    }

    private void connectToSpenRemote() {
        if (mSpenRemote.isConnected()) {
            Log.d(TAG, "Already Connected!");
            Toast.makeText(this, "Already Connected.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "connectToSpenRemote");

        mSpenRemote.setConnectionStateChangeListener(new SpenRemote.ConnectionStateChangeListener() {
            @Override
            public void onChange(int state) {
                if (state == SpenRemote.State.DISCONNECTED
                        || state == SpenRemote.State.DISCONNECTED_BY_UNKNOWN_REASON) {
                    Toast.makeText(maze.this, "Disconnected : " + state, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSpenRemote.connect(this, mConnectionResultCallback);

        mIsMotionListening = false;
    }

    private void disconnectSpenRemote() {
        if (mSpenRemote != null) {
            mSpenRemote.disconnect(this);
        }
    }

    private SpenRemote.ConnectionResultCallback mConnectionResultCallback = new SpenRemote.ConnectionResultCallback() {
        @Override
        public void onSuccess(SpenUnitManager spenUnitManager) {
            Log.d(TAG, "onConnected");
            Toast.makeText(maze.this, "Connected", Toast.LENGTH_SHORT).show();
            mSpenUnitManager = spenUnitManager;

            SpenUnit buttonUnit = mSpenUnitManager.getUnit(SpenUnit.TYPE_BUTTON);
            mSpenUnitManager.registerSpenEventListener(mButtonEventListener, buttonUnit);
        }

        @Override
        public void onFailure(int i) {
            Log.d(TAG, "onFailure");
            Toast.makeText(maze.this, "Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    private SpenEventListener mButtonEventListener = new SpenEventListener() {
        @Override
        public void onEvent(SpenEvent event) {
            ButtonEvent button = new ButtonEvent(event);

            if (button.getAction() == ButtonEvent.ACTION_DOWN) {
//                drawingImageView.setX(300);
//                drawingImageView.setY(200);
//                posX = 300;
//                posY = 200;
//                Log.v("Cathable", "NoCatch");
                buttonUp = true;
            }
            else if (button.getAction() == ButtonEvent.ACTION_UP) {
                buttonUp = false;
            }
        }
    };

    private SpenEventListener mAirMotionEventListener = new SpenEventListener() {
        @Override
        public void onEvent(SpenEvent event) {
            AirMotionEvent airMotion = new AirMotionEvent(event);
            float deltaX = airMotion.getDeltaX();
            float deltaY = airMotion.getDeltaY();
            if(!buttonUp){
                posX += deltaX*150;
                posY -= deltaY*150;
                drawingImageView.setX(posX);
                drawingImageView.setY(posY);
            }


        }
    };
}