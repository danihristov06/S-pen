package com.spen.spenstuff;

import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.samsung.android.sdk.penremote.AirMotionEvent;
import com.samsung.android.sdk.penremote.ButtonEvent;
import com.samsung.android.sdk.penremote.SpenEvent;
import com.samsung.android.sdk.penremote.SpenEventListener;
import com.samsung.android.sdk.penremote.SpenRemote;
import com.samsung.android.sdk.penremote.SpenUnit;
import com.samsung.android.sdk.penremote.SpenUnitManager;

import java.lang.reflect.Constructor;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "SpenRemoteSample";
    private TextView mButtonState;
    private TextView mAirMotion;

    private Button mConnectButton;
    private Button mMotionButton;
    private SpenRemote mSpenRemote;
    private SpenUnitManager mSpenUnitManager;
    private boolean mIsMotionListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonState = findViewById(R.id.buttonState);
        mAirMotion = findViewById(R.id.AirView);

        mSpenRemote = SpenRemote.getInstance();
        mSpenRemote.setConnectionStateChangeListener(new SpenRemote.ConnectionStateChangeListener() {
            @Override
            public void onChange(int i) {
                Toast.makeText(MainActivity.this, "Connection State = " + i, Toast.LENGTH_SHORT).show();
            }
        });
        checkSdkInfo();

        mConnectButton = findViewById(R.id.Connect);
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

        mMotionButton = findViewById(R.id.getData);
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
    }//end OnCreate

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
                    Toast.makeText(MainActivity.this, "Disconnected : " + state, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
            mSpenUnitManager = spenUnitManager;

            SpenUnit buttonUnit = mSpenUnitManager.getUnit(SpenUnit.TYPE_BUTTON);
            mSpenUnitManager.registerSpenEventListener(mButtonEventListener, buttonUnit);
        }

        @Override
        public void onFailure(int i) {
            Log.d(TAG, "onFailure");
            Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
        }
    };

    private SpenEventListener mButtonEventListener = new SpenEventListener() {
        @Override
        public void onEvent(SpenEvent event) {
            ButtonEvent button = new ButtonEvent(event);

            if (button.getAction() == ButtonEvent.ACTION_DOWN) {
                mButtonState.setText("BUTTON : Pressed");
            } else if (button.getAction() == ButtonEvent.ACTION_UP) {
                mButtonState.setText("BUTTON : Released");
            }
        }
    };

    private SpenEventListener mAirMotionEventListener = new SpenEventListener() {
        @Override
        public void onEvent(SpenEvent event) {
            AirMotionEvent airMotion = new AirMotionEvent(event);
            float deltaX = airMotion.getDeltaX();
            float deltaY = airMotion.getDeltaY();
            mAirMotion.setText("" + deltaX + ", " + deltaY);
        }
    };
}