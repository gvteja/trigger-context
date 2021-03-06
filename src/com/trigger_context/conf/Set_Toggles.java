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
package com.trigger_context.conf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.trigger_context.R;

public class Set_Toggles extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_toggles);
		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Bundle bundle = new Bundle();
				bundle.putBoolean("ToggleAction", true);
				RadioGroup bluetooth = (RadioGroup) findViewById(R.id.radioGroup1);
				RadioButton rb = (RadioButton) bluetooth.getChildAt(0);
				RadioButton rb1 = (RadioButton) bluetooth.getChildAt(1);
				if (rb.isChecked()) {
					bundle.putBoolean("bluetoothAction", true);
				} else if (rb1.isChecked()) {
					bundle.putBoolean("bluetoothAction", false);
				}
				RadioGroup wifi = (RadioGroup) findViewById(R.id.radioGroup2);
				RadioButton rb2 = (RadioButton) wifi.getChildAt(0);
				RadioButton rb3 = (RadioButton) wifi.getChildAt(1);
				if (rb2.isChecked()) {
					bundle.putBoolean("wifiAction", true);
				} else if (rb3.isChecked()) {
					bundle.putBoolean("wifiAction", false);
				}
				Intent mIntent = new Intent();
				mIntent.putExtras(bundle);
				setResult(RESULT_OK, mIntent);
				Toast.makeText(getApplicationContext(),
						"Toggle settings saved!", Toast.LENGTH_SHORT).show();
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}