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

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.util.Log;

public class Network_Service extends Service {

	public static Network_Service ns = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		SharedPreferences users_sp = getSharedPreferences(Main_Service.USERS,
				MODE_PRIVATE);// Mac->user

		// //////////////////////// to be removed

		Editor edit = users_sp.edit();
		edit.putString(Main_Service.ANY_USER, "default");
		edit.commit();
		// ////////////////////////////////////

		ns = this;
		Main_Service.main_Service.noti("netwrk serv", "started");
		Log.i(Main_Service.LOG_TAG, "Network_Service-Oncreate--End");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// note : possible race condition bw checking for null and closed
		// condition and actually closing inside if
		if (Node_Listener.datagramSocket != null
				&& !Node_Listener.datagramSocket.isClosed()) {
			Node_Listener.datagramSocket.close();
		}
		if (Comm_Listener.serverSocket != null
				&& !Comm_Listener.serverSocket.isClosed()) {
			try {
				Comm_Listener.serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// maybe join here
		ns = null;
		Log.i(Main_Service.LOG_TAG, "Network_Service-onDestroy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (ns != null) {
			return START_STICKY;
		}
		new Thread(new Comm_Listener(Main_Service.COMM_PORT)).start();// Listen
																		// AT
																		// 6000

		new Thread(new Node_Listener(Main_Service.NET_PORT, Network.getMAC()))
				.start();// Listen At 6001

		new Thread(new Keep_Alive(Network.getMAC(), Main_Service.NET_PORT,
				Network.getBIP())).start();
		return START_STICKY;

	}
}
