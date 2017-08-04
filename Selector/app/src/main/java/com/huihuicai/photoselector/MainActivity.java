package com.huihuicai.photoselector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;

import com.huihuicai.photo.activity.AlbumActivity;
import com.huihuicai.photo.bean.PhotoBean;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_SELECT = 100;
    private Toolbar toolbar;
    private Spinner spinner;
    private CheckBox cbCamera;
    private RecyclerView rvPhoto;
    private PhotoAdapter mAdapter;
    private String mMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        spinner = (Spinner) findViewById(R.id.spinner);
        cbCamera = (CheckBox) findViewById(R.id.cb_use_camera);
        rvPhoto = (RecyclerView) findViewById(R.id.rv_result);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        rvPhoto.setLayoutManager(manager);
        mAdapter = new PhotoAdapter(this);
        rvPhoto.setAdapter(mAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] array = getResources().getStringArray(R.array.spinner);
                mMax = array[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void startSelector(View view) {
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra(AlbumActivity.EXTRA_MAX, mMax);
        intent.putExtra(AlbumActivity.EXTRA_CAMERA, cbCamera.isChecked());
        startActivityForResult(intent, REQUEST_SELECT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_SELECT) {
            List<PhotoBean> result = (List<PhotoBean>) data.getSerializableExtra(AlbumActivity.EXTRA_RESULT);
            if (mAdapter != null) {
                mAdapter.setData(result);
            }
        }
    }

    public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
        private LayoutInflater mInflater;
        private List<PhotoBean> mList;

        public PhotoAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mList = new ArrayList<>();
        }

        private void setData(List<PhotoBean> list) {
            mList.clear();
            if (list != null) {
                mList.addAll(list);
            }
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_preview_photo, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PhotoBean bean = mList.get(position);
            if (bean != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(bean.path);
                holder.ivPhoto.setImageBitmap(bitmap);
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPhoto;

            ViewHolder(View itemView) {
                super(itemView);
                ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            }
        }
    }
}
