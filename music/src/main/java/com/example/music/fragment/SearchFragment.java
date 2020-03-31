package com.example.music.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.example.db.SqliteDB;
import com.example.model.Music;
import com.example.music.MainActivity;
import com.example.music.R;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SearchFragment extends Fragment {
	EditText edit;
	ListView resList;
	List<Music> musicList;
	SqliteDB db;

	// 通过 Handler 更新 UI 上的组件状态
	public Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View fragment = inflater.inflate(R.layout.fragment_search, container, false);
		db = SqliteDB.getInstance(getActivity());

		edit = (EditText) fragment.findViewById(R.id.edit_search);
		fragment.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				search();
			}
		});
		resList = (ListView) fragment.findViewById(R.id.res_list);
		resList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((MainActivity) getActivity()).getMusicService().add(musicList.get(position));
				Toast.makeText(getActivity(), String.format("%s 添加成功！", (String) musicList.get(position).getName()),
						Toast.LENGTH_SHORT).show();
			}
		});

		return fragment;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void createList() {
		handler.post(new Runnable() {

			@Override
			public void run() {
				String[] from = new String[] { "name", "singer" };
				int[] to = new int[] { R.id.music_name, R.id.singer };
				List<Map<String, String>> musicMapList = new ArrayList<>();
				Iterator<Music> iterator = musicList.iterator();
				while (iterator.hasNext()) {
					musicMapList.add(iterator.next().toMap());
				}
				ListAdapter adapter = new SimpleAdapter(getActivity(), musicMapList, R.layout.music_item, from, to);

				resList.setAdapter(adapter);
			}
		});
	}

	private void search() {
		musicList = db.searchMusic(edit.getText().toString());
		System.out.println(musicList.toString());
		createList();
	}
}
