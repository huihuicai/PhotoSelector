package com.huihuicai.photo.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huihuicai.photo.R;
import com.huihuicai.photo.bean.FolderBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ybm on 2017/8/3.
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.Holder> {

    private LayoutInflater mInflater;
    private List<FolderBean> mList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position,String content);
    }

    public FolderAdapter(Context context, List<FolderBean> list) {
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();
        if (list != null) {
            mList.addAll(list);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_folder, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(final Holder holder, int position) {
        final FolderBean folder = mList.get(position);
        if (folder != null) {
            holder.tvName.setText(folder.name);
            holder.ivIcon.setImageBitmap(BitmapFactory.decodeFile(folder.cover));
            String count = folder.photos != null ? folder.photos.size() + "" : "0";
            holder.tvCount.setText(count);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(holder.getAdapterPosition(),folder.name);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;
        TextView tvCount;

        public Holder(View itemView) {
            super(itemView);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvCount = (TextView) itemView.findViewById(R.id.tv_count);
        }
    }
}
