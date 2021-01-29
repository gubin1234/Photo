package com.example.photo.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo.R;
import com.example.photo.adapter.MusicAdapter;
import com.example.photo.bean.MusicInfo;
import com.example.photo.utils.AudioPlayer;
import com.example.photo.utils.Constants;
import com.example.photo.utils.MyPetApplication;
import com.example.photo.utils.RingUtil;

import java.util.ArrayList;
import java.util.List;

public class LocalMusicSelectActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "LocalMusicSelectActivity";
    private static final int LOADER = 1;
    List<MusicInfo> localMusicList = new ArrayList<>();

    String title;
    String uri;
    int from;

    private int position = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music_select);
        initToolbar();

        Intent intent = getIntent();
        title = intent.getStringExtra("ring_title");
        uri = intent.getStringExtra("ring_uri");
        from = intent.getIntExtra("ring_from",0);

        initMusicList();
        //initRecyclerView();
    }



    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_local_music_select);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null ){
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        }
    }

    private void initMusicList() {
        LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.initLoader(LOADER,null,this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioPlayer.getInstance(MyPetApplication.getContext()).stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_confirm,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.confirm:
                if(title == null ){
                    setResult(RESULT_CANCELED);
                }
                else{
                    Intent intent = new Intent();
                    intent.putExtra("ring_title",title);
                    intent.putExtra("ring_uri",uri);
                    intent.putExtra("ring_from",from);
                    setResult(RESULT_OK,intent);
                }
                finish();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(this,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA}, null, null,
                MediaStore.Audio.Media.TITLE);
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.local_music_select_RV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        MusicAdapter adapter = new MusicAdapter(localMusicList,recyclerView);
        adapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                AudioPlayer.getInstance(MyPetApplication.getContext())
                        .play(localMusicList.get(position).getMusicUri(),false,false);
                title = localMusicList.get(position).getMusicName();
                uri = localMusicList.get(position).getMusicUri();
                from = 1;
                //Toast.makeText(SystemRingSelectActivity.this,systemRingList.get(position).getMusicName(),Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        if(position >= 0){
            if(position<=2){
                recyclerView.scrollToPosition(0);
            }else{
                recyclerView.scrollToPosition(position-2);
            }
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch(loader.getId()){
            case LOADER:
                int i=0;
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
                        .moveToNext()) {
                    // 音频文件名
                    long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                    if(duration < 2000)
                        continue;
                    MusicInfo musicInfo = new MusicInfo();
                    musicInfo.setMusicName(cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.TITLE)));
                    musicInfo.setFrom(0);
                    musicInfo.setMusicUri(cursor.getString(cursor
                            .getColumnIndex(MediaStore.Audio.Media.DATA)));
                    musicInfo.setDuration(RingUtil.formatDuration(duration));
                    if(musicInfo.getMusicName().equals(title)){
                        musicInfo.setIsSelected(Constants.TRUE);
                        position = i;
                    }else{
                        musicInfo.setIsSelected(Constants.FALSE);
                    }
                    i++;
                    localMusicList.add(musicInfo);
                }
                initRecyclerView();
                break;
            default:
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

}
