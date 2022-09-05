package com.giftedcat.zhihuadvertise.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giftedcat.zhihuadvertise.R;

public class SplashActivity extends BaseActivity {

    Handler jumpHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            startNextActivity();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        jumpHandler.sendEmptyMessageDelayed(0, 1000);
    }


    private void startNextActivity(){
        startActivity(new Intent(context, MainActivity.class));
        SplashActivity.this.finish();
    }
}
