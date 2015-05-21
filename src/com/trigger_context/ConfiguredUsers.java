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

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ConfiguredUsers extends Activity {

	SharedPreferences users;
	ArrayAdapter<String> arrayAdapter;
	ArrayList<String> UserList = new ArrayList<String>();
	final Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configured_users);

		users = getSharedPreferences(Main_Service.USERS, MODE_PRIVATE);
		Map<String, ?> user_map = users.getAll();
		for (Map.Entry<String, ?> entry : user_map.entrySet()) {
			UserList.add(entry.getKey() + " -> " + entry.getValue().toString());

			final ListView lv = (ListView) findViewById(R.id.UserList);
			arrayAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, UserList);
			lv.setAdapter(arrayAdapter);
			lv.setClickable(true);

			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					String user = (String) lv.getItemAtPosition(position);
					String clicked[] = user.split(">");
					clicked[0] = clicked[0].substring(0,
							clicked[0].length() - 2);

					Intent x = new Intent(getBaseContext(),
							Conditions_Config.class);
					// check this
					x.putExtra("mac", clicked[0].trim());
					x.putExtra("name", clicked[1]);
					startActivity(x);
				}
			});
			lv.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						final int position, long id) {

					String user = (String) lv.getItemAtPosition(position);
					final String clicked[] = user.split(">");
					clicked[0] = clicked[0].substring(0,
							clicked[0].length() - 2);
					clicked[0].trim();
					clicked[1].trim();
					// Log.d(tag, msg)
					AlertDialog.Builder alert = new AlertDialog.Builder(context);

					alert.setTitle("Remove");
					alert.setMessage("Are you sure you want to delete permanently?");

					alert.setPositiveButton("Confirm",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									removeUserFromList();
								}

								private void removeUserFromList() {
									Editor edit = users.edit();
									edit.remove(clicked[0]);
									edit.commit();
									SharedPreferences conditions = getSharedPreferences(
											clicked[0], MODE_PRIVATE);
									edit = conditions.edit();
									edit.clear();
									edit.commit();
									UserList.remove(position);
									arrayAdapter.notifyDataSetChanged();
								}
							});

					alert.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							});

					alert.show();
					return true;
				}
			});

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.config_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
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

}
