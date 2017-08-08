package com.huihuicai.photo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;

/**
 * Created by ybm on 2017/8/8.
 */

public class ScaleImageView extends AppCompatImageView {
    /**
     * 1.监听手势的事件，判断每次手势，两个手指之间的距离，然后根据中心点来进行位置的缩放
     * 2.拖拽事件，当手指是在拖拽的时候，判断左、上、右、下的边界位置，是不是到达边界
     * 3.缩放回弹，当缩放手势松开之后，执行一个线程，回弹到最大或者最小的位置
     * 4.拖拽回弹，当松开拖拽的手势时，执行一个线程，回弹到原始的位置（拖拽前的位置）
     * 5.缩放和拖拽不能同时进行，需要分开
     */
    private int mMaxWidth, mMaxHeight, mMinWidth, mMinHeight;
    private int mFirstPointId = -1, mSecondPointId = -1;
    private float mFirstPointLastX, mFirstPointLastY;
    private float mSecondPointLastX, mSecondPointLastY;
    private boolean mDragNotScale;
    private Rect mStartRect, mCurrentRect;
    private int mTouchSlop;

    public ScaleImageView(Context context) {
        this(context, null);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bm != null) {
            mMinWidth = bm.getWidth();
            mMaxHeight = bm.getHeight();
            mMaxWidth = 3 * mMinWidth;
            mMaxHeight = 3 * mMinHeight;
        } else {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                boolean init = false;

                @Override
                public void onGlobalLayout() {
                    if (!init) {
                        init = true;
                        mMinWidth = getWidth();
                        mMinHeight = getHeight();
                        mMaxWidth = 3 * mMinWidth;
                        mMaxHeight = 3 * mMinHeight;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mStartRect == null) {
            mStartRect = new Rect(left, top, right, bottom);
            mCurrentRect = new Rect(left, top, right, bottom);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN://第一个手指按下
                mDragNotScale = true;
                int index = event.getActionIndex();
                mFirstPointId = event.getPointerId(index);
                mFirstPointLastX = event.getX(index);
                mFirstPointLastY = event.getY(index);
                return true;
            case MotionEvent.ACTION_POINTER_DOWN://第一个手指按下后，第二个手指按下
                if (mSecondPointId == -1) {
                    index = event.getActionIndex();
                    mSecondPointId = event.getPointerId(index);
                    mSecondPointLastX = event.getX(index);
                    mSecondPointLastY = event.getY(index);
                }
                mDragNotScale = false;
                break;
            case MotionEvent.ACTION_MOVE://手指移动
                if (mFirstPointId == -1 && mSecondPointId == -1) {
                    break;
                }
                Log.e("touch", "当前的状态：" + mDragNotScale);
                if (mDragNotScale) {
                    handleDrag(event);
                } else {
                    handleScale(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP://非最后一个手指抬起
                index = event.getActionIndex();
                int pointerId = event.getPointerId(index);
                if (pointerId == mSecondPointId && mFirstPointId != -1) {
                    mSecondPointId = -1;
                } else if (pointerId == mFirstPointId && mSecondPointId != -1) {
                    mFirstPointId = -1;
                }
                break;
            case MotionEvent.ACTION_UP://最后一个手指抬起
            case MotionEvent.ACTION_CANCEL://取消
                adjustBound();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 处理拖拽事件
     */
    private void handleDrag(MotionEvent e) {
        if (mFirstPointId == -1) {
            return;
        }
        float curX = e.getX(mFirstPointId);
        float curY = e.getY(mFirstPointId);
        int deltaX = (int) (curX - mFirstPointLastX);
        int deltaY = (int) (curY - mFirstPointLastY);
        if (Math.abs(deltaX) < mTouchSlop) {
            deltaX = 0;
        } else {
            deltaX = (int) (deltaX * 0.7);
        }
        if (Math.abs(deltaY) < mTouchSlop) {
            deltaY = 0;
        } else {
            deltaY = (int) (deltaY * 0.7);
        }
        mFirstPointLastX = curX;
        mFirstPointLastY = curY;

        mCurrentRect.left = mCurrentRect.left + deltaX;
        mCurrentRect.top = mCurrentRect.top + deltaY;
        mCurrentRect.right = mCurrentRect.right + deltaX;
        mCurrentRect.bottom = mCurrentRect.bottom + deltaY;
        this.setFrame(mCurrentRect.left, mCurrentRect.top, mCurrentRect.right, mCurrentRect.bottom);
    }

    /**
     * 处理缩放事件
     */
    private void handleScale(MotionEvent e) {
        if (mFirstPointId == -1 || mSecondPointId == -1) {
            return;
        }
        float firstX = e.getX(mFirstPointId);
        float firstY = e.getY(mFirstPointId);
        float secondX = e.getX(mSecondPointId);
        float secondY = e.getY(mSecondPointId);
        float middleX = firstX + (secondX - firstX) / 2;
        float middleY = firstY + (secondY - firstY) / 2;
        float rateX = (middleX - mCurrentRect.left) / (mCurrentRect.right - mCurrentRect.left);
        float rateY = (middleY - mCurrentRect.top) / (mCurrentRect.bottom - mCurrentRect.top);

        //deltaDistance>0放大，反之缩小
        double deltaDistance = Math.sqrt(Math.pow(firstX - secondX, 2) + Math.pow(firstY - secondY, 2))
                - Math.sqrt(Math.pow(mFirstPointLastX - mSecondPointLastX, 2) + Math.pow(mFirstPointLastY - mSecondPointLastY, 2));

        mFirstPointLastX = firstX;
        mFirstPointLastY = firstY;
        mSecondPointLastX = secondX;
        mSecondPointLastY = secondY;

        mCurrentRect.left -= deltaDistance * rateX;
        mCurrentRect.top -= deltaDistance * rateY;
        mCurrentRect.right += deltaDistance * (1 - rateX);
        mCurrentRect.bottom += deltaDistance * (1 - rateY);

        this.setFrame(mCurrentRect.left, mCurrentRect.top, mCurrentRect.right, mCurrentRect.bottom);
    }

    private void adjustBound() {
        if (mDragNotScale) {
            this.post(new DragReback(1000));
        } else {

        }
    }

    private class DragReback implements Runnable {
        private boolean mFinished;
        private int mDuration;
        private int mAdjustL, mAdjustT, mAdjustR, mAdjustB;

        private Interpolator mInter = new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return (float) Math.pow(1 - input, 5);
            }
        };

        public DragReback(int duration) {
            mDuration = duration > 2000 ? 2000 : duration;
            if (mCurrentRect.left > mStartRect.left) {
                mAdjustL = mStartRect.left - mCurrentRect.left;
            }
            if (mCurrentRect.top > mStartRect.top) {
                mAdjustT = mStartRect.top - mCurrentRect.top;
            }
            if (mCurrentRect.right < mStartRect.right) {
                mAdjustR = mStartRect.right - mCurrentRect.right;
            }
            if (mCurrentRect.bottom < mStartRect.bottom) {
                mAdjustB = mStartRect.bottom - mCurrentRect.bottom;
            }
        }

        public boolean isFinished() {
            return mFinished;
        }

        @Override
        public void run() {
            if (mAdjustL == 0 && mAdjustT == 0 && mAdjustR == 0 && mAdjustB == 0) {
                return;
            }
            mFinished = false;
            long start = System.currentTimeMillis();
            while (!mFinished) {
                float param = 1f * (System.currentTimeMillis() - start) / mDuration;
                float rate = mInter.getInterpolation(param);
                if (mAdjustL != 0) {
                    mCurrentRect.left += mAdjustL * rate;
                }
                if (mAdjustT != 0) {
                    mCurrentRect.top += mAdjustT * rate;
                }
                if (mAdjustR != 0) {
                    mCurrentRect.right += mAdjustR * rate;
                }
                if (mAdjustB != 0) {
                    mCurrentRect.bottom += mAdjustB * rate;
                }
                setFrame(mCurrentRect.left, mCurrentRect.top, mCurrentRect.right, mCurrentRect.bottom);
                if (mCurrentRect.left <= mStartRect.left && mCurrentRect.top <= mStartRect.top
                        && mCurrentRect.right >= mStartRect.right && mCurrentRect.bottom >= mStartRect.bottom) {
                    mFinished = true;
                }
            }
        }
    }
}
