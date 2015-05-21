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

import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Network implements Runnable {

	static private boolean WifiOn;
	static private String IP;
	static private String MAC;
	static private InetAddress BIP;

	public static synchronized InetAddress getBIP() {
		if (isWifiOn()) {
			return BIP;
		} else {
			return null;
		}
	}

	private static String getDeviceMAC() {

		Log.i(Main_Service.LOG_TAG, "Network-getDeviceMAC--Start");
		WifiManager wifiManager = (WifiManager) Main_Service.main_Service
				.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return wifiInfo.getMacAddress();
	}

	public static synchronized String getIP() {
		if (isWifiOn()) {
			return IP;
		} else {
			return null;
		}
	}

	public static synchronized String getMAC() {
		if (isWifiOn()) {
			return MAC;
		} else {
			return null;
		}
	}

	public static boolean isWifiOn() {
		return WifiOn;
	}

	private static synchronized void setBIP(InetAddress bIP) {
		BIP = bIP;
	}

	private static synchronized void setIP(String iP) {
		IP = iP;
	}

	private static synchronized void setMAC(String mAC) {
		MAC = mAC;
	}

	public static void setWifiOn(boolean wifiOn) {
		WifiOn = wifiOn;
	}

	private InetAddress getDeviceBIP() {
		Log.i(Main_Service.LOG_TAG, "Network-getDeviceBIP--Start");

		WifiManager wifi = (WifiManager) Main_Service.main_Service
				.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();

		int broadcast = dhcp.ipAddress & dhcp.netmask | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++) {
			quads[k] = (byte) (broadcast >> k * 8 & 0xFF);
		}
		try {
			return InetAddress.getByAddress(quads);

		} catch (UnknownHostException e) {
			Log.i(Main_Service.LOG_TAG, "Network-getDeviceBIP--Start");
			return null;
		}
	}

	private String getDeviceIP() {
		Log.i(Main_Service.LOG_TAG, "Network-getDeviceIP--Start");

		WifiManager wifiManager = (WifiManager) Main_Service.main_Service
				.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		String ipBinary = Integer.toBinaryString(ipAddress);

		while (ipBinary.length() < 32) {
			ipBinary = "0" + ipBinary;
		}

		String a = ipBinary.substring(0, 8);
		String b = ipBinary.substring(8, 16);
		String c = ipBinary.substring(16, 24);
		String d = ipBinary.substring(24, 32);

		String actualIpAddress = Integer.parseInt(d, 2) + "."
				+ Integer.parseInt(c, 2) + "." + Integer.parseInt(b, 2) + "."
				+ Integer.parseInt(a, 2);
		return actualIpAddress;
	}

	@Override
	public void run() {
		Log.i(Main_Service.LOG_TAG, "Network--Start Thread");
		setIP(getDeviceIP());
		setMAC(getDeviceMAC());
		setBIP(getDeviceBIP());
	}
}