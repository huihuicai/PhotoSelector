package com.huihuicai.photo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ViewTreeObserver;

import com.huihuicai.photo.R;
import com.huihuicai.photo.view.DragImageView;

public class CropActivity extends BaseActivity {
    public static final String EXTRA_PATH = "photo_path";
    private DragImageView ivPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent("图片裁剪");
        ivPhoto = (DragImageView) findViewById(R.id.iv_photo);
        String path = getIntent().getStringExtra(EXTRA_PATH);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if (bitmap != null) {
            ivPhoto.setImageBitmap(bitmap);
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_crop;
    }
}
