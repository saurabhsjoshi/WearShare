package com.sau.wearshare.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.wearable.view.CircularButton;
import android.view.View;
import android.widget.TextView;

import com.sau.wearshare.R;

import java.util.concurrent.TimeUnit;

/**
 * Created by saurabh on 2015-07-11.
 */
public class CountDownActivity extends Activity {
    private TextView txt_count;
    private CircularButton btn_cancel;

    private static final String FORMAT = "%02d:%02d";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);

        txt_count = (TextView) findViewById(R.id.txt_count);
        btn_cancel = (CircularButton) findViewById(R.id.btn_cancel);

        btn_cancel.setColor(getResources().getColor(R.color.btn_talk_blue));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ((TextView) findViewById(R.id.txt_code)).setText(extras.getString("code"));
        }

        final CountDownTimer timer = new CountDownTimer(600000, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                txt_count.setText(""+
                        String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(
                                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                        ));
            }

            @Override
            public void onFinish() {
                finish();
            }
        };

        timer.start();

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                finish();
            }
        });

    }

}
