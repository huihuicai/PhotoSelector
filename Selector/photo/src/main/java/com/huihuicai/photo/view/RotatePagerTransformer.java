package com.huihuicai.photo.view;

import android.animation.TimeInterpolator;
import android.support.v4.view.ViewPager;
import android.view.View;

import static java.lang.Math.pow;

public class RotatePagerTransformer implements ViewPager.PageTransformer {
    private static final float MAX_ROTATE_Y = 90;
    private final float pageWidth;

    private static final TimeInterpolator sInterpolator = new TimeInterpolator() {
        @Override
        public float getInterpolation(float input) {
            if (input < 0.7) {
                return input * (float) pow(0.7, 3) * MAX_ROTATE_Y;
            } else {
                return (float) pow(input, 4) * MAX_ROTATE_Y;
            }
        }
    };

    public RotatePagerTransformer(float pageWidth) {
        this.pageWidth = pageWidth;
    }

    public void transformPage(View view, float position) {
        view.setPivotY(view.getHeight() / 2);
        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setPivotX(0);
            view.setRotationY(90);
        } else if (position <= 0) { // [-1,0]
            view.setPivotX(pageWidth);
            view.setRotationY(-sInterpolator.getInterpolation(-position));
        } else if (position <= 1) { // (0,1]
            view.setPivotX(0);
            view.setRotationY(sInterpolator.getInterpolation(position));
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setPivotX(0);
            view.setRotationY(90);
        }
    }
}