package com.sau.wearshare.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.sau.wearshare.R;

/**
 * Created by saurabh on 2015-08-02.
 */
public class TwoButtonActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_button);
        final String filename = getIntent().getExtras().getString("filename");

        (findViewById(R.id.btn_open)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("filename", filename);
                setResult(11, returnIntent);
                finish();
            }
        });

        (findViewById(R.id.btn_add)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("filename", filename);
                setResult(99, returnIntent);
                finish();
            }
        });
    }

}
