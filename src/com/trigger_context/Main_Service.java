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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.trigger_context.conf.Action_Email_Client;
import com.trigger_context.conf.Action_Open_Url;
import com.trigger_context.conf.Action_Post_Tweet;

public class Main_Service extends Service implements
		OnSharedPreferenceChangeListener {

	private class SendData implements Runnable {

		String SerIP;
		int SerPo;
		String mess;

		SendData(String IP, int Po, String mes) {
			SerIP = IP;
			SerPo = Po;
			mess = mes;
		}

		void Nio() {
			try {
				InetAddress serverAddr = InetAddress.getByName(SerIP);
				DatagramSocket socket = new DatagramSocket();
				byte[] rbuf = new byte[1024];// 1024 is size.we can change it
												// our req
				DatagramPacket rpacket = new DatagramPacket(rbuf, rbuf.length);
				byte[] sbuf = mess.getBytes();
				DatagramPacket spacket = new DatagramPacket(sbuf, sbuf.length,
						serverAddr, SerPo);
				Main_Service.main_Service.noti("remote cmd", mess);
				socket.send(spacket);
				socket.receive(rpacket);
				byte[] rec = rpacket.getData();
				String recv = new String(rec);
				recv = recv.trim();
				noti("Remote Command Execution :",
						recv.equals("0") ? "Sucessful" : "UnSucesfull");
				socket.close();
			} catch (Exception e) {
				noti("Could not execute remote command due to ", e.toString());
			}
		}

		@Override
		public void run() {
			Nio();

		}

	}

	public static final String LOG_TAG = "Trigger_Log";

	private int mid = 1500;

	public static int COMM_PORT = 6000;

	public static int NET_PORT = 6001;
	static String USERS = "users";
	static String MY_DATA = "my_data";
	static String ANY_USER = "00:00:00:00:00:00";
	static String DEFAULT_USER_NAME = "userName";
	static long DEFAULT_TIME_OUT = 300 * 1000;// timeout in milli sec
	static String username = "userName";
	static long timeout = 300 * 1000;// timeout in milli sec
	static Main_Service main_Service = null;
	static ArrayList<String> conf_macs = null;
	static HashMap<String, Long> active_macs = new HashMap<String, Long>();

	static volatile boolean wifi = false;

	public String calculateMD5(File updateFile) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			noti("Exception while getting Digest", e.toString());
			return null;
		}

		InputStream is;
		try {
			is = new FileInputStream(updateFile);
		} catch (FileNotFoundException e) {
			noti("Exception while getting FileInputStream", e.toString());
			return null;
		}

		byte[] buffer = new byte[8192];
		int read;
		try {
			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			String output = bigInt.toString(16);
			// Fill to 32 chars
			output = String.format("%32s", output).replace(' ', '0');
			return output.toUpperCase();
		} catch (IOException e) {
			throw new RuntimeException("Unable to process file for MD5", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				noti("Exception on closing MD5 input stream", e.toString());
			}
		}
	}

	public void noti(String title, String txt) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setContentText(txt);

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(mid++, mBuilder.build());
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		main_Service = this;
		SharedPreferences users_sp = getSharedPreferences(Main_Service.USERS,
				MODE_PRIVATE);
		SharedPreferences data_sp = getSharedPreferences(Main_Service.MY_DATA,
				MODE_PRIVATE);
		conf_macs = new ArrayList<String>(users_sp.getAll().keySet());
		noti(conf_macs.toString(), "");
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm
				.getNetworkInfo(android.net.ConnectivityManager.TYPE_WIFI);
		Intent ServiceIntent = new Intent(this, Network_Service.class);

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
				// start service
				startService(ServiceIntent);
			}
		}
		users_sp.registerOnSharedPreferenceChangeListener(this);
		data_sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				// timeout n username can only be added or modified. not removed
				if (key.equals("username")) {
					username = sharedPreferences.getString("username",
							"defUsername");
				} else {
					timeout = sharedPreferences.getLong("timeout", 5 * 60) * 1000;
				}

			}
		});
		Log.i(LOG_TAG, "Main_Service-onCreate");
		noti("main serv", "started");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		main_Service = null;
		Log.i(LOG_TAG, "Main_Service-onDestory");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// check if key not there before - new user.
		// if key is not there, removed
		noti("in users shard pref changd", key);

		if (!conf_macs.contains(key)) {
			conf_macs.add(key);
		} else if (sharedPreferences.getString(key, null) == null) {
			conf_macs.remove(key);
		}
	}

	public synchronized void processUser(String mac, InetAddress ip) {
		noti("in process user ", mac);
		if (testConditions(mac)) {
			takeAction(mac, ip);
		}

	}

	private void recvFile(DataInputStream in, String path) {
		Log.i("recvFile", "Start");
		// path should end with "/"
		String Filename = null;
		long size = 0;
		try {
			Filename = in.readUTF();
			size = in.readLong();
		} catch (IOException e1) {
			Log.i(Main_Service.LOG_TAG,
					"recvFile--error in readins file name and lngth");
		}

		OutputStream outfile = null;
		// noti("path of recv folder:",path);
		try {
			outfile = new FileOutputStream(path + Filename);
		} catch (FileNotFoundException e1) {
			Log.i(Main_Service.LOG_TAG,
					"recvFile--Error file not found exception");
		}

		byte[] buff = new byte[1024];
		int readbytes;
		try {
			while (size > 0
					&& (readbytes = in.read(buff, 0,
							(int) Math.min(buff.length, size))) != -1) {
				try {
					outfile.write(buff, 0, readbytes);
					size -= readbytes;
				} catch (IOException e) {
					Log.i(Main_Service.LOG_TAG, "recvFile--Error file write");
				}
			}
		} catch (IOException e) {
			Log.i(Main_Service.LOG_TAG, "recvFile--Error socket read");
			e.printStackTrace();
		}
		try {
			outfile.close();
		} catch (IOException e) {
			Log.i(Main_Service.LOG_TAG, "recvFile--Erro oufile close");
		}
	}

	public void senderSync(DataInputStream in, DataOutputStream out,
			String folder) {
		String tfolder = folder
				+ (folder.charAt(folder.length() - 1) == '/' ? "" : "/");
		File f = new File(folder);
		File file[] = f.listFiles();
		// noti(file.toString(),"");
		String md5 = null;
		HashMap<String, File> hm = new HashMap<String, File>();

		HashSet<String> A = new HashSet<String>();
		for (File element : file) {
			hm.put(md5 = calculateMD5(element), element);
			A.add(md5);
		}
		// noti(hm.toString(),"");
		int numB = 0;
		try {
			numB = in.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			noti("error reading 1st int in sendersync", "");
			e.printStackTrace();
		}
		HashSet<String> B = new HashSet<String>();
		for (int i = 0; i < numB; i++) {
			try {
				B.add(in.readUTF());
			} catch (IOException e1) {
				noti("error in readins md5", "");
				e1.printStackTrace();
			}
		}
		HashSet<String> aMb = new HashSet<String>(A);
		aMb.removeAll(B);
		int l1 = aMb.size();
		try {
			out.writeInt(l1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			noti("error in writing 1st int", "");
			e.printStackTrace();
		}
		Iterator<String> itr = aMb.iterator();
		while (itr.hasNext()) {
			f = hm.get(itr.next());
			sendFile(out, f.getPath());
		}
		HashSet<String> bMa = new HashSet<String>(B);
		bMa.removeAll(A);
		int l2 = bMa.size();
		try {
			out.writeInt(l2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			noti("error in writing 2nd int", "");
			e.printStackTrace();
		}
		itr = bMa.iterator();
		while (itr.hasNext()) {
			md5 = itr.next();
			try {
				out.writeUTF(md5);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				noti("error in sending md5", "");
				e.printStackTrace();
			}
			recvFile(in, folder);
		}
	}

	private void sendFile(DataOutputStream out, String Path) {
		Log.i(Main_Service.LOG_TAG, "SendFile--Start");
		File infile = new File(Path);
		String FileName = null;
		try {

			FileName = Path.substring(Path.lastIndexOf("/") + 1);
			out.writeUTF(FileName);
			out.writeLong(infile.length());
		} catch (IOException e) {
			Log.i(Main_Service.LOG_TAG,
					"SendFile--error sending filename length");
		}

		byte[] mybytearray = new byte[(int) infile.length()];

		FileInputStream fis = null;
		;
		try {
			fis = new FileInputStream(infile);
		} catch (FileNotFoundException e1) {
			Log.i(Main_Service.LOG_TAG, "sendFile--Error file not found");
		}
		BufferedInputStream bis = new BufferedInputStream(fis);

		DataInputStream dis = new DataInputStream(bis);
		try {
			dis.readFully(mybytearray, 0, mybytearray.length);
		} catch (IOException e1) {
			Log.i(Main_Service.LOG_TAG,
					"sendFile--Error while reading bytes from file");

		}

		try {
			out.write(mybytearray, 0, mybytearray.length);
		} catch (IOException e1) {
			Log.i(Main_Service.LOG_TAG, "sendFile--error while sending");
		}

		try {
			dis.close();
			bis.close();
			fis.close();
		} catch (IOException e) {

			Log.i(Main_Service.LOG_TAG, "sendFile--error in closing streams");
		}

	}

	private void takeAction(String mac, InetAddress ip) {
		noti("comes to ", mac);

		SharedPreferences conditions = getSharedPreferences(mac, MODE_PRIVATE);
		Map<String, ?> cond_map = conditions.getAll();
		Set<String> key_set = cond_map.keySet();
		Main_Service.main_Service.noti(cond_map.toString(), "");
		if (key_set.contains("SmsAction")) {
			String number = conditions.getString("SmsActionNumber", null);
			String message = conditions.getString("SmsActionMessage", null);

			try {
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(number, null, message, null, null);
				noti("Sms Sent To : ", "" + number);
			} catch (Exception e) {
				noti("Sms Sending To ", number + "Failed");
			}
		}
		if (key_set.contains("OpenWebsiteAction")) {
			Intent dialogIntent = new Intent(getBaseContext(),
					Action_Open_Url.class);
			dialogIntent.putExtra("urlAction",
					(String) cond_map.get("urlAction"));
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(dialogIntent);

		}
		if (key_set.contains("ToggleAction")) {
			if (key_set.contains("bluetoothAction")
					&& conditions.getBoolean("bluetoothAction", false)) {
				final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
						.getDefaultAdapter();
				bluetoothAdapter.enable();
			} else if (key_set.contains("bluetoothAction")) {
				final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
						.getDefaultAdapter();
				bluetoothAdapter.disable();
			}
			if (key_set.contains("wifiAction")
					&& conditions.getBoolean("wifiAction", false)) {
				final WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				wm.setWifiEnabled(true);
			} else if (key_set.contains("wifiAction")) {
				final WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
				wm.setWifiEnabled(false);
			}
		}
		if (key_set.contains("TweetAction")) {
			Intent dialogIntent = new Intent(getBaseContext(),
					Action_Post_Tweet.class);
			dialogIntent.putExtra("tweetTextAction",
					(String) cond_map.get("tweetTextAction"));
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(dialogIntent);
		}

		if (key_set.contains("EmailAction")) {
			Intent dialogIntent = new Intent(getBaseContext(),
					Action_Email_Client.class);
			dialogIntent
					.putExtra("toAction", (String) cond_map.get("toAction"));
			dialogIntent.putExtra("subjectAction",
					(String) cond_map.get("subjectAction"));
			dialogIntent.putExtra("emailMessageAction",
					(String) cond_map.get("emailMessageAction"));
			dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplication().startActivity(dialogIntent);
		}
		if (key_set.contains("RemoteServerCmd")) {

			String text = conditions.getString("cmd", null);
			new Thread(new SendData("224.0.0.1", 9876, text)).start();
		}
		// network activities from here.
		// in all network actions send username first
		if (key_set.contains("FileTransferAction"))

		{
			String path = conditions.getString("filePath", null);
			SharedPreferences my_data = getSharedPreferences(
					Main_Service.MY_DATA, MODE_PRIVATE);
			String usrName = my_data.getString("name", "userName");
			// type 1
			try {
				Socket socket = new Socket(ip, COMM_PORT);
				DataOutputStream out = null;
				out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF(usrName);
				out.writeInt(1);
				sendFile(out, path);

				noti("Sent " + path + " file to :",
						getSharedPreferences(Main_Service.USERS, MODE_PRIVATE)
								.getString("name", "userName"));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (key_set.contains("ccfMsgAction"))

		{
			String msg = conditions.getString("ccfMsg", null);
			SharedPreferences my_data = getSharedPreferences(
					Main_Service.MY_DATA, MODE_PRIVATE);
			String usrName = my_data.getString("name", "userName");
			// type 2 is msg
			try {
				Socket socket = new Socket(ip, COMM_PORT);
				DataOutputStream out = null;
				out = new DataOutputStream(socket.getOutputStream());
				out.writeUTF(usrName);
				out.writeInt(2);
				out.writeUTF(msg);

				noti("Sent msg : '" + msg + "'",
						"to : "
								+ getSharedPreferences(Main_Service.USERS,
										MODE_PRIVATE).getString("name",
										"userName"));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (key_set.contains("sync")) {
			SharedPreferences my_data = getSharedPreferences(
					Main_Service.MY_DATA, MODE_PRIVATE);
			String usrName = my_data.getString("name", "userName");
			// type 3 is sync
			try {
				Socket socket = new Socket(ip, COMM_PORT);
				DataInputStream in = new DataInputStream(
						socket.getInputStream());
				DataOutputStream out = new DataOutputStream(
						socket.getOutputStream());
				out.writeUTF(usrName);
				out.writeInt(3);
				senderSync(in, out, Environment.getExternalStorageDirectory()
						.getPath() + "/TriggerSync/");

				noti("Synced with:",
						getSharedPreferences(Main_Service.USERS, MODE_PRIVATE)
								.getString("name", "userName"));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean testConditions(String mac) {
		SharedPreferences conditions = getSharedPreferences(mac, MODE_PRIVATE);
		Map<String, ?> cond_map = conditions.getAll();
		Set<String> key_set = cond_map.keySet();
		boolean takeAction = true;
		if (key_set.contains("bluetooth")) {
			// checking the current state against the state set by the user
			final BluetoothAdapter bluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			takeAction = new Boolean(bluetoothAdapter.isEnabled())
					.equals(conditions.getString("bluetooth", "false"));
		}
		if (takeAction && key_set.contains("wifi")) {
			final WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			takeAction = new Boolean(wm.isWifiEnabled()) == conditions
					.getBoolean("wifi", false);
		}

		if (takeAction && key_set.contains("gps")) {
			final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			takeAction = new Boolean(
					locationManager
							.isProviderEnabled(LocationManager.GPS_PROVIDER))
					.equals(conditions.getString("gps", "false"));
		}
		if (takeAction && key_set.contains("sms")) {
			final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
			Cursor c = getContentResolver().query(SMS_INBOX, null, "read = 0",
					null, null);
			if (c != null) {
				int unreadMessagesCount = c.getCount();
				c.close();
				takeAction = new Boolean(unreadMessagesCount > 0)
						.equals(conditions.getString("sms", "false"));
			} else {
				takeAction = false;
			}
		}

		// "NOT TESTED" head set, missed call, accelerometer, proximity, gyro,
		// orientation
		if (takeAction && key_set.contains("headset")) {
			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			takeAction = am.isMusicActive() == conditions.getBoolean("headset",
					false);
			// am.isWiredHeadsetOn() is deprecated

		}
		/*
		 * if(takeAction && key_set.contains("missedCall")) { final String[]
		 * projection = null; final String selection = null; final String[]
		 * selectionArgs = null; final String sortOrder =
		 * android.provider.CallLog.Calls.DATE + " DESC"; Cursor cursor = null;
		 * try{ cursor = getApplicationContext().getContentResolver().query(
		 * Uri.parse("content://call_log/calls"), projection, selection,
		 * selectionArgs, sortOrder); while (cursor.moveToNext()) { String
		 * callLogID =
		 * cursor.getString(cursor.getColumnIndex(android.provider.CallLog
		 * .Calls._ID)); String callNumber =
		 * cursor.getString(cursor.getColumnIndex
		 * (android.provider.CallLog.Calls.NUMBER)); String callDate =
		 * cursor.getString
		 * (cursor.getColumnIndex(android.provider.CallLog.Calls.DATE)); String
		 * callType =
		 * cursor.getString(cursor.getColumnIndex(android.provider.CallLog
		 * .Calls.TYPE)); String isCallNew =
		 * cursor.getString(cursor.getColumnIndex
		 * (android.provider.CallLog.Calls.NEW)); if(Integer.parseInt(callType)
		 * == android.provider.CallLog.Calls.MISSED_CALL_TYPE &&
		 * Integer.parseInt(isCallNew) > 0){
		 * 
		 * } } }catch(Exception ex){ }finally{ cursor.close(); }
		 * 
		 * }
		 */
		return takeAction;
	}

}
