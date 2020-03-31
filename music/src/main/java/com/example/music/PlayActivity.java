package com.example.music;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

import com.example.db.SqliteDB;
import com.example.model.Music;
import com.example.music.listener.OnMusicChangeListener;
import com.example.music.service.MusicService;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class PlayActivity extends Activity implements View.OnClickListener, OnMusicChangeListener {
	private TextView musicName, musicSinger, musicTime, musicLength;
	private SeekBar seekBar;
	private Button btnPlayOrPause;
	private ImageView bgimg, mImg;
	private int[] imgs;

	private SimpleDateFormat time = new SimpleDateFormat("mm:ss", Locale.CHINA);

	private MusicService musicService;
	ServiceConnection serviceConnection;
	private Intent intent;

	private ObjectAnimator imageAnimator;
	private long mCurrentPlayTime;

	private SqliteDB db;

	public Handler handler = new Handler();
	public Runnable runnable = new Runnable() {

		@Override
		public void run() {
			if (musicService.isPlaying()) {
				btnPlayOrPause.setBackground(getResources().getDrawable(R.drawable.ic_puase));
				if (!imageAnimator.isStarted()) {
					imageAnimator.start();
					imageAnimator.setCurrentPlayTime(mCurrentPlayTime);
				}
			} else {
				btnPlayOrPause.setBackground(getResources().getDrawable(R.drawable.ic_play));
				imageAnimator.cancel();
			}
			seekBar.setMax(musicService.getDuration());
			seekBar.setProgress(musicService.getCurrentPosition());
			musicTime.setText(time.format(musicService.getCurrentPosition()));
			musicLength.setText(time.format(musicService.getDuration()));
			handler.postDelayed(runnable, 200);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		db = SqliteDB.getInstance(PlayActivity.this);

		imgs = new int[] { R.drawable.m_img_1, R.drawable.m_img_2, R.drawable.m_img_3, R.drawable.m_img_4,
				R.drawable.m_img_5, R.drawable.m_img_6, R.drawable.m_img_7 };
		initView();
		setImg(imgs[0]);
		setListener();
		doBindService();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(runnable);
		musicService.removeOnMusicChangeListener(this);
		unbindService(serviceConnection);
	}

	private void initView() {
		musicTime = (TextView) findViewById(R.id.music_time);
		musicLength = (TextView) findViewById(R.id.music_length);
		seekBar = (SeekBar) findViewById(R.id.music_seekbar);
		btnPlayOrPause = (Button) findViewById(R.id.btn_playorpause);
		musicName = (TextView) findViewById(R.id.music_name);
		musicSinger = (TextView) findViewById(R.id.music_singer);
		bgimg = (ImageView) findViewById(R.id.music_bgimg);
		mImg = (ImageView) findViewById(R.id.music_img);

		imageAnimator = ObjectAnimator.ofFloat(mImg, "rotation", 0f, 360.0f);
		imageAnimator.setDuration(20000);
		imageAnimator.setInterpolator(new LinearInterpolator());
		imageAnimator.setRepeatCount(-1);
	}

	private void setListener() {
		btnPlayOrPause.setOnClickListener(this);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser == true) {
					musicService.seekTo(progress);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	private void initMusicInfo() {
		Music music = musicService.getPlayingInfo();
		if (music != null) {
			musicName.setText(music.getName());
			musicSinger.setText(Arrays.toString(music.getSinger()));
		}
	}

	private void doBindService() {
		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				musicService = ((MusicService.LocalBinder) service).getService();
				musicService.setOnMusicChangeListener(PlayActivity.this);
				initMusicInfo();
				handler.post(runnable);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				serviceConnection = null;
			}
		};

		intent = new Intent(PlayActivity.this, MusicService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onMusicChang(Music music) {
		musicName.setText(music.getName());
		musicSinger.setText(Arrays.toString(music.getSinger()));
		setImg(imgs[(int) (Math.random() * 7)]);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_playorpause:
			Log.i("playOrPause", "playOrPause");
			if (musicService.isPlaying())
				mCurrentPlayTime = imageAnimator.getCurrentPlayTime();
			musicService.playOrPause();
			break;
		case R.id.btn_last:
			musicService.last();
			break;
		case R.id.btn_next:
			musicService.next();
			break;
		case R.id.btn_stop:
			musicService.stop();
			break;
		case R.id.btn_love:
			addLove();
			break;
		}
	}

	private void addLove() {
		SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
		int uId = sp.getInt("user_id", 0);
		if (uId == 0) {
			Toast.makeText(this, "请登录", Toast.LENGTH_SHORT).show();
			return;
		}
		Music music = musicService.getPlayingInfo();
		if (music == null)
			return;
		int status = db.addLove(uId, music.getId());
		if (status > 0) {
			Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
		} else if (status == -2) {
			Toast.makeText(this, "已添加", Toast.LENGTH_SHORT).show();
		}
	}

	private void setImg(final int resId) {
		circleImage(resId);
		handler.post(new Runnable() {

			@Override
			public void run() {
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
				bgimg.setImageBitmap(rsBlur(PlayActivity.this, bitmap, 20));
			}
		});
	}

	private void circleImage(int resId) {
		Bitmap src = BitmapFactory.decodeResource(getResources(), resId);
		Bitmap dst;
		// 将长方形图片裁剪成正方形图片
		if (src.getWidth() >= src.getHeight()) {
			dst = Bitmap.createBitmap(src, src.getWidth() / 2 - src.getHeight() / 2, 0, src.getHeight(),
					src.getHeight());
		} else {
			dst = Bitmap.createBitmap(src, 0, src.getHeight() / 2 - src.getWidth() / 2, src.getWidth(), src.getWidth());
		}
		RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), dst);
		roundedBitmapDrawable.setCornerRadius(dst.getWidth() / 2); // 设置圆角半径为正方形边长的一半
		roundedBitmapDrawable.setAntiAlias(true);
		mImg.setImageDrawable(roundedBitmapDrawable);
	}

	private static Bitmap rsBlur(Context context, Bitmap source, int radius) {
		Bitmap inputBmp = source;
		RenderScript renderScript = RenderScript.create(context);
		final Allocation input = Allocation.createFromBitmap(renderScript, inputBmp);
		final Allocation output = Allocation.createTyped(renderScript, input.getType());
		ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
		scriptIntrinsicBlur.setInput(input);
		scriptIntrinsicBlur.setRadius(radius);
		scriptIntrinsicBlur.forEach(output);
		output.copyTo(inputBmp);
		renderScript.destroy();
		return inputBmp;
	}
}
