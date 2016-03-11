package com.github.nukc.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnOutside).setOnClickListener(this);
        findViewById(R.id.btnInside).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnOutside:
                startActivity(new Intent(this, OutsideActivity.class));
                break;
            case R.id.btnInside:
                startActivity(new Intent(this, InsideActivity.class));
                break;
        }
    }
}
