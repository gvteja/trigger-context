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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddUser extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_user);
		final Button saveConfigure = (Button) findViewById(R.id.button1);
		saveConfigure.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(getBaseContext(),
						Conditions_Config.class);
				String mac = ((EditText) findViewById(R.id.editText2))
						.getText().toString();
				String name = ((EditText) findViewById(R.id.editText1))
						.getText().toString();
				mac.trim();
				if (validMAC(mac)) {
					myIntent.putExtra("mac", mac);
					myIntent.putExtra("name", name);
					Main_Service.main_Service.noti(mac, name);
					startActivity(myIntent);
				} else {
					Toast.makeText(getBaseContext(),
							"Enter a valid MAC address (xx:xx:xx:xx:xx:xx)",
							Toast.LENGTH_LONG).show();

				}
			}
		});

	}

	boolean validMAC(String Test) {
		return Test.matches("^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$");
	}

}
