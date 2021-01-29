package com.example.photo.activity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.photo.R;
import com.example.photo.service.ClockService;
import com.example.photo.service.PetWindowService;
import com.example.photo.service.WeChatListenerService;
import com.example.photo.utils.Constants;
import com.example.photo.utils.InfoPrefs;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private DrawerLayout mDrawerLayout;//滑动菜单 抽屉布局
    //Accessibility权限标志
    private boolean accessibilityIsOpen = false;
    private final int REQUEST_ACCESSIBILITY = 1;//微信消息
    private final int REQUEST_FLOAT_WINDOW = 10;//悬浮窗

    private CircleImageView circleImageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent startIntent = new Intent(this, ClockService.class);
        startService(startIntent);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle("主页");
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_main);
        NavigationView navigationView = findViewById(R.id.navigation_main);//侧边栏的控件
        View headView = navigationView.getHeaderView(0);
        circleImageView = headView.findViewById(R.id.icon_image);
        textView = headView.findViewById(R.id.username);
        refreshHeadView();
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }

        //navigationView.setCheckedItem(R.id.user_info);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.user_info:
                        Intent it = new Intent(MainActivity.this, UserInfoActivity.class);
                        startActivity(it);
                        break;
                    case R.id.pet_info:
                        Intent petIntent = new Intent(MainActivity.this,PetInfoActivity.class);
                        startActivity(petIntent);
                        break;
                    case R.id.pet_mmListen:
                        //检查Accessibility权限
                        if (checkAccessibilityPermission()) {
                            //新线程开启服务
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    WeChatListenerService.isRunning = true;
                                    startService(new Intent(MainActivity.this, WeChatListenerService.class));
                                }
                            }).start();
                        } else {
                            //申请Accessibility权限
                            requestAccessibilityPermission();
                        }
                        break;
                    case R.id.pet_blt:
                        Intent blt = new Intent(MainActivity.this, BlueToothInfoActivity.class);
                        startActivity(blt);
                        break;
                    case R.id.alarm_clock:
                        Intent alarm_intent = new Intent(MainActivity.this, AlarmClockActivity.class);
                        startActivity(alarm_intent);
                    default:
                }
                mDrawerLayout.closeDrawers();//关闭所有抽屉布局
                return true;
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        refreshHeadView();
    }

    private void refreshHeadView() {//刷新图片
        InfoPrefs.init("user_info");
        String path = InfoPrefs.getData(Constants.UserInfo.HEAD_IMAGE);// 获取图片路径

        File file = new File(path);
        if (file.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(path);
            // 将图片显示到ImageView中
            circleImageView.setImageBitmap(bm);
        }else{
            Log.e(TAG,"no file");
            circleImageView.setImageResource(R.drawable.huaji);
        }
        textView.setText(InfoPrefs.getData("user_nick_name"));
    }

    private long firstTime = 0;

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //不存在连按两次退出
            if (System.currentTimeMillis() - firstTime > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);//START表示打开左边   END表示打开右边抽屉布局
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    //Accessibility权限检查
    private boolean checkAccessibilityPermission() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        //遍历所有的AccessibilityService 如果没有本服务，则表示没开启
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals("com.example.photo/.service.WeChatListenerService")) {
                accessibilityIsOpen = true;
                return true;
            }
        }
        return false;
    }

    //Accessibility权限申请
    private void requestAccessibilityPermission() {
        if (!accessibilityIsOpen) {
            Toast.makeText(this, "请打开本应用的权限", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, REQUEST_ACCESSIBILITY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCESSIBILITY:
                AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
                List<AccessibilityServiceInfo> accessibilityServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
                for (AccessibilityServiceInfo info : accessibilityServices) {
                    if (info.getId().equals("com.example.photo/.service.WeChatListenerService")) {
                        accessibilityIsOpen = true;
                        WeChatListenerService.isRunning = true;
                        startService(new Intent(this, WeChatListenerService.class));
                        Toast.makeText(this, "微信消息提醒已开启", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Toast.makeText(this, "微信消息提醒未开启", Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_FLOAT_WINDOW:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!Settings.canDrawOverlays(getApplicationContext())) {
                        Toast.makeText(this, "未开启悬浮窗权限", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent it = new Intent(MainActivity.this, PetWindowService.class);
                        startService(it);
                    }
                }
                break;
        }

    }

}
