package com.huihuicai.photo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

public class DragImageView extends AppCompatImageView {

    private int mScreenW, mScreenH;// 可见屏幕的宽高度
    private int MAX_W, MAX_H, MIN_W, MIN_H;// 极限值
    private int mStartT = -1, mStartR = -1, mStartB = -1, mStartL = -1;// 初始化默认位置.
    private int mStartX, mStartY, mCurrentX, mCurrentY;// 触摸位置
    private float mBeforeLength, mAfterLength;// 两触点距离
    private boolean isControlV = false;// 垂直监控
    private boolean isControlH = false;// 水平监控
    private boolean isScaleAnim = false;//是否动画
    private MODE mode = MODE.NONE;// 默认模式

    private enum MODE {
        //NONE：无 DRAG：拖拽. ZOOM:缩放
        NONE, DRAG, ZOOM
    }

    public DragImageView(Context context) {
        this(context, null);
    }

    public DragImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mScreenW = getWidth();
                mScreenH = getHeight();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        int width = bm.getWidth();
        int height = bm.getHeight();
        MAX_W = width * 3;
        MAX_H = height * 3;
        MIN_W = width / 2;
        MIN_H = height / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mStartT == -1) {
            mStartL = left;
            mStartT = top;
            mStartR = right;
            mStartB = bottom;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                onPointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                mode = MODE.NONE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE.NONE;
                if (isScaleAnim) {
                    doScaleAnim();
                }
                break;
        }
        return true;
    }

    private void onTouchDown(MotionEvent event) {
        mode = MODE.DRAG;
        mCurrentX = (int) event.getRawX();
        mCurrentY = (int) event.getRawY();
        mStartX = (int) event.getX();
        mStartY = mCurrentY - this.getTop();
    }

    private void onPointerDown(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            mode = MODE.ZOOM;
            mBeforeLength = getDistance(event);
        }
    }

    private void onTouchMove(MotionEvent event) {
        int left, top, right, bottom;
        if (mode == MODE.DRAG) {
            left = mCurrentX - mStartX;
            right = mCurrentX + this.getWidth() - mStartX;
            top = mCurrentY - mStartY;
            bottom = mCurrentY - mStartY + this.getHeight();
            if (isControlH) {
                if (left >= 0) {
                    left = 0;
                    right = this.getWidth();
                }
                if (right <= mScreenW) {
                    left = mScreenW - this.getWidth();
                    right = mScreenW;
                }
            } else {
                left = this.getLeft();
                right = this.getRight();
            }
            if (isControlV) {
                if (top >= 0) {
                    top = 0;
                    bottom = this.getHeight();
                }
                if (bottom <= mScreenH) {
                    top = mScreenH - this.getHeight();
                    bottom = mScreenH;
                }
            } else {
                top = this.getTop();
                bottom = this.getBottom();
            }
            if (isControlH || isControlV) {
                this.setPosition(left, top, right, bottom);
            }
            mCurrentX = (int) event.getRawX();
            mCurrentY = (int) event.getRawY();

        } else if (mode == MODE.ZOOM) {
            mAfterLength = getDistance(event);
            float gapLength = mAfterLength - mBeforeLength;
            if (Math.abs(gapLength) > 5f) {
                float scale = mAfterLength / mBeforeLength;
                this.setScale(scale);
                mBeforeLength = mAfterLength;
            }
        }
    }

    /**
     * 获取两个之间的距离
     **/
    float getDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 拖拽
     **/
    private void setPosition(int left, int top, int right, int bottom) {
        this.layout(left, top, right, bottom);
    }

    /**
     * 缩放
     **/
    private void setScale(float scale) {
        int disX = (int) (this.getWidth() * Math.abs(1 - scale)) / 4;
        int disY = (int) (this.getHeight() * Math.abs(1 - scale)) / 4;
        int currentT, currentR, currentB, currentL;
        if (scale > 1 && this.getWidth() <= MAX_W) {
            currentL = this.getLeft() - disX;
            currentT = this.getTop() - disY;
            currentR = this.getRight() + disX;
            currentB = this.getBottom() + disY;

            this.setFrame(currentL, currentT, currentR, currentB);

            if (currentT <= 0 && currentB >= mScreenH) {
                isControlV = true;
            } else {
                isControlV = false;
            }
            if (currentL <= 0 && currentR >= mScreenW) {
                isControlH = true;
            } else {
                isControlH = false;
            }
        } else if (scale < 1 && this.getWidth() >= MIN_W) {
            currentL = this.getLeft() + disX;
            currentT = this.getTop() + disY;
            currentR = this.getRight() - disX;
            currentB = this.getBottom() - disY;
            if (isControlV && currentT > 0) {
                currentT = 0;
                currentB = this.getBottom() - 2 * disY;
                if (currentB < mScreenH) {
                    currentB = mScreenH;
                    isControlV = false;
                }
            }
            if (isControlV && currentB < mScreenH) {
                currentB = mScreenH;
                currentT = this.getTop() + 2 * disY;
                if (currentT > 0) {
                    currentT = 0;
                    isControlV = false;
                }
            }

            if (isControlH && currentL >= 0) {
                currentL = 0;
                currentR = this.getRight() - 2 * disX;
                if (currentR <= mScreenW) {
                    currentR = mScreenW;
                    isControlH = false;
                }
            }
            if (isControlH && currentR <= mScreenW) {
                currentR = mScreenW;
                currentL = this.getLeft() + 2 * disX;
                if (currentL >= 0) {
                    currentL = 0;
                    isControlH = false;
                }
            }

            if (isControlH || isControlV) {
                this.setFrame(currentL, currentT, currentR, currentB);
            } else {
                this.setFrame(currentL, currentT, currentR, currentB);
                isScaleAnim = true;
            }

        }

    }

    public void doScaleAnim() {
        MyAsyncTask scaleTask = new MyAsyncTask(mScreenW, this.getWidth(), this.getHeight());
        scaleTask.setLTRB(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
        scaleTask.execute();
        isScaleAnim = false;
    }

    private class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        private int screen_W, current_Width, current_Height;
        private int left, top, right, bottom;
        private float scale_WH;
        private float STEP = 8f;
        private float step_H, step_V;
        private final int UPDATE = 1;
        private Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == UPDATE) {
                    onProgressUpdate(left, top, right, bottom);
                }
            }
        };

        public void setLTRB(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public MyAsyncTask(int screen_W, int current_Width, int current_Height) {
            super();
            this.screen_W = screen_W;
            this.current_Width = current_Width;
            this.current_Height = current_Height;
            scale_WH = (float) current_Height / current_Width;
            step_H = STEP;
            step_V = scale_WH * STEP;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (current_Width <= screen_W) {
                left -= step_H;
                top -= step_V;
                right += step_H;
                bottom += step_V;
                current_Width += 2 * step_H;
                left = Math.max(left, mStartL);
                top = Math.max(top, mStartT);
                right = Math.min(right, mStartR);
                bottom = Math.min(bottom, mStartB);
                mHandler.sendEmptyMessageDelayed(UPDATE, 10);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(final Integer... values) {
            super.onProgressUpdate(values);
            post(new Runnable() {
                @Override
                public void run() {
                    setFrame(values[0], values[1], values[2], values[3]);
                }
            });
        }
    }

}
