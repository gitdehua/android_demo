package com.example.music.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.example.db.SqliteDB;
import com.example.model.Music;
import com.example.music.LoginActivity;
import com.example.music.PlayActivity;
import com.example.music.R;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class UserFragment extends Fragment {
	private int uId;
	private ListView listView;
	private List<Music> loveList;
	private LinearLayout navLogin, navLogout, loveListPanel;
	private TextView tvUserName, tvLogTime;

	private SqliteDB db;

	// 通过 Handler 更新 UI 上的组件状态
	private Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View fragment = inflater.inflate(R.layout.fragment_user, container, false);
		db = SqliteDB.getInstance(getActivity());

		listView = (ListView) fragment.findViewById(R.id.list_view);
		navLogin = (LinearLayout) fragment.findViewById(R.id.nav_login);
		navLogout = (LinearLayout) fragment.findViewById(R.id.nav_logout);
		loveListPanel = (LinearLayout) fragment.findViewById(R.id.love_list_panel);
		tvUserName = (TextView) fragment.findViewById(R.id.tv_user_name);
		tvLogTime = (TextView) fragment.findViewById(R.id.tv_login_time);
		setListener();
		getLoginData();
		return fragment;
	}

	private void setListener() {
		navLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), LoginActivity.class));
			}
		});
		navLogout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences sp = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.clear();
				editor.commit();
				loveListPanel.setVisibility(View.GONE);
				navLogin.setVisibility(View.VISIBLE);
				navLogout.setVisibility(View.GONE);
			}
		});
	}

	private void getLoginData() {
		SharedPreferences sp = getActivity().getSharedPreferences("login", Context.MODE_PRIVATE);
		uId = sp.getInt("user_id", 0);
		String username = sp.getString("username", null);
		String loginTime = sp.getString("login_time", null);
		if (uId == 0) {
			loveListPanel.setVisibility(View.GONE);
			navLogin.setVisibility(View.VISIBLE);
			navLogout.setVisibility(View.GONE);
			return;
		}
		loveListPanel.setVisibility(View.VISIBLE);
		navLogin.setVisibility(View.GONE);
		navLogout.setVisibility(View.VISIBLE);
		tvUserName.setText("欢迎：" + username);
		tvLogTime.setText("登录时间：" + loginTime);
		getLoveList();
	}

	private void getLoveList() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				loveList = db.getLoveList(uId);
				String[] from = new String[] { "name", "singer" };
				int[] to = new int[] { R.id.music_name, R.id.singer };
				List<Map<String, String>> musicMapList = new ArrayList<>();
				Iterator<Music> iterator = loveList.iterator();
				while (iterator.hasNext()) {
					musicMapList.add(iterator.next().toMap());
				}
				ListAdapter adapter = new SimpleAdapter(getActivity(), musicMapList, R.layout.music_item, from, to) {
					@Override
					public View getView(final int position, View convertView, ViewGroup parent) {
						View view = super.getView(position, convertView, parent);
						ImageView imageView = (ImageView) view.findViewById(R.id.icon);
						imageView.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								deleteLove(loveList.get(position).getId());
								getLoveList();
							}
						});
						imageView.setBackground(getActivity().getResources().getDrawable(R.drawable.ic_close));
						return view;
					}
				};

				listView.setAdapter(adapter);
			}
		});
	}

	private void deleteLove(int mId) {
		db.deleteLove(uId, mId);
	}
}
