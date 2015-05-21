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
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class Conditions_Config extends Activity {

	String mac = null;
	String name = null;
	final HashMap<String, Boolean> setConditions = new HashMap<String, Boolean>();
	SharedPreferences conditions;
	Editor editor;

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.condition_configuration);

		Bundle bundle = getIntent().getExtras();// check if it snull. means no
		// bundle sent

		mac = bundle.getString("mac");
		name = bundle.getString("name");

		Main_Service.main_Service.noti(mac, "condition_config:" + name);

		conditions = getSharedPreferences(mac, MODE_PRIVATE);
		setConditions.put("trigger", true);
		// Read the previous data and have the toggle button state accordingly
		Map<String, ?> cond_map = conditions.getAll();
		Toast.makeText(getApplicationContext(), cond_map.toString(),
				Toast.LENGTH_SHORT).show();
		editor = conditions.edit();

		RadioGroup trigger = (RadioGroup) findViewById(R.id.radioGroup0);
		if (cond_map.containsKey("trigger"))// previous value stored
		{
			if (conditions.getBoolean("trigger", false)) {
				RadioButton rb = (RadioButton) trigger.getChildAt(0);
				rb.setChecked(true);
			} else {
				RadioButton rb = (RadioButton) trigger.getChildAt(1);
				rb.setChecked(true);
			}
		}
		trigger.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton radioButton = (RadioButton) findViewById(checkedId);
				if (radioButton == null) {
					return;
				}
				if (radioButton.getText().equals("Arrival")) {
					setConditions.put("trigger", true);
				} else if (radioButton.getText().equals("Departure")) {
					setConditions.put("trigger", false);
				}
			}

		});
		RadioGroup bluetooth = (RadioGroup) findViewById(R.id.radioGroup1);
		if (cond_map.containsKey("bluetooth"))// previous value stored
		{
			if (conditions.getBoolean("bluetooth", false)) {
				RadioButton rb = (RadioButton) bluetooth.getChildAt(0);
				rb.setChecked(true);
			} else {
				RadioButton rb = (RadioButton) bluetooth.getChildAt(1);
				rb.setChecked(true);
			}
		}
		bluetooth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton radioButton = (RadioButton) findViewById(checkedId);
				if (radioButton == null) {
					return;
				}
				if (radioButton.getText().equals("On")) {
					setConditions.put("bluetooth", true);
				} else if (radioButton.getText().equals("Off")) {
					setConditions.put("bluetooth", false);
				}
			}

		});
		RadioGroup wifi = (RadioGroup) findViewById(R.id.radioGroup2);
		if (cond_map.containsKey("wifi")) {
			if (conditions.getBoolean("wifi", false)) {
				RadioButton rb = (RadioButton) wifi.getChildAt(0);
				rb.setChecked(true);
			} else {
				RadioButton rb = (RadioButton) wifi.getChildAt(1);
				rb.setChecked(true);
			}
		}
		wifi.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton radioButton = (RadioButton) findViewById(checkedId);
				if (radioButton == null) {
					return;
				}
				if (radioButton.getText().equals("On")) {
					setConditions.put("wifi", true);
				} else if (radioButton.getText().equals("Off")) {
					setConditions.put("wifi", false);
				}
			}

		});

		RadioGroup gps = (RadioGroup) findViewById(R.id.radioGroup3);
		if (cond_map.containsKey("gps")) {
			if (conditions.getBoolean("gps", false)) {
				RadioButton rb = (RadioButton) gps.getChildAt(0);
				rb.setChecked(true);
			} else {
				RadioButton rb = (RadioButton) gps.getChildAt(1);
				rb.setChecked(true);
			}
		}
		gps.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton radioButton = (RadioButton) findViewById(checkedId);
				if (radioButton == null) {
					return;
				}
				if (radioButton.getText().equals("On")) {
					setConditions.put("gps", true);
				} else if (radioButton.getText().equals("Off")) {
					setConditions.put("gps", false);
				}
			}

		});
		RadioGroup headset = (RadioGroup) findViewById(R.id.radioGroup4);
		if (cond_map.containsKey("headset")) {
			if (conditions.getBoolean("headset", false)) {
				RadioButton rb = (RadioButton) headset.getChildAt(0);
				rb.setChecked(true);
			} else {
				RadioButton rb = (RadioButton) headset.getChildAt(1);
				rb.setChecked(true);
			}
		}
		headset.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton radioButton = (RadioButton) findViewById(checkedId);
				if (radioButton == null) {
					return;
				}
				if (radioButton.getText().equals("On")) {
					setConditions.put("headset", true);
				} else if (radioButton.getText().equals("Off")) {
					setConditions.put("headset", false);
				}
			}

		});
		RadioGroup sms = (RadioGroup) findViewById(R.id.radioGroup5);
		if (cond_map.containsKey("sms")) {
			if (conditions.getBoolean("sms", false)) {
				RadioButton rb = (RadioButton) sms.getChildAt(0);
				rb.setChecked(true);
			} else {
				RadioButton rb = (RadioButton) sms.getChildAt(1);
				rb.setChecked(true);
			}
		}
		sms.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RadioButton radioButton = (RadioButton) findViewById(checkedId);
				if (radioButton == null) {
					return;
				}
				if (radioButton.getText().equals("On")) {
					setConditions.put("sms", true);
				} else if (radioButton.getText().equals("Off")) {
					setConditions.put("sms", false);
				}
			}

		});

		final Button actions = (Button) findViewById(R.id.button1);
		actions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(Conditions_Config.this,
						Action_Config.class);
				myIntent.putExtra("mac", mac);
				myIntent.putExtra("name", name);
				Set<String> keys = setConditions.keySet();
				for (String i : keys) {
					myIntent.putExtra(i, setConditions.get(i));
				}

				Conditions_Config.this.startActivity(myIntent);
				finish();

			}
		});

		final Button reset = (Button) findViewById(R.id.button2);
		reset.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// clear UI
				RadioGroup radioGrp = (RadioGroup) findViewById(R.id.radioGroup0);
				int selectedid = radioGrp.getCheckedRadioButtonId();
				if (selectedid != -1) {
					radioGrp.clearCheck();
				}
				radioGrp.getChildAt(0).setEnabled(true);

				radioGrp = (RadioGroup) findViewById(R.id.radioGroup1);
				selectedid = radioGrp.getCheckedRadioButtonId();
				if (selectedid != -1) {
					radioGrp.clearCheck();
				}
				radioGrp = (RadioGroup) findViewById(R.id.radioGroup2);
				selectedid = radioGrp.getCheckedRadioButtonId();
				if (selectedid != -1) {
					radioGrp.clearCheck();
				}
				radioGrp = (RadioGroup) findViewById(R.id.radioGroup3);
				selectedid = radioGrp.getCheckedRadioButtonId();
				if (selectedid != -1) {
					radioGrp.clearCheck();
				}
				radioGrp = (RadioGroup) findViewById(R.id.radioGroup4);
				selectedid = radioGrp.getCheckedRadioButtonId();
				if (selectedid != -1) {
					radioGrp.clearCheck();
				}
				radioGrp = (RadioGroup) findViewById(R.id.radioGroup5);
				selectedid = radioGrp.getCheckedRadioButtonId();
				if (selectedid != -1) {
					radioGrp.clearCheck();
				}

				setConditions.clear();// buffer to be sent to the next activity
										// is cleared
				setConditions.put("trigger", true);

				// clear existing db for the usr
				editor.clear();
				editor.commit();
			}
		});

	}

}
