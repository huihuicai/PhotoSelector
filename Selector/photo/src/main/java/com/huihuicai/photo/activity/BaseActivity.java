package com.huihuicai.photo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.huihuicai.photo.R;
import com.huihuicai.photo.Util;

/**
 * Created by ybm on 2017/8/4.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton btnLeft;
    private TextView tvTittle;
    private TextView tvRight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentLayout());
        Util.settingStatusBar(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnLeft = (ImageButton) findViewById(R.id.btn_back);
        tvTittle = (TextView) findViewById(R.id.tv_title);
        tvRight = (TextView) findViewById(R.id.tv_right);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    protected abstract int getContentLayout();

    protected ImageButton getLeft() {
        return btnLeft;
    }

    protected TextView getContent() {
        return tvTittle;
    }

    protected TextView getRight() {
        return tvRight;
    }

    protected void setContent(String text) {
        tvTittle.setText(text);
    }

    protected void setContent(int res) {
        tvTittle.setText(res);
    }

    protected void setRightText(String text) {
        tvRight.setText(text);
    }

    protected void setRightText(int res) {
        tvRight.setText(res);
    }

    protected void setOnClick(View.OnClickListener listener) {
        tvRight.setOnClickListener(listener);
    }

}
