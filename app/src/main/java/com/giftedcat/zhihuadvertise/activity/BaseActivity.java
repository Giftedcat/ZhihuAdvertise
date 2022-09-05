package com.giftedcat.zhihuadvertise.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.giftedcat.zhihuadvertise.R;
import com.gyf.immersionbar.ImmersionBar;

public class BaseActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
    }

    /**
     * 设置黑色状态栏和黑色导航栏
     */
    public void setBlackStatusBlackNavigationBar() {
        ImmersionBar.with(this)
                .fitsSystemWindows(false)
                .transparentStatusBar()
                .navigationBarColor(R.color.white)
                .statusBarDarkFont(false)//设置状态栏字体是否是深色
                .navigationBarDarkIcon(true)
                .init();
    }
}
