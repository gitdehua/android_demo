package com.example.music;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.db.SqliteDB;
import com.example.model.Music;
import com.example.music.service.MusicService;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ScanMusicActivity extends Activity {
	SqliteDB db;
	ListView resList;

	ServiceConnection serviceConnection;
	MusicService musicService;
	List<Music> musicList;
	HashSet<Music> musicListChecked;

	// 通过 Handler 更新 UI 上的组件状态
	public Handler handler = new Handler();

	// 返回按钮
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish(); // back button
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_music);
		musicListChecked = new HashSet<Music>();

		db = SqliteDB.getInstance(ScanMusicActivity.this);

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(false); // 图标不可以点击
			actionBar.setDisplayHomeAsUpEnabled(true); // 添加向左小箭头
			actionBar.setDisplayShowHomeEnabled(false); // 隐藏logo和icon
			actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar));
		}

		resList = (ListView) findViewById(R.id.res_list);
		resList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
				if (checkBox.isChecked()) {
					checkBox.setChecked(false);
					musicListChecked.remove(musicList.get(position));
				} else {
					musicListChecked.add(musicList.get(position));
					checkBox.setChecked(true);
				}
			}
		});

		doBindService();
		loadList();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}

	private void doBindService() {
		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				musicService = ((MusicService.LocalBinder) service).getService();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				serviceConnection = null;
			}
		};

		Intent intent = new Intent(ScanMusicActivity.this, MusicService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	public void onClick(View v) {
		Iterator<Music> iterator = musicListChecked.iterator();
		switch (v.getId()) {
		case R.id.btn_add2paly:
			while (iterator.hasNext()) {
				musicService.add(iterator.next());
			}
			Toast.makeText(ScanMusicActivity.this, String.format("添加 %s 首歌！", musicListChecked.size()),
					Toast.LENGTH_SHORT).show();
			clearChecked();
			break;
		case R.id.btn_delete_db:
			showLoading();
			Log.i("onDelete", musicListChecked.toString());
			while (iterator.hasNext()) {
				db.deleteMusic(iterator.next().getId());
			}
			loadList();
			break;
		case R.id.btn_scan:
			scanFile();
			break;
		}
		clearChecked();
	}

	private void scanFile() {
		findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			// 创建 Pattern 对象
			Pattern r = Pattern.compile("(.*) - (.*)\\.(mp3|m4a)");

			@Override
			public void run() {
				String musicDirPath = Environment.getExternalStorageDirectory().getPath() + "/music/";
				File musicDir = new File(musicDirPath);
				String[] fileList = musicDir.list();
				for (int i = 0; i < fileList.length; i++) {
					String fname = fileList[i];
					System.out.println(fname);
					String mName, singer;
					// 创建 matcher 对象
					Matcher m = r.matcher(fname);
					if (m.find()) {
						mName = m.group(2);
						singer = m.group(1);
					} else {
						continue;
					}

					int mId = db.getMusicId(mName, musicDirPath + fname);
					if (mId <= 0)
						continue;
					String[] singers = singer.split("&");
					for (int j = 0; j < singers.length; j++) {
						int sId = db.getSingerId(singers[j]);
						db.insertMusicAndSinger(mId, sId);
					}
				}
				loadList();
			}
		}).start();
	}

	private void loadList() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				musicList = db.getAllMusic();
				String[] from = new String[] { "name", "singer" };
				int[] to = new int[] { R.id.music_name, R.id.singer };
				List<Map<String, String>> musicMapList = new ArrayList<>();
				Iterator<Music> iterator = musicList.iterator();
				while (iterator.hasNext()) {
					musicMapList.add(iterator.next().toMap());
				}
				ListAdapter adapter = new SimpleAdapter(ScanMusicActivity.this, musicMapList,
						R.layout.music_item_checkbox, from, to) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						// TODO Auto-generated method stub
						View view = super.getView(position, convertView, parent);
						((CheckBox) view.findViewById(R.id.checkbox))
								.setChecked(musicListChecked.contains(musicList.get(position)));
						return view;
					}
				};
				resList.setAdapter(adapter);
				hideLoading();
			}
		});
	}

	private void showLoading() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
			}
		});
	}

	private void hideLoading() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.progressBar).setVisibility(View.GONE);
			}
		}, 1000);
	}

	private void clearChecked() {
		for (int i = 0; i < resList.getChildCount(); i++) {
			((CheckBox) resList.getChildAt(i).findViewById(R.id.checkbox)).setChecked(false);
		}
		musicListChecked.clear();
	}
}
