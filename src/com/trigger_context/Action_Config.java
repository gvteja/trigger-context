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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.trigger_context.conf.Set_File_Select;
import com.trigger_context.conf.Set_Open_Url;
import com.trigger_context.conf.Set_Post_Tweet;
import com.trigger_context.conf.Set_Send_Email;
import com.trigger_context.conf.Set_Send_Msg;
import com.trigger_context.conf.Set_Send_Sms;
import com.trigger_context.conf.Set_Server_Cmd;
import com.trigger_context.conf.Set_Toggles;

public class Action_Config extends Activity {

	String mac, name;
	final HashMap<String, Object> setConditions = new HashMap<String, Object>();
	SharedPreferences conditions;
	Editor editor;
	boolean flag = false;
	boolean resetFlag = false;
	String conditionToggles[] = { "bluetooth", "wifi", "gps", "headset", "sms",
			"trigger" };
	String actionToggles[] = { "SmsAction", "EmailAction", "RemoteServerCmd",
			"ToggleAction", "TweetAction", "OpenWebsiteAction",
			"bluetoothAction", "wifiAction", "FileTransferAction",
			"ccfMsgAction", "sync" };
	String actionStrings[] = { "SmsActionNumber", "SmsActionMessage",
			"toAction", "subjectAction", "emailMessageAction", "cmd",
			"tweetTextAction", "urlAction", "filePath", "ccfMsg" };

	HashSet<String> boolBuffer = new HashSet<String>();

	HashSet<String> stringBuffer = new HashSet<String>();
	{
		for (String i : conditionToggles) {
			boolBuffer.add(i);
		}
		for (String i : actionToggles) {
			boolBuffer.add(i);
		}
		for (String i : actionStrings) {
			stringBuffer.add(i);
		}
	}

	private void commitSettings() {
		if (resetFlag) {
			// reset was performed. Clear all Actions from db for the given usr.
			for (String i : actionToggles) {
				if (conditions.contains(i)) {
					editor.remove(i);
				}
			}
			for (String i : actionStrings) {
				if (conditions.contains(i)) {
					editor.remove(i);
				}
			}
			resetFlag = false;
		} else {
			Set<String> keys = setConditions.keySet();
			for (String i : keys) {
				if (boolBuffer.contains(i)) {
					editor.putBoolean(i, (Boolean) setConditions.get(i));
				} else if (stringBuffer.contains(i)) {
					editor.putString(i, (String) setConditions.get(i));
				}
			}

			SharedPreferences users;
			Editor edit;
			users = getSharedPreferences(Main_Service.USERS, MODE_PRIVATE);
			edit = users.edit();
			Main_Service.main_Service.noti(mac, name);
			edit.putString(mac.trim(), name);

			edit.commit();
		}
		editor.commit();

	}

	public void leaveToDat() {
		Intent myIntent = new Intent(Action_Config.this, Device_Activity.class);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		Action_Config.this.startActivity(myIntent);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			Set<String> keys = bundle.keySet();
			for (String i : keys) {
				setConditions.put(i, bundle.get(i));
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (flag == false) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Save");
			alert.setMessage("Do you wish you save the changes made?");

			alert.setPositiveButton("Save",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {

							flag = true;
							commitSettings();
							conditions = getSharedPreferences(mac, MODE_PRIVATE);
							Map<String, ?> cond_map = conditions.getAll();
							Toast.makeText(getApplicationContext(),
									"Actions saved!", Toast.LENGTH_SHORT)
									.show();
							Toast.makeText(getApplicationContext(),
									cond_map.toString(), Toast.LENGTH_SHORT)
									.show();

							// DBACtivity
							leaveToDat();
						}
					});

			alert.setNegativeButton("Don't Save",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							leaveToDat();
						}
					});

			alert.show();
			return;
		} else {
			leaveToDat();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.action_configuration);
		Bundle bundle = getIntent().getExtras();
		mac = bundle.getString("mac");
		name = bundle.getString("name");

		for (String i : conditionToggles) {
			if (bundle.containsKey(i)) {
				setConditions.put(i, bundle.getBoolean(i));
			}
		}

		conditions = getSharedPreferences(mac, MODE_PRIVATE);
		Map<String, ?> cond_map = conditions.getAll();
		Toast.makeText(getApplicationContext(), cond_map.toString(),
				Toast.LENGTH_SHORT).show();
		editor = conditions.edit();

		RadioGroup SmsAction = (RadioGroup) findViewById(R.id.radioGroup1);
		if (cond_map.containsKey("SmsAction")) {
			if (conditions.getBoolean("SmsAction", false)) {
				RadioButton rb = (RadioButton) SmsAction.getChildAt(0);
				rb.setChecked(true);
			}
		}

		SmsAction.getChildAt(0).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Action_Config.this,
						Set_Send_Sms.class);
				Action_Config.this.startActivityForResult(myIntent, 11);

			}
		});
		RadioGroup EmailAction = (RadioGroup) findViewById(R.id.radioGroup2);
		if (cond_map.containsKey("EmailAction")) {
			if (conditions.getBoolean("EmailAction", false)) {
				RadioButton rb = (RadioButton) EmailAction.getChildAt(0);
				rb.setChecked(true);
			}
		}
		EmailAction.getChildAt(0).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Action_Config.this,
						Set_Send_Email.class);
				Action_Config.this.startActivityForResult(myIntent, 12);

			}
		});
		RadioGroup TweetAction = (RadioGroup) findViewById(R.id.radioGroup3);
		if (cond_map.containsKey("TweetAction")) {
			if (conditions.getBoolean("TweetAction", false)) {
				RadioButton rb = (RadioButton) TweetAction.getChildAt(0);
				rb.setChecked(true);
			}
		}
		TweetAction.getChildAt(0).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Action_Config.this,
						Set_Post_Tweet.class);
				Action_Config.this.startActivityForResult(myIntent, 12);

			}
		});
		RadioGroup ToggleAction = (RadioGroup) findViewById(R.id.radioGroup4);
		if (cond_map.containsKey("ToggleAction")) {
			if (conditions.getBoolean("ToggleAction", false)) {
				RadioButton rb = (RadioButton) ToggleAction.getChildAt(0);
				rb.setChecked(true);
			}
		}
		ToggleAction.getChildAt(0).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Action_Config.this,
						Set_Toggles.class);
				Action_Config.this.startActivityForResult(myIntent, 12);

			}
		});
		RadioGroup OpenWebsiteAction = (RadioGroup) findViewById(R.id.radioGroup5);
		if (cond_map.containsKey("OpenWebsiteAction")) {
			if (conditions.getBoolean("OpenWebsiteAction", false)) {
				RadioButton rb = (RadioButton) OpenWebsiteAction.getChildAt(0);
				rb.setChecked(true);
			}
		}
		OpenWebsiteAction.getChildAt(0).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent myIntent = new Intent(Action_Config.this,
								Set_Open_Url.class);
						Action_Config.this.startActivityForResult(myIntent, 12);

					}
				});
		RadioGroup RemoteServerCmd = (RadioGroup) findViewById(R.id.radioGroup6);
		if (cond_map.containsKey("RemoteServerCmd")) {
			if (conditions.getBoolean("RemoteServerCmd", false)) {
				RadioButton rb = (RadioButton) RemoteServerCmd.getChildAt(0);
				rb.setChecked(true);
			}
		}
		RemoteServerCmd.getChildAt(0).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Action_Config.this,
						Set_Server_Cmd.class);
				Action_Config.this.startActivityForResult(myIntent, 12);

			}
		});
		RadioGroup FileTransferAction = (RadioGroup) findViewById(R.id.radioGroup7);
		if (cond_map.containsKey("FileTransferAction")) {
			if (conditions.getBoolean("FileTransferAction", false)) {
				RadioButton rb = (RadioButton) FileTransferAction.getChildAt(0);
				rb.setChecked(true);
			}
		}
		FileTransferAction.getChildAt(0).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent myIntent = new Intent(Action_Config.this,
								Set_File_Select.class);
						Action_Config.this.startActivityForResult(myIntent, 12);

					}
				});

		RadioGroup sendMsgAction = (RadioGroup) findViewById(R.id.radioGroup8);
		if (cond_map.containsKey("ccfMsgAction")) {
			if (conditions.getBoolean("ccfMsgAction", false)) {
				RadioButton rb = (RadioButton) sendMsgAction.getChildAt(0);
				rb.setChecked(true);
			}
		}

		sendMsgAction.getChildAt(0).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Action_Config.this,
						Set_Send_Msg.class);
				Action_Config.this.startActivityForResult(myIntent, 11);

			}
		});
		RadioGroup Sync = (RadioGroup) findViewById(R.id.radioGroup9);
		if (cond_map.containsKey("sync")) {
			if (conditions.getBoolean("sync", false)) {
				RadioButton rb = (RadioButton) Sync.getChildAt(0);
				rb.setChecked(true);
			}
		}

		Sync.getChildAt(0).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setConditions.put("sync", true);

			}
		});

		final Button save = (Button) findViewById(R.id.button1);
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				flag = true;
				commitSettings();
				conditions = getSharedPreferences(mac, MODE_PRIVATE);
				Map<String, ?> cond_map = conditions.getAll();
				Toast.makeText(getApplicationContext(), "Actions saved!",
						Toast.LENGTH_SHORT).show();
				Toast.makeText(getApplicationContext(), cond_map.toString(),
						Toast.LENGTH_SHORT).show();

				// DBACtivity
				leaveToDat();
			}

		});

		final Button reset = (Button) findViewById(R.id.button2);
		reset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetFlag = true;

				// need to clear all selected options in the UI
				RadioGroup radio = (RadioGroup) findViewById(R.id.radioGroup1);
				((RadioButton) radio.getChildAt(0)).setChecked(false);
				radio = (RadioGroup) findViewById(R.id.radioGroup2);
				((RadioButton) radio.getChildAt(0)).setChecked(false);
				radio = (RadioGroup) findViewById(R.id.radioGroup3);
				((RadioButton) radio.getChildAt(0)).setChecked(false);
				radio = (RadioGroup) findViewById(R.id.radioGroup4);
				((RadioButton) radio.getChildAt(0)).setChecked(false);
				radio = (RadioGroup) findViewById(R.id.radioGroup5);
				((RadioButton) radio.getChildAt(0)).setChecked(false);
				radio = (RadioGroup) findViewById(R.id.radioGroup6);
				((RadioButton) radio.getChildAt(0)).setChecked(false);
				radio = (RadioGroup) findViewById(R.id.radioGroup7);
				((RadioButton) radio.getChildAt(0)).setChecked(false);

				setConditions.clear();

				commitSettings();

				/* ActionConfig.this.recreate(); */
			}

		});
	}

}
