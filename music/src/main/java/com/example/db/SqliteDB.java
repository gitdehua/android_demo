package com.example.db;

import java.util.ArrayList;
import java.util.List;

import com.example.OpenHelper.OpenHelper;
import com.example.model.Music;
import com.example.model.User;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SqliteDB {
	public static final String DB_NAME = "sqlite_dbname";

	public static final int VERSION = 1;

	private static SqliteDB sqliteDB;

	private SQLiteDatabase db;

	private SqliteDB(Context context) {
		OpenHelper dbHelper = new OpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	public synchronized static SqliteDB getInstance(Context context) {
		if (sqliteDB == null) {
			sqliteDB = new SqliteDB(context);
		}
		return sqliteDB;
	}

	public int saveUSer(User user) {
		if (user != null) {
			Cursor cursor = db.rawQuery("select * from user where username=?", new String[] { user.getUsername() });
			if (cursor.getCount() > 0) {
				return -1;
			} else {
				try {
					db.execSQL("insert into user(username,userpwd) values(?,?)",
							new String[] { user.getUsername(), user.getUserpwd() });
				} catch (Exception e) {
					Log.d("sqlite", e.getMessage().toString());
				}
				return 1;
			}
		} else {
			return 0;
		}
	}

	public int queryUser(String name, String pwd) {
		Cursor cursor = db.rawQuery("select * from user where username=?", new String[] { name });
		if (cursor.getCount() > 0) {
			Cursor pwdcursor = db.rawQuery("select * from user where username=? and userpwd=?",
					new String[] { name, pwd });
			if (pwdcursor.getCount() > 0) {
				cursor.moveToFirst();
				return cursor.getInt(cursor.getColumnIndex("id"));
			} else {
				return -1;
			}
		} else {
			return 0;
		}
	}

	public int insertMusic(String mName, String path) {
		ContentValues data = new ContentValues();
		data.put("m_name", mName);
		data.put("path", path);
		return (int) db.insert("music", null, data);
	}

	public int getMusicId(String mName, String path) {
		Cursor cursor = db.rawQuery("select id from music where path=?", new String[] { path });
		if (cursor.getCount() > 0) {
			return -2;
		} else {
			return insertMusic(mName, path);
		}
	}

	public void deleteMusic(int id) {
		// 删除条件参数
		String[] whereArgs = { String.valueOf(id) };
		// 执行删除
		db.delete("music", "id=?", whereArgs);
		db.delete("music_and_singer", "m_id=?", whereArgs);
	}

	public int insertSinger(String sName) {
		ContentValues data = new ContentValues();
		data.put("s_name", sName);
		return (int) db.insert("singer", null, data);
	}

	public int getSingerId(String name) {
		Cursor cursor = db.rawQuery("select id from singer where s_name=?", new String[] { name });
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getInt(cursor.getColumnIndex("id"));
		} else {
			return insertSinger(name);
		}
	}

	public int insertMusicAndSinger(int mId, int sId) {
		ContentValues data = new ContentValues();
		data.put("m_id", mId);
		data.put("s_id", sId);
		return (int) db.insert("music_and_singer", null, data);
	}

	public Music getMusicById(int id) {
		Cursor c = db.rawQuery("select * from music where id=?", new String[] { String.valueOf(id) });
		Music m = new Music();
		c.moveToFirst();
		m.setId(c.getInt(c.getColumnIndex("id")));
		m.setName(c.getString(c.getColumnIndex("name")));
		m.setPath(c.getString(c.getColumnIndex("path")));
		return m;
	}

	public String[] getMusicSinger(int id) {
		Cursor c = db.rawQuery(
				"SELECT singer.s_name FROM music, music_and_singer, singer WHERE music.id = music_and_singer.m_id AND singer.id = music_and_singer.s_id AND music.id = ?",
				new String[] { String.valueOf(id) });
		String[] singer = new String[c.getCount()];
		while (c.moveToNext()) {
			singer[c.getPosition()] = c.getString(0);
		}
		return singer;
	}

	public List<Music> searchMusic(String key) {
		List<Music> musicList = new ArrayList<>();
		Cursor c = db.rawQuery(
				"SELECT DISTINCT music.id AS 'id', music.m_name, music.path FROM music, music_and_singer, singer \r\n"
						+ "WHERE music.id = music_and_singer.m_id AND  singer.id = music_and_singer.s_id \r\n"
						+ "AND (music.m_name LIKE '%'||?||'%' OR singer.s_name LIKE '%'||?||'%');",
				new String[] { key, key });
		while (c.moveToNext()) {
			Music m = new Music();
			m.setId(c.getInt(c.getColumnIndex("id")));
			m.setName(c.getString(c.getColumnIndex("m_name")));
			m.setPath(c.getString(c.getColumnIndex("path")));
			m.setSinger(getMusicSinger(c.getInt(c.getColumnIndex("id"))));
			musicList.add(m);
		}
		return musicList;
	}

	public List<Music> getAllMusic() {
		List<Music> musicList = new ArrayList<>();
		Cursor c = db.rawQuery("SELECT * FROM music;", null);
		while (c.moveToNext()) {
			Music m = new Music();
			m.setId(c.getInt(c.getColumnIndex("id")));
			m.setName(c.getString(c.getColumnIndex("m_name")));
			m.setPath(c.getString(c.getColumnIndex("path")));
			m.setSinger(getMusicSinger(c.getInt(c.getColumnIndex("id"))));
			musicList.add(m);
		}
		return musicList;
	}

	public List<Music> getLoveList(int uId) {
		List<Music> musicList = new ArrayList<>();
		Cursor c = db.rawQuery(
				"SELECT music.id, m_name, path FROM user JOIN love ON user.id = love.u_id JOIN music ON music.id = love.m_id WHERE user.id = ?;",
				new String[] { String.valueOf(uId) });
		while (c.moveToNext()) {
			Music m = new Music();
			m.setId(c.getInt(c.getColumnIndex("id")));
			m.setName(c.getString(c.getColumnIndex("m_name")));
			m.setPath(c.getString(c.getColumnIndex("path")));
			m.setSinger(getMusicSinger(c.getInt(c.getColumnIndex("id"))));
			musicList.add(m);
		}
		return musicList;
	}

	public void deleteLove(int uId, int mId) {
		// 删除条件参数
		String[] whereArgs = { String.valueOf(uId), String.valueOf(mId) };
		// 执行删除
		db.delete("love", "u_id=? AND m_id=?", whereArgs);
	}

	public int addLove(int uId, int mId) {
		Cursor cursor = db.rawQuery("select id from love where u_id=? AND m_id=?",
				new String[] { String.valueOf(uId), String.valueOf(mId) });
		if (cursor.getCount() > 0) {
			return -2;
		} else {
			ContentValues data = new ContentValues();
			data.put("m_id", mId);
			data.put("u_id", uId);
			return (int) db.insert("love", null, data);
		}
	}
}
