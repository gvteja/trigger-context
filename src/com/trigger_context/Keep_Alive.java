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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.Iterator;

import android.util.Log;

public class Keep_Alive implements Runnable {

	private String MAC;
	static DatagramSocket socket = null;
	private int Port;
	private InetAddress BIP;
	private String data = null;
	private byte[] byteData = null;
	private DatagramPacket pkt = null;

	public Keep_Alive(String MAC, int Port, InetAddress bcastIp) {
		this.MAC = MAC;
		this.Port = Port;
		this.BIP = bcastIp;
		try {
			socket = new DatagramSocket();
			socket.setBroadcast(true);
		} catch (SocketException e) {
			Log.i(Main_Service.LOG_TAG,
					"Keep_Alive--Error Unable to bind to port");
		}
		Log.i(Main_Service.LOG_TAG, "Keep_Alive--Constructor");
	}

	@Override
	public void run() {
		Log.i(Main_Service.LOG_TAG, "Keep_Alive-run--Started");
		Iterator<String> iter = null;
		Date now = null;
		while (Main_Service.wifi) {
			data = Main_Service.username + ";" + MAC + ";2";
			byteData = data.getBytes();
			pkt = new DatagramPacket(byteData, byteData.length, BIP, Port);
			try {
				socket.send(pkt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// removing expired entries
			now = new Date();
			iter = Main_Service.active_macs.keySet().iterator();
			synchronized (Main_Service.active_macs) {
				while (iter.hasNext()) {
					if (now.getTime()
							- Main_Service.active_macs.get(iter.next()) > Main_Service.timeout) {
						iter.remove();
					}
				}
			}

			try {
				Thread.sleep(Main_Service.timeout);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		socket.close();

		Log.i(Main_Service.LOG_TAG, "Keep_Alive-ending");
	}

}
