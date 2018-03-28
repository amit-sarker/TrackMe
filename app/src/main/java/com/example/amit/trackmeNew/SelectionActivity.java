package com.example.amit.trackmeNew;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import Authentication.DriverSigninActivity;
import Authentication.OwnerSigninActivity;

public class SelectionActivity extends AppCompatActivity {
    private ImageButton mDriver, mOwner;
    private static boolean isDriver = false;

    public static boolean getisDriver() {
        return isDriver;
    }
    public static void setIsDriver(boolean ismDriver){isDriver = ismDriver;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        mDriver = (ImageButton) findViewById(R.id.driver);
        mOwner = (ImageButton) findViewById(R.id.owner);

        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isDriver = true;
                Intent intent = new Intent(SelectionActivity.this, DriverSigninActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectionActivity.this, OwnerSigninActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}
