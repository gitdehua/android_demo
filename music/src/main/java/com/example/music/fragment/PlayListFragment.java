package com.example.music.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.example.model.Music;
import com.example.music.MainActivity;
import com.example.music.R;
import com.example.music.service.MusicService;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class PlayListFragment extends Fragment {
	LinearLayout notFund;
	private ListView playListView;

	private MusicService musicService;

	// 通过 Handler 更新 UI 上的组件状态
	public Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View fragment = inflater.inflate(R.layout.fragment_play_list, container, false);
		notFund = (LinearLayout) fragment.findViewById(R.id.view_not_fund);
		playListView = (ListView) fragment.findViewById(R.id.play_list);
		playListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				musicService.changeMusic(position);
			}
		});
		musicService = ((MainActivity) getActivity()).getMusicService();
		initListView(musicService.getMusicList());
		return fragment;
	}

	private void initListView(List<Music> list) {
		if (list.size() == 0) {
			notFund.setVisibility(View.VISIBLE);
			return;
		}
		notFund.setVisibility(View.GONE);
		String[] from = new String[] { "name", "singer" };
		int[] to = new int[] { R.id.music_name, R.id.singer };
		List<Map<String, String>> musicMapList = new ArrayList<>();
		Iterator<Music> iterator = list.iterator();
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
						musicService.remove(position);
					}
				});
				imageView.setBackground(getActivity().getResources().getDrawable(R.drawable.ic_close));
				return view;
			}
		};

		playListView.setAdapter(adapter);
	}

}
