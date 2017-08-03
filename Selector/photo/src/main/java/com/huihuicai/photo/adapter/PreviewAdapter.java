package com.huihuicai.photo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.huihuicai.photo.R;
import com.huihuicai.photo.bean.PhotoBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ybm on 2017/8/2.
 */

public class PreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener {
    private final int TYPE_CAMERA = 0;
    private final int TYPE_PHOTO = 1;

    private Context mContext;
    private LayoutInflater mInflater;
    private List<PhotoBean> mList;
    private SparseArray<PhotoBean> mSelected;
    private boolean mUseCamera;
    private int mMaxPiece;

    public PreviewAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
        mSelected = new SparseArray<>();
        mUseCamera = true;
        mMaxPiece = 1;
    }

    public PreviewAdapter(Context context, int max, boolean camera) {
        this(context);
        mMaxPiece = max;
        mUseCamera = camera;
    }

    public void setData(List<PhotoBean> list) {
        mList.clear();
        if (list != null) {
            mList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public List<PhotoBean> getSelected() {
        if (mSelected.size() <= 0) {
            return null;
        }
        List<PhotoBean> data = new ArrayList<>();
        for (int i = 0; i < mSelected.size(); i++) {
            data.add(mSelected.get(i));
        }
        return data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_CAMERA) {
            View view = mInflater.inflate(R.layout.item_camera, parent, false);
            return new CameraHolder(view);
        } else {
            View view = mInflater.inflate(R.layout.item_preview, parent, false);
            return new PhotoHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int realPosition = mUseCamera ? position - 1 : position;
        if (mUseCamera && position == 0) {
            ((CameraHolder) holder).root.setTag(holder);
            ((CameraHolder) holder).root.setOnClickListener(null);
            ((CameraHolder) holder).root.setOnClickListener(this);
        } else {
            PhotoBean bean = mList.get(realPosition);
            if (bean != null) {
                PhotoBean value = mSelected.get(realPosition);
                ((PhotoHolder) holder).marker.setVisibility(value == null ? View.GONE : View.VISIBLE);
                ((PhotoHolder) holder).ivSelect.setImageResource(value == null ?
                        R.drawable.checkbox_normal : R.drawable.checkbox_select);
                Glide.with(mContext).load(bean.path).into(((PhotoHolder) holder).ivPhoto);
                ((PhotoHolder) holder).root.setTag(holder);
                ((PhotoHolder) holder).root.setOnClickListener(null);
                ((PhotoHolder) holder).root.setOnClickListener(this);

            }
        }
    }

    @Override
    public int getItemCount() {
        return mUseCamera ? mList.size() + 1 : mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mUseCamera) {
            return position == 0 ? TYPE_CAMERA : TYPE_PHOTO;
        } else {
            return TYPE_PHOTO;
        }
    }

    @Override
    public void onClick(View v) {
        RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) v.getTag();
        int position = holder.getAdapterPosition();
        if (mUseCamera && position == 0) {
            // TODO: 2017/8/2 调到拍照
        } else {
            int realPosition = mUseCamera ? position - 1 : position;
            PhotoHolder photoHolder = (PhotoHolder) v.getTag();
            if (mMaxPiece <= mSelected.size()) {
                PhotoBean value = mSelected.get(realPosition);
                if (value != null) {
                    photoHolder.marker.setVisibility(View.GONE);
                    photoHolder.ivSelect.setImageResource(R.drawable.checkbox_normal);
                    mSelected.remove(realPosition);
                }
                return;
            }
            if (photoHolder == null) {
                return;
            }
            if (mList == null || realPosition >= mList.size()) {
                return;
            }
            PhotoBean value = mSelected.get(realPosition);
            //之前的保存的有，本次就去掉；反之保存的没有，就加上
            photoHolder.marker.setVisibility(value == null ? View.VISIBLE : View.GONE);
            photoHolder.ivSelect.setImageResource(value == null ?
                    R.drawable.checkbox_select : R.drawable.checkbox_normal);
            if (value == null) {
                mSelected.put(realPosition, mList.get(realPosition));
            } else {
                mSelected.remove(realPosition);
            }
        }
    }

    class PhotoHolder extends RecyclerView.ViewHolder {

        ImageView ivPhoto;
        ImageView ivSelect;
        View root;
        View marker;

        PhotoHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            ivSelect = (ImageView) itemView.findViewById(R.id.iv_select);
            root = itemView.findViewById(R.id.root);
            marker = itemView.findViewById(R.id.view_marker);
        }
    }

    class CameraHolder extends RecyclerView.ViewHolder {

        View root;

        public CameraHolder(View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
        }
    }
}
