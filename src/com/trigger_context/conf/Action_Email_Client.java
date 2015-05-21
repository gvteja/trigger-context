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
import android.net.Uri;
import android.os.Bundle;

public class Action_Email_Client extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { bundle.getString("toAction", null) });
		sendIntent.setData(Uri.parse(bundle.getString("toAction", null)));
		sendIntent.putExtra(Intent.EXTRA_SUBJECT,
				bundle.getString("subjectAction", null));
		sendIntent.setType("plain/text");
		sendIntent.putExtra(Intent.EXTRA_TEXT,
				bundle.getString("emailMessageAction", null));
		sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(sendIntent);
		finish();
	}
}
