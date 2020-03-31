package com.example.music;

import java.util.Arrays;

import com.example.model.Music;
import com.example.music.fragment.PlayListFragment;
import com.example.music.fragment.SearchFragment;
import com.example.music.fragment.UserFragment;
import com.example.music.listener.OnMusicChangeListener;
import com.example.music.service.MusicService;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener, OnMusicChangeListener {
	private Button btnPlayOrPause;
	private TextView mNameView;

	private MusicService musicService;
	private ServiceConnection serviceConnection;
	private Intent intent;

	public Handler handler = new Handler();
	public Runnable runnable = new Runnable() {

		@Override
		public void run() {
			if (musicService.isPlaying()) {
				btnPlayOrPause.setBackground(getResources().getDrawable(R.drawable.ic_puase));
			} else {
				btnPlayOrPause.setBackground(getResources().getDrawable(R.drawable.ic_play));
			}
			handler.postDelayed(runnable, 200);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mNameView = (TextView) findViewById(R.id.music_name);
		btnPlayOrPause = (Button) findViewById(R.id.btn_playorpause);
		btnPlayOrPause.setOnClickListener(this);
		doBindService();
	}

	public MusicService getMusicService() {
		return musicService;
	}

	public void showFragment(View v) {
		// TODO Auto-generated method stub
		Log.i("main", "show fragment");
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = null;
		switch (v.getId()) {
		case R.id.btn_show_playlist:
			fragment = new PlayListFragment();
			break;
		case R.id.btn_show_search:
			fragment = new SearchFragment();
			break;
		case R.id.btn_show_user:
			fragment = new UserFragment();
			break;
		}
		ft.replace(R.id.content, fragment);
		ft.commit();
	}

	private void bindFragment() {
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment fragment = null;
		fragment = new PlayListFragment();
		ft.replace(R.id.content, fragment);
		ft.commit();
	}

	private void doBindService() {
		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				musicService = ((MusicService.LocalBinder) service).getService();
				musicService.setOnMusicChangeListener(MainActivity.this);
				handler.post(runnable);
				bindFragment();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				serviceConnection = null;
			}
		};

		intent = new Intent(MainActivity.this, MusicService.class);
		startService(intent);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		musicService.removeOnMusicChangeListener(this);
		unbindService(serviceConnection);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.show_paly_activity:
			startActivity(new Intent(this, PlayActivity.class));
			break;
		case R.id.btn_playorpause:
			musicService.playOrPause();
			break;
		case R.id.scan_file:
			startActivity(new Intent(this, ScanMusicActivity.class));
			break;
		case R.id.app_quit:
			this.finish();
			stopService(intent);
			break;
		}
	}

	@Override
	public void onMusicChang(Music music) {
		mNameView.setText(music.getName() + " - " + Arrays.toString(music.getSinger()));
	}
}
