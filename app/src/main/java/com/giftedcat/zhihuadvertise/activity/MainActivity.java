package com.giftedcat.zhihuadvertise.activity;

import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.giftedcat.zhihuadvertise.R;
import com.giftedcat.zhihuadvertise.config.SourceConfig;
import com.giftedcat.zhihuadvertise.loader.GlideImageLoader;
import com.giftedcat.zhihuadvertise.loader.ImageLoader;
import com.giftedcat.zhihuadvertise.utils.FileUtils;
import com.giftedcat.zhihuadvertise.utils.ImageUtils;
import com.giftedcat.zhihuadvertise.view.TransferImage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifDrawable;

public class MainActivity extends BaseActivity {

    private final String TAG = "MainActivity";

    private final int MAX_COUNT_DOWNTIME = 5;

    Unbinder unbinder;

    @BindView(R.id.iv_test)
    ImageView ivTest;
    @BindView(R.id.ti_advertise)
    TransferImage tiAdvertise;
    @BindView(R.id.tv_jump)
    TextView tvJump;
    @BindView(R.id.tv_skip)
    TextView tvSkip;
    @BindView(R.id.fl_advertise)
    FrameLayout flAdvertise;

    private List<String> advertiseList;
    private String img_url;

    private ImageLoader imageLoader;

    /**
     * 倒计时计数
     */
    private int countdown;

    /**
     * 右上角跳过按钮倒计时显示handler
     */
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            countdown -= 1;
            if (countdown <= 0) {
                showMain();
            } else {
                sendEmptyMessageDelayed(0, 1000);
            }
            tvSkip.setText("跳过 " + countdown);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        setBlackStatusBlackNavigationBar();
        imageLoader = GlideImageLoader.with(getApplicationContext());
        initAdvertiseUrl();
        initView();
    }

    /**
     * 在所有广告列表中随机获取一条
     */
    private void initAdvertiseUrl() {
        advertiseList = SourceConfig.getSourcePicUrlList();
        img_url = advertiseList.get(getRandom(advertiseList.size()));
    }

    private int getRandom(int bound) {
        Random random = new Random();
        return random.nextInt(bound);
    }

    private void initView() {
        loadSourceImage(img_url, ivTest);

        /** 缓存中有广告页的数据才加载广告页*/
        if (imageLoader.getCache(img_url) != null) {
            flAdvertise.setVisibility(View.VISIBLE);
            tiAdvertise.setDuration(300);
            setOriginalInfo();

            File localFile = new File(imageLoader.getCache(img_url).getAbsolutePath());

            if (ImageUtils.getImageType(localFile) == ImageUtils.TYPE_GIF) {
                try {
                    tiAdvertise.setImageDrawable(new GifDrawable(localFile.getPath()));
                } catch (IOException ignored) {
                }
            } else {
                tiAdvertise.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
            }
            tiAdvertise.setOnTransferListener(new TransferImage.OnTransferListener() {
                @Override
                public void onTransferStart(int state, int cate, int stage) {
                }

                @Override
                public void onTransferUpdate(int state, float fraction) {
                }

                @Override
                public void onTransferComplete(int state, int cate, int stage) {
                    flAdvertise.setVisibility(View.GONE);
                }
            });

            countdown = MAX_COUNT_DOWNTIME;
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    /**
     * 显示主界面
     */
    private void showMain() {
        tvJump.setVisibility(View.GONE);
        tvSkip.setVisibility(View.GONE);
        tiAdvertise.transformOut();
    }

    /**
     * 设置TransferImage的缩放相关信息
     */
    private void setOriginalInfo() {
        ivTest.post(new Runnable() {
            @Override
            public void run() {
                int[] location = getViewLocation(ivTest);
                tiAdvertise.setOriginalInfo(location[0], location[1],
                        ivTest.getWidth(), ivTest.getHeight());
            }
        });
    }

    /**
     * 获取 View 在屏幕坐标系中的坐标
     *
     * @param view 需要定位位置的 View
     * @return 坐标系数组
     */
    private int[] getViewLocation(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location;
    }

    @OnClick({R.id.tv_skip, R.id.tv_jump})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_skip:
                /** 跳过*/
                handler.removeMessages(0);
                showMain();
                break;
            case R.id.tv_jump:
                /** 跳转*/
                Toast.makeText(context, "跳转至详情页", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 下载广告图至本地，并加载至首页的ImageView
     * */
    private void loadSourceImage(final String imgUrl, final ImageView targetImage) {
        imageLoader.loadSource(imgUrl, new ImageLoader.SourceCallback() {
            @Override
            public void onStart() {
            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onDelivered(int status, File source) {
                switch (status) {
                    case ImageLoader.STATUS_DISPLAY_SUCCESS:
                        if (ImageUtils.getImageType(source) == ImageUtils.TYPE_GIF) {
                            try {
                                targetImage.setImageDrawable(new GifDrawable(source.getPath()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            targetImage.setImageBitmap(BitmapFactory.decodeFile(source.getAbsolutePath()));
                        }
                        saveCache(imgUrl, source, imageLoader.getCacheDir());
                        break;
                    case ImageLoader.STATUS_DISPLAY_FAILED: // 加载失败，显示加载错误的占位图
                        loadFailedDrawable(targetImage);
                        break;
                }
            }
        });
    }

    /**
     * 根据图片url将图片缓存至本地
     */
    private void saveCache(final String key, final File sourceFile, final File savedDir) {
        final File targetFile = new File(savedDir, FileUtils.getFileName(key));
        if (FileUtils.isFileExists(targetFile)) { // 处理后的图片已经存在
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtils.copyFile(sourceFile, targetFile);
                Log.i(TAG, "save success");
            }
        }).start();
    }


    /**
     * 显示加载失败的图片
     */
    void loadFailedDrawable(ImageView targetImage) {
        Drawable errorDrawable = getResources().getDrawable(R.drawable.ic_empty_photo);
        targetImage.setImageDrawable(errorDrawable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbinder.unbind();
    }

}
