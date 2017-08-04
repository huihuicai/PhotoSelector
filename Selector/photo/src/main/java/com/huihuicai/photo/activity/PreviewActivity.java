package com.huihuicai.photo.activity;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.huihuicai.photo.R;
import com.huihuicai.photo.bean.PhotoBean;

import java.util.List;

public class PreviewActivity extends BaseActivity {
    public static final String EXTRA_DATA = "data";
    private List<PhotoBean> mData;
    private int mCount;

    private ViewPager viewPager;
    private ShowAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent("预览");
        setRightText("");
        mData = (List<PhotoBean>) getIntent().getSerializableExtra(EXTRA_DATA);
        mCount = mData == null ? 0 : mData.size();
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new ShowAdapter();
        viewPager.setAdapter(mAdapter);
        initViewPager();
    }

    private void initViewPager() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setContent("预览（" + (position + 1) + "/" + mCount + "）");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_preview;
    }

    public class ShowAdapter extends PagerAdapter {
        PhotoView ivPhoto;

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ivPhoto = new PhotoView(container.getContext());
            ivPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivPhoto.setLayoutParams(new LinearLayout.LayoutParams
                    (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            Glide.with(container.getContext()).load(mData.get(position) == null ? "" : mData.get(position).path).into(ivPhoto);
            container.addView(ivPhoto);
            return ivPhoto;
        }


        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object != null) {
                container.removeView((View) object);
            }
        }
    }
}
