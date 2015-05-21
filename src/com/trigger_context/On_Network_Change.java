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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class On_Network_Change extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(Main_Service.LOG_TAG, "OnNetworkChange-OnReceive");
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm
				.getNetworkInfo(android.net.ConnectivityManager.TYPE_WIFI);

		Intent ServiceIntent = new Intent(context, Network_Service.class);

		if (info != null) {
			if (info.isConnected()) {
				Main_Service.wifi = true;
				Network.setWifiOn(true);
				Thread z = new Thread(new Network());

				z.start();

				try {
					z.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Toast.makeText(context, "Starting Network_Service",
						Toast.LENGTH_LONG).show();
				// start service
				if (Network_Service.ns == null) {
					context.startService(ServiceIntent);
				}
				Log.i(Main_Service.LOG_TAG, "OnNetworkChange--Start Service");

			} else {
				Main_Service.wifi = false;
				Network.setWifiOn(false);
				// stop service
				if (Network_Service.ns != null) {
					context.stopService(ServiceIntent);
				}
				Toast.makeText(context, "Stoping Network_Service",
						Toast.LENGTH_LONG).show();
				Log.i(Main_Service.LOG_TAG, "OnNetworkChange--Stop Service");

			}
		}
	}
}