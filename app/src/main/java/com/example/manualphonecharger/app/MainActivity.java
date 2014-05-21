package com.example.manualphonecharger.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import com.example.manualphonecharger.app.activity.ChargingActivity;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initStartButton();
    }

    private void initStartButton() {
        Button startChargingButton = (Button)findViewById(R.id.start_charging_button);
        startChargingButton.setOnClickListener(startChargingClickListener);
    }

    private View.OnClickListener startChargingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            createChargingActivity();
        }
    };

    private void createChargingActivity() {
        Intent chargingActivityIntent = new Intent(this, ChargingActivity.class);
        startActivity(chargingActivityIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
