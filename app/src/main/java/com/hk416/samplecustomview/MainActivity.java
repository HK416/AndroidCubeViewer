package com.hk416.samplecustomview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

public class MainActivity extends AppCompatActivity {

    
    private CustomView upperCubeView;
    private CustomView lowerCubeView;
    private CheckBox upperCubeCheckBox;
    private CheckBox lowerCubeCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        upperCubeView = findViewById(R.id.upperCubeView); 
        lowerCubeView = findViewById(R.id.lowerCubeView);
        upperCubeCheckBox = findViewById(R.id.upperCubeCheckBox);
        lowerCubeCheckBox = findViewById(R.id.lowerCubeCheckBox);
    }

    public void onBtnClockwise(View view) {
        rotateCube((float)Math.toRadians(10.0));
    }

    public void onBtnCounterClockwise(View view) {
        rotateCube((float)Math.toRadians(-10.0));
    }

    private void rotateCube(float radians) {
        if (upperCubeCheckBox.isChecked()) {
            upperCubeView.rotate(radians);
            upperCubeView.invalidate();
        }
        if (lowerCubeCheckBox.isChecked()) {
            lowerCubeView.rotate(radians);
            lowerCubeView.invalidate();
        }
    }
}