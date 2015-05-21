/*******************************************************************************
 *   Copyright 2013-2015 Karishma Sureka , Sai Gopal , Vijay Teja
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *******************************************************************************/
package com.trigger_context;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class Main_Activity extends Activity {

	private int mid = 0;
	public static Main_Activity main_activity;

	public void goToDeviceActivity(View a) {
		Intent x = new Intent(getBaseContext(), Device_Activity.class);
		startActivity(x);
	}

	public void noti(String title, String txt) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setContentText(txt);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mid allows you to update the notification later on.
		mNotificationManager.notify(mid++, mBuilder.build());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		main_activity = this;
		Start_MainService();

		Start_NetworkService();

		Log.i(Main_Service.LOG_TAG, "Main_Activity-onCreate--End");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_settings2:
			Intent ConfiguredUsers = new Intent(getBaseContext(),
					ConfiguredUsers.class);
			startActivity(ConfiguredUsers);
			return true;
		case R.id.action_settings3:
			Intent AddUser = new Intent(getBaseContext(), AddUser.class);
			startActivity(AddUser);
			return true;
		case R.id.action_settings4:
			Intent Settings = new Intent(getBaseContext(), Settings.class);
			startActivity(Settings);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void Start_MainService() {
		if (Main_Service.main_Service != null) {
			return;
		}
		Context x = getBaseContext();
		Intent startServiceIntent = new Intent(x, Main_Service.class);
		x.startService(startServiceIntent);
		Toast.makeText(x, "Starting Main_Service", Toast.LENGTH_LONG).show();
	}

	private void Start_NetworkService() {
		if (!Main_Service.wifi) {
			return;
		} else if (Main_Service.wifi && Network_Service.ns != null) {
			// but
			// Network
			// Service
			// is
			// already
			// running
			return;
		}

		Context x = getBaseContext();
		Intent startServiceIntent = new Intent(x, Network_Service.class);
		x.startService(startServiceIntent);
		Toast.makeText(x, "Starting Network_Service", Toast.LENGTH_LONG).show();
	}
}
