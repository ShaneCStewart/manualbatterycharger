package com.example.manualphonecharger.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.manualphonecharger.app.customview.PowerGaugeView;
import com.example.manualphonecharger.app.R;


public class ChargingActivity extends Activity {
    private PowerGaugeView powerGaugeView;
    private TextView motionValueTextView;
    private Button changeMotionStateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charging);
        init();
    }

    private void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        powerGaugeView = (PowerGaugeView) findViewById(R.id.power_gauge_view);
        motionValueTextView = (TextView) findViewById(R.id.motion_state_text_view);
        setMotionValueText();
        changeMotionStateButton = (Button) findViewById(R.id.switch_motion_button);
        changeMotionStateButton.setOnClickListener(changeMotionStateClickListener);
    }

    private void setMotionValueText() {
        String motionState = powerGaugeView.getMotionState();
        if(motionState.equals(PowerGaugeView.RATE_OF_MOTION_STATE)){
            motionValueTextView.setText(R.string.rate_of_motion_state);

        } else if(motionState.equals(PowerGaugeView.TOTAL_MOTION_STATE)){
            motionValueTextView.setText(R.string.total_motion_state);
        }
    }

    private View.OnClickListener changeMotionStateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeMotionStateButton.setEnabled(false);
            powerGaugeView.flipMotionState();
            setMotionValueText();
            changeMotionStateButton.setEnabled(true);
        }
    };
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        powerGaugeView.setMotionState(savedInstanceState
                .getString(PowerGaugeView.CURRENT_MOTION_STATE_TAG));
        setMotionValueText();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(PowerGaugeView.CURRENT_MOTION_STATE_TAG,
                powerGaugeView.getMotionState());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume(){
        super.onResume();
        powerGaugeView.attachToSensor();
    }

    @Override
    public void onPause(){
        super.onPause();
        powerGaugeView.detachFromSensor();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
