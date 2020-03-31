package com.example.music.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.example.model.Music;
import com.example.music.listener.OnMusicChangeListener;
import com.example.music.listener.OnMusicListChangeListener;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service implements OnCompletionListener {
	private MediaPlayer mediaPlayer;
	private List<Music> musicList;
	private int musicIndex;
	private HashSet<OnMusicListChangeListener> musicListChangeListener = new HashSet<>();
	private HashSet<OnMusicChangeListener> musicChangeListener = new HashSet<>();
	private boolean isFirst = true;

	public class LocalBinder extends Binder {
		public MusicService getService() {
			return MusicService.this;
		}
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("MusicService_event", "onBind");
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("MusicService_event", "onCreate");
		initMediaPlayer();
		musicList = new ArrayList<>();
		musicIndex = 0;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("MusicService_event", "onDestroy");
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
	}

	public void setOnMusicListChangeListener(OnMusicListChangeListener l) {
		musicListChangeListener.add(l);
	}

	public void removeOnMusicListChangeListener(OnMusicListChangeListener l) {
		musicListChangeListener.remove(l);
	}

	public void setOnMusicChangeListener(OnMusicChangeListener l) {
		musicChangeListener.add(l);
	}

	public void removeOnMusicChangeListener(OnMusicChangeListener l) {
		musicChangeListener.remove(l);
	}

	private void initMediaPlayer() {
//		mediaPlayer = MediaPlayer.create(this, R.raw.music);
		mediaPlayer = new MediaPlayer();
//      mediaPlayer.setLooping(true);
		mediaPlayer.setOnCompletionListener(this);
	}

	public void add(Music m) {
		musicList.add(m);
		onMusicListChange();
	}

	public void remove(int index) {
		musicList.remove(index);
		onMusicListChange();
		if (musicList.size() == 0) {
			isFirst = true;
		}
	}

	public void onMusicListChange() {
		if (musicListChangeListener != null && musicListChangeListener.size() > 0) {
			Iterator<OnMusicListChangeListener> iterator = musicListChangeListener.iterator();
			while (iterator.hasNext()) {
				iterator.next().onMusicListChange(musicList);
			}
		}

	}

	public List<Music> getMusicList() {
		return musicList;
	}

	public void last() {
		musicIndex--;
		if (musicIndex < 0) {
			musicIndex = musicList.size() - 1;
		}
		changeMusic(musicIndex);
	}

	public void next() {
		musicIndex++;
		if (musicIndex >= musicList.size()) {
			musicIndex = 0;
		}
		changeMusic(musicIndex);
	}

	public void changeMusic(int index) {
		System.out.println(index);
		if (mediaPlayer == null)
			return;
		if (musicList.size() == 0) {
			Toast.makeText(this, "播放列表为空", Toast.LENGTH_LONG).show();
			return;
		}
		isFirst = false;
		musicIndex = index;
		if (musicChangeListener != null && musicChangeListener.size() > 0) {
			Iterator<OnMusicChangeListener> iterator = musicChangeListener.iterator();
			while (iterator.hasNext()) {
				iterator.next().onMusicChang(musicList.get(index));
			}
		}
		Log.i("res_index", musicList.get(index).toString());
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(musicList.get(index).getPath());
			mediaPlayer.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaPlayer.start();
		mediaPlayer.seekTo(0);
	}

	public void playOrPause() {
		if (isFirst) {
			if (musicList.size() == 0) {
				Toast.makeText(this, "播放列表为空", Toast.LENGTH_LONG).show();
				return;
			}
			changeMusic(0);
			isFirst = false;
			return;
		}
		if (!mediaPlayer.isPlaying()) {
			mediaPlayer.start();
		} else {
			mediaPlayer.pause();
		}
	}

	public boolean isPlaying() {
		if (mediaPlayer == null)
			return false;
		return mediaPlayer.isPlaying();
	}

	public int getCurrentPosition() {
		if (mediaPlayer == null)
			return 0;
		return mediaPlayer.getCurrentPosition();
	}

	public int getDuration() {
		if (mediaPlayer == null)
			return 0;
		return mediaPlayer.getDuration();
	}

	public Music getPlayingInfo() {
		if (musicList.size() > 0) {
			return musicList.get(musicIndex);
		}
		return null;
	}

	public void seekTo(int msec) {
		if (mediaPlayer == null)
			return;
		mediaPlayer.seekTo(msec);
	}

	public void stop() {
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			Toast.makeText(this, "结束播放", Toast.LENGTH_SHORT).show();
			try {
				mediaPlayer.prepare();
				mediaPlayer.seekTo(0);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		next();
	}
}
