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
import java.net.SocketException;
import java.util.Date;

import android.util.Log;

public class Node_Listener implements Runnable {
	// private ArrayList<String> macAddressListActive = new ArrayList<String>();
	public static DatagramSocket datagramSocket = null, replySocket = null;
	private int Port;
	private String mac;
	private String data;
	private byte[] byteData;
	private DatagramPacket pkt;
	Date now = null;

	public Node_Listener(int port, String mac) {
		this.Port = port;
		this.mac = mac;
		try {
			replySocket = new DatagramSocket();
			datagramSocket = new DatagramSocket(Port);
		} catch (SocketException e) {
			Main_Service.main_Service.noti(
					"Node Listener - Socket creation exception", e.toString());
			e.printStackTrace();
		}
		Log.i("Trigger_Log", "Node_Listener--constructor end");
	}

	@Override
	public void run() {
		Log.i("Trigger_Log", "Node_Listener-run--start");
		Main_Service.main_Service.noti("in node listener", "");
		byte[] buf = new byte[256];
		String userData;
		String[] userDataArray;
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		while (Main_Service.wifi) {
			try {
				datagramSocket.receive(packet);
				now = new Date();
				userData = new String(packet.getData(), "UTF-8");
				userData = userData.substring(0, packet.getLength());

				Main_Service.main_Service.noti("recvd pkt", userData);

				userDataArray = userData.split(";");// name;MAC;type
				String m = userDataArray[1].trim();
				if (!m.equals(Network.getMAC())) {
					String replyType = new String("1".getBytes(), "UTF-8");
					if (!Main_Service.active_macs.containsKey(m)) {
						// macAddressListActive.add(m);add to map
						// processing - trigger on arrival - any user or saved
						// user
						Main_Service.main_Service.noti("in node lisntr mac",
								"'" + m + "'");
						Main_Service.main_Service.noti(
								Main_Service.conf_macs.toString(), "ssup!");
						if (Main_Service.conf_macs.contains(m)) {
							Main_Service.main_Service.noti("vj sux",
									"and sux more");
							new Thread(new Process_User(m, packet.getAddress()))
									.start();
						} else if (Main_Service.conf_macs
								.contains(Main_Service.ANY_USER)) {
							new Thread(new Process_User(Main_Service.ANY_USER,
									packet.getAddress())).start();
						}
						// ^any user
					}
					if (userDataArray[2].trim().equals(replyType)) {
						Main_Service.main_Service.noti("got a reply 2 msg", "");
						data = Main_Service.username + ";" + mac;
						byteData = data.getBytes();
						pkt = new DatagramPacket(byteData, byteData.length,
								packet.getAddress(), Device_Activity.PORT);

						replySocket.send(pkt);
					}

					synchronized (Main_Service.active_macs) {
						Main_Service.active_macs.put(m, now.getTime());
					}

				}
			} catch (IOException e) {
				if (!datagramSocket.isClosed()) {
					Log.i("Trigger_Log", "Node_Listener-run--Error in receive");
					e.printStackTrace();
				}
			}
		}
		if (!datagramSocket.isClosed()) {
			datagramSocket.close();
		}
		datagramSocket = null;
		replySocket.close();
		Log.i("Trigger_Log", "Node_Listener-run--ending");
	}
}
