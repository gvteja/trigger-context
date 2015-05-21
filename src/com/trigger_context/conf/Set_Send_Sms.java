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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trigger_context.R;

public class Set_Send_Sms extends Activity {

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Uri uri = data.getData();

			if (uri != null) {
				Cursor c = null;
				try {
					c = getContentResolver()
							.query(uri,
									new String[] {
											ContactsContract.CommonDataKinds.Phone.NUMBER,
											ContactsContract.CommonDataKinds.Phone.TYPE },
									null, null, null);

					if (c != null && c.moveToFirst()) {
						String number = c.getString(0);
						int type = c.getInt(1);
						showSelectedNumber(type, number);
					}
				} finally {
					if (c != null) {
						c.close();
					}
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_sms);
		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Bundle bundle = new Bundle();
				bundle.putBoolean("SmsAction", true);
				bundle.putString("SmsActionNumber",
						((EditText) findViewById(R.id.editText1)).getText()
								.toString());
				bundle.putString("SmsActionMessage",
						((EditText) findViewById(R.id.editText2)).getText()
								.toString());
				Intent mIntent = new Intent();
				mIntent.putExtras(bundle);
				setResult(RESULT_OK, mIntent);
				Toast.makeText(getApplicationContext(), "Sms settings saved!",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		});
		final Button pickContact = (Button) findViewById(R.id.button2);
		pickContact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				startActivityForResult(intent, 1);
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

	public void showSelectedNumber(int type, String number) {
		EditText editText = (EditText) findViewById(R.id.editText1);
		editText.setText(number, TextView.BufferType.EDITABLE);
	}

}