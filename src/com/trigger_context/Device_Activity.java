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
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Device_Activity extends Activity {

	class DeviceDiscovery implements Runnable {

		private DatagramSocket datagramSocket;
		private byte[] buf;
		private DatagramPacket packet;
		private String userData;
		private String[] userDataArray;

		public DeviceDiscovery(int Port) {

			buf = new byte[256];
			packet = new DatagramPacket(buf, buf.length);
			try {
				datagramSocket = new DatagramSocket(Port);
				dd_socket = datagramSocket;
			} catch (SocketException e) {
				Log.i(Main_Service.LOG_TAG,
						"Device-Activity-DeviceDisc-Constructor--Error in Bind");
			}
		}

		@Override
		public void run() {
			Log.i(Main_Service.LOG_TAG,
					"Device-Activity-DeviceDisc-Run Thread Start");
			while (true) {
				try {
					datagramSocket.receive(packet);
					userData = new String(packet.getData(), "UTF-8");
					userData = userData.substring(0, packet.getLength());
					userDataArray = userData.split(";");
					if (!userDataArray[1].equals(Network.getMAC())) {
						DeviceList.add(userDataArray[0] + " -> "
								+ userDataArray[1]);
						Device_Activity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								arrayAdapter.notifyDataSetChanged();
							}
						});
					}
				} catch (UnsupportedEncodingException e) {
					Log.i(Main_Service.LOG_TAG,
							"Device-Activity-DeviceDisc-Run--Error in getData()");
				} catch (IOException e) {
					if (!datagramSocket.isClosed()) {
						Log.i(Main_Service.LOG_TAG,
								"Device-Activity-DeviceDisc-Run--Error in receive()");
					}
				}

			}
		}
	}

	class NewSendBroadcast implements Runnable {

		private String Name, MAC;
		private DatagramSocket socket;
		private int Port;
		private InetAddress BIP;
		private DatagramPacket sendPackcast;

		public NewSendBroadcast(String Name, String MAC, int Port,
				InetAddress inetAddress) {
			this.Name = Name;
			this.MAC = MAC;
			this.Port = Port;
			this.BIP = inetAddress;
			try {
				socket = new DatagramSocket();
				socket.setBroadcast(true);
			} catch (SocketException e) {
				Log.i(Main_Service.LOG_TAG,
						"SendBordcast--Error Unable to bind to port");
			}
			Log.i(Main_Service.LOG_TAG, "SendBordcast--Constructor");
		}

		@Override
		public void run() {
			Log.i(Main_Service.LOG_TAG, "SendBordcast-run--Started");

			String Packet = Name + ";" + MAC.trim() + ";1";
			Main_Service.main_Service.noti(Packet, "in send broadcast");
			byte[] SendData = Packet.getBytes();
			sendPackcast = new DatagramPacket(SendData, SendData.length, BIP,
					Port);

			try {
				socket.send(sendPackcast);
			} catch (IOException e) {
				Log.i(Main_Service.LOG_TAG,
						"Device_Activity-NewSendBoradcast-run-- Error in send");
			}
			socket.close();

		}

	}

	ArrayAdapter<String> arrayAdapter;
	ArrayList<String> DeviceList = new ArrayList<String>();
	static String ANY_USER = "00:00:00:00:00:00";
	static String USERS = "users";
	static String MY_DATA = "my_data";
	public static int PORT = 6002;
	DatagramSocket dd_socket = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_device);
		if (Main_Service.wifi) {
			SharedPreferences my_data = getSharedPreferences(MY_DATA,
					MODE_PRIVATE);

			Main_Service.main_Service.noti("before bcast", "");

			new Thread(new DeviceDiscovery(PORT)).start();

			Main_Service.main_Service.noti("aftr bcast", "");
			new Thread(new NewSendBroadcast(my_data.getString("name",
					Main_Service.DEFAULT_USER_NAME), Network.getMAC(),
					Main_Service.NET_PORT, Network.getBIP())).start();
			Toast.makeText(getBaseContext(), "Starting Device Discovery",
					Toast.LENGTH_LONG).show();

		}
		// else - no net. go to some other pg

		final ListView lv = (ListView) findViewById(R.id.DeviceList);
		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, DeviceList);
		lv.setAdapter(arrayAdapter);
		lv.setClickable(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String o = (String) lv.getItemAtPosition(position);
				String clicked[] = o.split(">");
				clicked[0] = clicked[0].substring(0, clicked[0].length() - 2);

				Intent x = new Intent(getBaseContext(), Conditions_Config.class);
				// check this
				x.putExtra("mac", clicked[1].trim());
				x.putExtra("name", clicked[0]);
				startActivity(x);
			}
		});

		Log.i(Main_Service.LOG_TAG, "Device-Activity-onCreate--End");

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dd_socket != null) {
			dd_socket.close();// close the socket to stop device discovery
								// thread
		}
		Log.i(Main_Service.LOG_TAG, "Device_Activity-onDestroy");
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
}
