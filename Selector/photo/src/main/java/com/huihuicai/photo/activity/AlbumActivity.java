package com.huihuicai.photo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.huihuicai.photo.R;
import com.huihuicai.photo.Util;
import com.huihuicai.photo.adapter.FolderAdapter;
import com.huihuicai.photo.adapter.AlbumAdapter;
import com.huihuicai.photo.bean.FolderBean;
import com.huihuicai.photo.bean.PhotoBean;
import com.huihuicai.photo.view.StrongBottomSheetDialog;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends BaseActivity implements
        View.OnClickListener, AlbumAdapter.OnItemClickListener {

    public static final String EXTRA_MAX = "max_piece";
    public static final String EXTRA_CAMERA = "use_camera";
    public static final String EXTRA_COLUMN = "max_column";
    public static final String EXTRA_RESULT = "result";
    public static final String SUFFIX_PHOTO = "temp.png";
    public static final String SUFFIX_CROP = "crop.png";

    public static final int LOADER_ALL = 0;
    public static final int LOADER_OTHER = 1;
    public static final int REQUEST_CAMERA = 100;
    public static final int REQUEST_CROP = 101;
    public static final int REQUEST_PERMISSION = 102;

    private TextView tvFinish;
    private TextView tvCategory;
    private TextView tvPreview;
    private RecyclerView rvPhoto;
    private StrongBottomSheetDialog mFolderDialog;

    private int mMaxPiece;
    private boolean mUseCamera;
    private AlbumAdapter mAdapter;
    private List<FolderBean> mFolderList = new ArrayList<>();
    private File mCameraFile, mCutFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvFinish = getRight();
        tvCategory = (TextView) findViewById(R.id.tv_category);
        tvPreview = (TextView) findViewById(R.id.tv_preview);
        rvPhoto = (RecyclerView) findViewById(R.id.rv_photo);
        tvFinish.setOnClickListener(this);
        tvCategory.setOnClickListener(this);
        tvPreview.setOnClickListener(this);
        initData();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.activity_album;
    }

    private void initData() {
        String piece = getIntent().getStringExtra(EXTRA_MAX);
        mUseCamera = getIntent().getBooleanExtra(EXTRA_CAMERA, false);
        mMaxPiece = TextUtils.isEmpty(piece) ? 1 : Integer.parseInt(piece);
        int column = getIntent().getIntExtra(EXTRA_COLUMN, 3);
        GridLayoutManager manager = new GridLayoutManager(this, column);
        rvPhoto.setLayoutManager(manager);
        mAdapter = new AlbumAdapter(this, mMaxPiece, mUseCamera);
        mAdapter.setOnClickListener(this);
        rvPhoto.setAdapter(mAdapter);
        tvCategory.setText(R.string.all);
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
    public void onItemClick(int position, int count) {
        if (mUseCamera && position == 0) {
            String[] permission = {Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE};
            boolean ready = Util.requestPermission(this, permission, REQUEST_PERMISSION);
            if (ready) {
                openCamera();
            }
        } else {
            tvFinish.setEnabled(count != 0);
            tvPreview.setEnabled(count != 0);
            int color = getResources().getColor(R.color.light_white);
            int selectColor = getResources().getColor(R.color.white);
            tvFinish.setTextColor(count != 0 ? selectColor : color);
            tvPreview.setTextColor(count != 0 ? selectColor : color);
            if (count == 0) {
                tvFinish.setText(R.string.finish);
            } else {
                tvFinish.setText(getResources().getString(R.string.select_finish, count, mMaxPiece));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == tvFinish) {
            if (mAdapter != null && mAdapter.getSelected() != null) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT, (Serializable) mAdapter.getSelected());
                setResult(RESULT_OK, intent);
                finish();
            }
        } else if (v == tvCategory) {
            showFolderList();
        } else if (v == tvPreview) {
            if (mAdapter != null && mAdapter.getSelected() != null) {
                Intent intent = new Intent(this, PreviewActivity.class);
                intent.putExtra(PreviewActivity.EXTRA_DATA, (Serializable) mAdapter.getSelected());
                startActivity(intent);
            }
        }
    }

    //loader callback
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
                return new CursorLoader(AlbumActivity.this,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        PROJECTION,
                        PROJECTION[4] + "=? or " + PROJECTION[4] + "=? and " + PROJECTION[5] + ">0",
                        new String[]{"image/jpeg", "image/png"},
                        PROJECTION[4] + " desc");
            } else if (id == LOADER_OTHER) {
                String folder = args.getString("folder");
                return new CursorLoader(AlbumActivity.this,
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

    //通过一个文件，得到其所在的文件夹
    private String getFolder(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        return file.getParent();
    }

    //通过一个文件夹，得到一个文件夹对象
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

    //显示有图片的文件夹列表
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

    private void openCamera() {
        mCameraFile = getFile(mCameraFile, SUFFIX_PHOTO);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri imgUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imgUri = FileProvider.getUriForFile(this, "com.huihuicai.photo.fileprovider", mCameraFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            imgUri = Uri.fromFile(mCameraFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void cropPhoto() {
        mCutFile = getFile(mCutFile, SUFFIX_CROP);
        Intent intent = new Intent("com.android.camera.action.CROP");
        Uri imgUri, outputUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            outputUri = FileProvider.getUriForFile(this, "com.huihuicai.photo.fileprovider", mCutFile);
            imgUri = FileProvider.getUriForFile(this, "com.huihuicai.photo.fileprovider", mCameraFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            outputUri = Uri.fromFile(mCutFile);
            imgUri = Uri.fromFile(mCameraFile);
        }
        intent.setDataAndType(imgUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, REQUEST_CROP);
    }

    private File getFile(File file, String photoName) {
        if (file == null) {
            String directory;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                directory = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "camera" + File.separator;
            } else {
                directory = getCacheDir().getAbsolutePath() + File.separator + "camera" + File.separator;
            }
            file = new File(directory + photoName);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            boolean isGrantAll = true;
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isGrantAll = false;
                    break;
                }
            }
            if (isGrantAll) {
                openCamera();
            } else {
                Toast.makeText(this, R.string.permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (data == null) {
                cropPhoto();
            }
        } else if (requestCode == REQUEST_CROP) {
            List<PhotoBean> list = new ArrayList<>();
            if (mCutFile != null) {
                PhotoBean bean = new PhotoBean();
                bean.path = mCutFile.getAbsolutePath();
                bean.parent = mCutFile.getParent();
                bean.name = mCutFile.getName();
                list.add(bean);
            }
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT, (Serializable) list);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
