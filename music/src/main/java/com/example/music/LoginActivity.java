package com.example.music;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.db.SqliteDB;
import com.example.model.User;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity implements View.OnClickListener {
	Button btn, login, logup;
	EditText username, password;
	TextView loginStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initLogin();
	}

	private void initLogin() {
		login = (Button) findViewById(R.id.btn_login);
		logup = (Button) findViewById(R.id.btn_logup);
		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		loginStatus = (TextView) findViewById(R.id.login_status);

		login.setOnClickListener(this);
		logup.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		String uName = username.getText().toString();
		String uPwd = password.getText().toString();

		int reslt;
		switch (v.getId()) {
		case R.id.btn_login:
			reslt = SqliteDB.getInstance(LoginActivity.this).queryUser(uName, uPwd);
			if (reslt > 0) {
				loginStatus.setText("登录成功");
				SharedPreferences sp = getSharedPreferences("login", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.clear();
				editor.putString("login_time",
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA).format(new Date()));
				editor.putInt("user_id", reslt);
				editor.putString("username", uName);
				editor.commit();
				startActivity(new Intent(this, MainActivity.class));
			} else if (reslt == -1) {
				loginStatus.setText("密码错误");
			} else if (reslt == 0) {
				loginStatus.setText("用户名不存在");
			}
			break;
		case R.id.btn_logup:
			User user = new User();
			user.setUsername(uName);
			user.setUserpwd(uPwd);
			reslt = SqliteDB.getInstance(LoginActivity.this).saveUSer(user);
			if (reslt == 1) {
				loginStatus.setText("注册成功");
			} else if (reslt == -1) {
				loginStatus.setText("用户名已存在");
			} else {
				loginStatus.setText("!");
			}
			break;
		}
	}

}
