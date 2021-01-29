package com.example.photo.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.photo.R;
import com.example.photo.bean.ChangeInfoBean;
import com.example.photo.dialog.ItemsAlertDialogUtil;
import com.example.photo.dialog.PhotoPopupWindow;
import com.example.photo.utils.Constants;
import com.example.photo.utils.InfoPrefs;
import com.example.photo.utils.PictureUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
* @ 作者: GB
* @ 描述:用户信息
*/

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UserInfoActivity";
    private static final int REQUEST_IMAGE_GET = 0;//相册选取
    private static final int REQUEST_IMAGE_CAPTURE = 1;//拍照
    private static final int REQUEST_SMALL_IMAGE_CUTTING = 2;//切割
    private static final int REQUEST_CHANGE_USER_NICK_NAME = 10;//名字
    private static final String IMAGE_FILE_NAME = "user_head_icon.jpg";//给头像一个默认的图片

    PhotoPopupWindow mPhotoPopupWindow;//悬浮框
    TextView textView_user_nick_name;//名字
    TextView textView_user_gender;//性别
    CircleImageView circleImageView_user_head;//头像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        textView_user_nick_name = findViewById(R.id.user_nick_name_TV);//名字
        textView_user_gender = findViewById(R.id.user_gender_TV);//性别
        circleImageView_user_head = findViewById(R.id.user_head_iv);//头像
        //InfoPrefs自己封装的一个 SharedPreferences 工具类
        //init()指定文件名，getData(String key)获取key对应的字符串，getIntData(int key)获取key对应的int
        InfoPrefs.init("user_info");
        refresh();

        RelativeLayout relativeLayout_user_nick_name = findViewById(R.id.user_nick_name);
        relativeLayout_user_nick_name.setOnClickListener(this);

        RelativeLayout relativeLayout_user_gender = findViewById(R.id.user_gender);
        relativeLayout_user_gender.setOnClickListener(this);

        RelativeLayout relativeLayout_user_head = findViewById(R.id.user_head);
        relativeLayout_user_head.setOnClickListener(this);
        //初始化 toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_userinfo);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            //指定toolbar左上角的返回按钮，这个按钮的id是home（无法更改）
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeAsUpIndicator();
        }
    }

    /*@Override
    protected void onRestart() {
        super.onRestart();
        refresh();
    }*/

    public void refresh(){
        textView_user_nick_name.setText(InfoPrefs.getData(Constants.UserInfo.NAME));
        textView_user_gender.setText(InfoPrefs.getData(Constants.UserInfo.GENDER));
        showHeadImage();
        //circleImageView_user_head.setImageURI();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.user_head:
                //创建存放头像的文件夹
                PictureUtil.mkdirMyPetRootDirectory();
                mPhotoPopupWindow = new PhotoPopupWindow(UserInfoActivity.this, new View.OnClickListener() {//悬浮框
                    @Override
                    public void onClick(View v) {
                        // 文件权限申请
                        if (ContextCompat.checkSelfPermission(UserInfoActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // 权限还没有授予，进行申请
                            ActivityCompat.requestPermissions(UserInfoActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200); // 申请的 requestCode 为 200
                        } else {
                            // 如果权限已经申请过，直接进行图片选择
                            mPhotoPopupWindow.dismiss();
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            // 判断系统中是否有处理该 Intent 的 Activity
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivityForResult(intent, REQUEST_IMAGE_GET);
                            } else {
                                Toast.makeText(UserInfoActivity.this, "未找到图片查看器", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, new View.OnClickListener()
                {
                    @Override
                    public void onClick (View v){
                        // 拍照及文件权限申请
                        if (ContextCompat.checkSelfPermission(UserInfoActivity.this,
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                                || ContextCompat.checkSelfPermission(UserInfoActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // 权限还没有授予，进行申请
                            ActivityCompat.requestPermissions(UserInfoActivity.this,
                                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 300); // 申请的 requestCode 为 300
                        } else {
                            // 权限已经申请，直接拍照
                            mPhotoPopupWindow.dismiss();
                            imageCapture();
                        }
                    }
                });
                View rootView = LayoutInflater.from(UserInfoActivity.this).inflate(R.layout.activity_userinfo, null);
                mPhotoPopupWindow.showAtLocation(rootView,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                break;

            case R.id.user_nick_name:
                ChangeInfoBean bean = new ChangeInfoBean();
                bean.setTitle("修改昵称");
                bean.setInfo(InfoPrefs.getData(Constants.UserInfo.NAME));
                Intent intent = new Intent(UserInfoActivity.this,ChangeInfoActivity.class);
                intent.putExtra("data", bean);
                startActivityForResult(intent,REQUEST_CHANGE_USER_NICK_NAME);
                break;

            case R.id.user_gender:
                new ItemsAlertDialogUtil(UserInfoActivity.this).setItems(Constants.GENDER_ITEMS).
                        setListener(new ItemsAlertDialogUtil.OnSelectFinishedListener() {
                            @Override
                            public void SelectFinished(int which) {
                                InfoPrefs.setData(Constants.UserInfo.GENDER,Constants.GENDER_ITEMS[which]);
                                textView_user_gender.setText(InfoPrefs.getData(Constants.UserInfo.GENDER));
                            }
                        }).showDialog();
                break;
            default:
        }
    }
    //以下为头像相关方法

    private void showHeadImage() {
        boolean isSdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);// 判断sdcard是否存在
        if (isSdCardExist) {

            String path = InfoPrefs.getData(Constants.UserInfo.HEAD_IMAGE);// 获取图片路径

            File file = new File(path);
            if (file.exists()) {
                Bitmap bm = BitmapFactory.decodeFile(path);
                // 将图片显示到ImageView中
                circleImageView_user_head.setImageBitmap(bm);
            }else{
                Log.e(TAG,"no file");
                circleImageView_user_head.setImageResource(R.drawable.huaji);
            }
        } else {
            Log.e(TAG,"no SD card");
            circleImageView_user_head.setImageResource(R.drawable.huaji);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 回调成功
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                // 切割
                case REQUEST_SMALL_IMAGE_CUTTING:
                    Log.e(TAG, "before show");
                    File cropFile = new File(PictureUtil.getMyPetRootDirectory(), "crop.jpg");
                    Uri cropUri = Uri.fromFile(cropFile);
                    setPicToView(cropUri);
                    break;

                // 相册选取
                case REQUEST_IMAGE_GET:
                    Uri uri = PictureUtil.getImageUri(this, data);
                    startPhotoZoom(uri);
                    break;

                // 拍照
                case REQUEST_IMAGE_CAPTURE:
                    File pictureFile = new File(PictureUtil.getMyPetRootDirectory(), IMAGE_FILE_NAME);
                    Uri pictureUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        pictureUri = FileProvider.getUriForFile(this,
                                "com.example.photo.fileprovider", pictureFile);
                        Log.e(TAG, "picURI=" + pictureUri.toString());
                    } else {
                        pictureUri = Uri.fromFile(pictureFile);
                    }
                    startPhotoZoom(pictureUri);
                    // 通知图库更新
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri uri1 = Uri.fromFile(pictureFile);
                    intent.setData(uri1);
                    this.sendBroadcast(intent);
                    break;
                // 获取changeinfo销毁 后 回传的数据
                case REQUEST_CHANGE_USER_NICK_NAME:
                    String returnData = data.getStringExtra("data_return");
                    InfoPrefs.setData(Constants.UserInfo.NAME, returnData);
                    textView_user_nick_name.setText(InfoPrefs.getData(Constants.UserInfo.NAME));
                    break;
                default:
            }
        } else {
            Log.e(TAG, "result = " + resultCode + ",request = " + requestCode);
        }
    }

    private void startPhotoZoom(Uri uri) {
        Log.d(TAG,"Uri = "+uri.toString());
        //保存裁剪后的图片
        File cropFile=new File(PictureUtil.getMyPetRootDirectory(),"crop.jpg");
        try{
            if(cropFile.exists()){
                cropFile.delete();
                Log.e(TAG,"delete");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        Uri cropUri;
        cropUri = Uri.fromFile(cropFile);

        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1); // 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300); // 输出图片大小
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);

        Log.e(TAG,"cropUri = "+cropUri.toString());

        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, REQUEST_SMALL_IMAGE_CUTTING);
    }

   /** public void startBigPhotoZoom(Uri uri) {
        // 创建大图文件夹
        Uri imageUri = null;
        File file;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String storage = Environment.getExternalStorageDirectory().getPath();
            File dirFile = new File(storage + "/bigIcon");
            if (!dirFile.exists()) {
                if (!dirFile.mkdirs()) {
                    Log.e("TAG", "文件夹创建失败");
                } else {
                    Log.e("TAG", "文件夹创建成功");
                }
            }
            file = new File(dirFile, System.currentTimeMillis() + ".jpg");
            imageUri = Uri.fromFile(file);
        }
        // 开始切割
        Intent intent = new Intent("com.android.camera.action.CROP");
        //intent.setDataAndType(uri, "image/*");
        intent.setDataAndType(FileProvider.getUriForFile(this,"com.example.mypet.fileprovider",file),"image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1); // 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 600); // 输出图片大小
        intent.putExtra("outputY", 600);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false); // 不直接返回数据
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // 返回一个文件
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, REQUEST_BIG_IMAGE_CUTTING);
    }*/

    private void imageCapture() {
        Intent intent;
        Uri pictureUri;
        //getMyPetRootDirectory()得到的是Environment.getExternalStorageDirectory() + File.separator+"MyPet"
        //也就是我之前创建的存放头像的文件夹（目录）
        File pictureFile = new File(PictureUtil.getMyPetRootDirectory(), IMAGE_FILE_NAME);
        // 判断当前系统
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //这一句非常重要
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //""中的内容是随意的，但最好用package名.provider名的形式，清晰明了
            pictureUri = FileProvider.getUriForFile(this,
                    "com.example.photo.fileprovider", pictureFile);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        // 去拍照,拍照的结果存到oictureUri对应的路径中
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        Log.e(TAG,"before take photo"+pictureUri.toString());
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }

    public void setPicToView(Uri uri)  {
        if (uri != null) {
            Bitmap photo = null;
            try {
                photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
            // 创建 smallIcon 文件夹
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //String storage = Environment.getExternalStorageDirectory().getPath();
                File dirFile = new File(PictureUtil.getMyPetRootDirectory(),  "Icon");
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.d(TAG, "in setPicToView->文件夹创建失败");
                    } else {
                        Log.d(TAG, "in setPicToView->文件夹创建成功");
                    }
                }
                File file = new File(dirFile, IMAGE_FILE_NAME);
                InfoPrefs.setData(Constants.UserInfo.HEAD_IMAGE,file.getPath());
                //Log.d("result",file.getPath());
                // Log.d("result",file.getAbsolutePath());
                // 保存图片
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 在视图中显示图片
            showHeadImage();
            //circleImageView_user_head.setImageBitmap(InfoPrefs.getData(Constants.UserInfo.GEAD_IMAGE));
        }
    }

    /**public void setPicToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data"); // 直接获得内存中保存的 bitmap
            // 创建 smallIcon 文件夹
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //String storage = Environment.getExternalStorageDirectory().getPath();
                File dirFile = new File(PictureUtil.getMyPetRootDirectory(),  "Icon");
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.d(TAG, "in setPicToView->文件夹创建失败");
                    } else {
                        Log.d(TAG, "in setPicToView->文件夹创建成功");
                    }
                }
                File file = new File(dirFile, IMAGE_FILE_NAME);
                InfoPrefs.setData(Constants.UserInfo.HEAD_IMAGE,file.getPath());
                //Log.d("result",file.getPath());
               // Log.d("result",file.getAbsolutePath());
                // 保存图片
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 在视图中显示图片
            showHeadImage();
            //circleImageView_user_head.setImageBitmap(InfoPrefs.getData(Constants.UserInfo.GEAD_IMAGE));
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPhotoPopupWindow.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    // 判断系统中是否有处理该 Intent 的 Activity
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_GET);
                    } else {
                        Toast.makeText(UserInfoActivity.this, "未找到图片查看器", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mPhotoPopupWindow.dismiss();
                }
                break;
            case 300://拍照权限
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPhotoPopupWindow.dismiss();
                    imageCapture();
                } else {
                    mPhotoPopupWindow.dismiss();
                }
                break;
        }
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


//头像相关到此结束

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                //Toast.makeText(UserInfoActivity.this,"you press back!",Toast.LENGTH_LONG).show();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }






}