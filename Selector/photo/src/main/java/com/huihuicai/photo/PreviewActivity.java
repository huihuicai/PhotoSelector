package com.huihuicai.photo;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.huihuicai.photo.adapter.FolderAdapter;
import com.huihuicai.photo.adapter.PreviewAdapter;
import com.huihuicai.photo.bean.FolderBean;
import com.huihuicai.photo.bean.PhotoBean;
import com.huihuicai.photo.view.DividerGridDecoration;
import com.huihuicai.photo.view.StrongBottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_MAX = "max_piece";
    public static final String EXTRA_CAMERA = "use_camera";
    public static final String EXTRA_RESULT = "result";

    public static final int LOADER_ALL = 0;
    public static final int LOADER_OTHER = 1;

    private Toolbar toolbar;
    private ImageButton btnBack;
    private TextView tvFinish;
    private TextView tvCategory;
    private TextView tvPreview;
    private RecyclerView rvPhoto;
    private StrongBottomSheetDialog mFolderDialog;

    private int mMaxPiece;
    private boolean mUseCamera;
    private PreviewAdapter mAdapter;
    private List<FolderBean> mFolderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Util.settingStatusBar(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        btnBack = (ImageButton) findViewById(R.id.btn_back);
        tvFinish = (TextView) findViewById(R.id.tv_finish);
        tvCategory = (TextView) findViewById(R.id.tv_category);
        tvPreview = (TextView) findViewById(R.id.tv_preview);
        rvPhoto = (RecyclerView) findViewById(R.id.rv_photo);
        btnBack.setOnClickListener(this);
        tvFinish.setOnClickListener(this);
        tvCategory.setOnClickListener(this);
        tvPreview.setOnClickListener(this);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        initData();
    }

    private void initData() {
        String piece = getIntent().getStringExtra(EXTRA_MAX);
        mUseCamera = getIntent().getBooleanExtra(EXTRA_CAMERA, false);
        mMaxPiece = TextUtils.isEmpty(piece) ? 1 : Integer.parseInt(piece);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        rvPhoto.setLayoutManager(manager);
        mAdapter = new PreviewAdapter(this, mMaxPiece, mUseCamera);
        rvPhoto.setAdapter(mAdapter);
        tvCategory.setText("所有");
//        rvPhoto.addItemDecoration(new DividerGridDecoration(10, 0xfffff));
        rvPhoto.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.left = 4;
                outRect.top = 4;
                outRect.right = 4;
                outRect.bottom = 4;
            }
        });
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(LOADER_ALL, null, mLoaderCallback);
    }

    @Override
    public void onClick(View v) {
        if (v == btnBack) {
            finish();
        } else if (v == tvFinish) {
            if (mAdapter != null && mAdapter.getSelected() != null) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT, (Parcelable) mAdapter.getSelected());
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if (v == tvCategory) {
            showFolderList();
        } else if (v == tvPreview) {
            //切换到预览页面
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
        String[] PROJECTION = new String[]{
                MediaStore.Images.Media._ID,//0
                MediaStore.Images.Media.DATA,//1
                MediaStore.Images.Media.DISPLAY_NAME,//2
                MediaStore.Images.Media.DATE_ADDED,//3
                MediaStore.Images.Media.MIME_TYPE,//4
                MediaStore.Images.Media.SIZE//5
        };

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                return new CursorLoader(PreviewActivity.this,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        PROJECTION,
                        PROJECTION[4] + "=? or " + PROJECTION[4] + "=? and " + PROJECTION[5] + ">0",
                        new String[]{"image/jpeg", "image/png"},
                        PROJECTION[4] + " desc");
            } else if (id == LOADER_OTHER) {
                String folder = args.getString("folder");
                return new CursorLoader(PreviewActivity.this,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        PROJECTION,
                        PROJECTION[1] + " like %" + folder + "% and " + PROJECTION[5] + ">0",
                        null,
                        PROJECTION[4] + " desc");
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                List<PhotoBean> allPhoto = new ArrayList<>();
                PhotoBean item;
                FolderBean folder;
                File itemFile;
                int nameIndex = data.getColumnIndex(PROJECTION[2]);
                int pathIndex = data.getColumnIndex(PROJECTION[1]);
                while (data.moveToNext()) {
                    item = new PhotoBean();
                    item.name = data.getString(nameIndex);
                    item.path = data.getString(pathIndex);
                    itemFile = new File(item.path);
                    String parent = getFolder(itemFile);
                    item.parent = parent;
                    allPhoto.add(item);
                    //把文件加入到相对应的folder中去
                    String folderName = TextUtils.isEmpty(parent) ? "" : itemFile.getParentFile().getName();
                    folder = findFolder(parent, item.path, folderName);
                    if (folder != null) {
                        folder.photos.add(item);
                    }
                }
                if (mAdapter != null) {
                    mAdapter.setData(allPhoto);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private String getFolder(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        return file.getParent();
    }

    public FolderBean findFolder(String folderPath, String cover, String name) {
        if (TextUtils.isEmpty(folderPath)) {
            return null;
        }
        FolderBean folderBean = null;
        for (int i = 0, len = mFolderList.size(); i < len; i++) {
            if (TextUtils.equals(folderPath, mFolderList.get(i).path)) {
                folderBean = mFolderList.get(i);
                break;
            }
        }
        if (folderBean == null) {
            FolderBean folder = new FolderBean();
            folder.path = folderPath;
            folder.cover = cover;
            folder.name = name;
            folder.photos = new ArrayList<>();
            mFolderList.add(folder);
        }
        return folderBean;
    }

    private void showFolderList() {
        if (mFolderList == null || mFolderList.size() == 0) {
            return;
        }
        if (mFolderDialog == null) {
            mFolderDialog = new StrongBottomSheetDialog(this);
            View view = View.inflate(this, R.layout.layout_folder_dialog, null);
            mFolderDialog.setContentView(view);
            mFolderDialog.setPeekHeight(1000);
            RecyclerView rvFolder = (RecyclerView) view.findViewById(R.id.rv_folder);
            LinearLayoutManager manager = new LinearLayoutManager(rvFolder.getContext());
            rvFolder.setLayoutManager(manager);
            FolderAdapter adapter = new FolderAdapter(this, mFolderList);
            rvFolder.setAdapter(adapter);
            Window window = mFolderDialog.getWindow();
            if (window != null) {
                window.setGravity(Gravity.BOTTOM);
            }
            rvFolder.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    outRect.bottom = 2;
                }
            });
            adapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position, String name) {
                    mFolderDialog.dismiss();
                    if (position >= mFolderList.size() || mFolderList.get(position) == null) {
                        return;
                    }
                    tvCategory.setText(name);
                    if (mAdapter != null) {
                        mAdapter.setData(mFolderList.get(position).photos);
                    }
                }
            });
        }
        mFolderDialog.show();
    }
}
