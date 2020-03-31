package com.example.OpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class OpenHelper extends SQLiteOpenHelper {
	public static String CREATE_USER = "create table user (id integer primary key autoincrement, username text, userpwd text)",
			CREATE_MUSIC = "create table music (id integer primary key autoincrement, m_name text, path text)",
			CREATE_SINGER = "CREATE TABLE singer (id integer primary key autoincrement, s_name text)",
			CREATE_MUSIC_AND_SINGER = "CREATE TABLE music_and_singer (id integer primary key autoincrement, m_id integer, s_id integer)",
			CREATE_LOVE = "CREATE TABLE love (id integer primary key autoincrement, u_id integer, m_id integer)";

	public OpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_USER);
		db.execSQL(CREATE_MUSIC);
		db.execSQL(CREATE_SINGER);
		db.execSQL(CREATE_MUSIC_AND_SINGER);
		db.execSQL(CREATE_LOVE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

}
