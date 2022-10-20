package com.giftedcat.zhihuadvertise.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * TransferImage 可以完成从大图平滑缩放到ImageView
 */
public class TransferImage extends AppCompatImageView {

    private boolean isTransOut;

    private int originalWidth;
    private int originalHeight;
    private int originalLocationX;
    private int originalLocationY;
    private long duration = 300; // 默认动画时长
    private boolean transformStart = false; // 开始动画的标记

    private Matrix transMatrix;

    private Transform transform;

    public TransferImage(Context context) {
        this(context, null);
    }

    public TransferImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TransferImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setScaleType(ScaleType.CENTER_CROP);
        init();
    }

    private void init() {
        transMatrix = new Matrix();
    }

    /**
     * 设置 TransferImage 初始位置信息
     *
     * @param locationX x坐标位置
     * @param locationY y坐标位置
     * @param width     宽度
     * @param height    高度
     */
    public void setOriginalInfo(int locationX, int locationY, int width, int height) {
        originalLocationX = locationX;
        originalLocationY = locationY;
        originalWidth = width;
        originalHeight = height;
    }

    /**
     * 用于开始退出的方法。 调用此方前，需已经调用过setOriginalInfo
     */
    public void transformOut() {
        isTransOut = true;
        transformStart = true;
        invalidate();
    }

    /**
     * 获取伸缩动画执行的时间
     *
     * @return unit ：毫秒
     */
    public long getDuration() {
        return duration;
    }

    /**
     * 设置伸缩动画执行的时间
     *
     * @param duration unit ：毫秒
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) return;

        if (!isTransOut) {
            super.onDraw(canvas);
        } else {
            if (transformStart) {
                initTransform();
                transform.initStart();
            }

            int saveCount = canvas.getSaveCount();
            canvas.save();
            // 先得到图片在此刻的图像Matrix矩阵
            calcBmpMatrix();
            canvas.translate(transform.rect.left, transform.rect.top);
            canvas.clipRect(0, 0, transform.rect.width, transform.rect.height);
            canvas.concat(transMatrix);
            getDrawable().draw(canvas);
            canvas.restoreToCount(saveCount);
            if (transformStart) {
                transformStart = false;

                startTrans();
            }
        }
    }

    /**
     * 初始化进入的变量信息
     */
    private void initTransform() {
        Drawable transDrawable = getDrawable();
        if (transDrawable == null) return;
        if (getWidth() == 0 || getHeight() == 0) return;

        transform = new Transform();

        /** 计算初始的缩放值*/
        float xSScale = getWidth() / ((float) transDrawable.getIntrinsicWidth());
        float ySScale = getHeight() / ((float) transDrawable.getIntrinsicHeight());
        float startScale = Math.max(xSScale, ySScale);
        transform.startScale = startScale;
        /** 计算结束时候的缩放值*/
        float xEScale = originalWidth / ((float) transDrawable.getIntrinsicWidth());
        float yEScale = originalHeight / ((float) transDrawable.getIntrinsicHeight());
        float endScale = Math.max(xEScale, yEScale);
        transform.endScale = endScale;

        /** 开始区域 */
        transform.startRect = new LocationSizeF();
        float bitmapStartWidth = transDrawable.getIntrinsicWidth() * transform.startScale;// 图片开始的宽度
        float bitmapStartHeight = transDrawable.getIntrinsicHeight() * transform.startScale;// 图片开始的高度
        transform.startRect.left = (getWidth() - bitmapStartWidth) / 2;
        transform.startRect.top = (getHeight() - bitmapStartHeight) / 2;
        transform.startRect.width = bitmapStartWidth;
        transform.startRect.height = bitmapStartHeight;
        /** 结束区域 */
        transform.endRect = new LocationSizeF();
        transform.endRect.left = originalLocationX;
        transform.endRect.top = originalLocationY;
        transform.endRect.width = originalWidth;
        transform.endRect.height = originalHeight;

        transform.rect = new LocationSizeF();
    }

    private void calcBmpMatrix() {
        Drawable transDrawable = getDrawable();
        if (transDrawable == null || transform == null) return;

        transMatrix.setScale(transform.scale, transform.scale);
        transMatrix.postTranslate(-(transform.scale * transDrawable.getIntrinsicWidth() / 2 - transform.rect.width / 2),
                -(transform.scale * transDrawable.getIntrinsicHeight() / 2 - transform.rect.height / 2));
    }

    private void startTrans() {
        if (transform == null) return;

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        PropertyValuesHolder scaleHolder = PropertyValuesHolder.ofFloat("scale", transform.startScale, transform.endScale);
        PropertyValuesHolder leftHolder = PropertyValuesHolder.ofFloat("left", transform.startRect.left, transform.endRect.left);
        PropertyValuesHolder topHolder = PropertyValuesHolder.ofFloat("top", transform.startRect.top, transform.endRect.top);
        PropertyValuesHolder widthHolder = PropertyValuesHolder.ofFloat("width", transform.startRect.width, transform.endRect.width);
        PropertyValuesHolder heightHolder = PropertyValuesHolder.ofFloat("height", transform.startRect.height, transform.endRect.height);
        valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public synchronized void onAnimationUpdate(ValueAnimator animation) {
                transform.scale = (Float) animation.getAnimatedValue("scale");
                transform.rect.left = (Float) animation.getAnimatedValue("left");
                transform.rect.top = (Float) animation.getAnimatedValue("top");
                transform.rect.width = (Float) animation.getAnimatedValue("width");
                transform.rect.height = (Float) animation.getAnimatedValue("height");
                invalidate();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(GONE);
            }
        });

        valueAnimator.start();

    }

    private class Transform {
        float startScale;// 图片开始的缩放值
        float endScale;// 图片结束的缩放值
        float scale;// 属性ValueAnimator计算出来的值
        LocationSizeF startRect;// 开始的区域
        LocationSizeF endRect;// 结束的区域
        LocationSizeF rect;// 属性ValueAnimator计算出来的值

        void initStart() {
            scale = startScale;
            try {
                rect = (LocationSizeF) startRect.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

    }

    private class LocationSizeF implements Cloneable {
        float left;
        float top;
        float width;
        float height;

        @Override
        public String toString() {
            return "[left:" + left + " top:" + top + " width:" + width + " height:" + height + "]";
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

}
