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
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;

import android.os.Environment;
import android.util.Log;

public class Cond_Action implements Runnable {

	private Socket socket;

	public Cond_Action(Socket socket) {
		this.socket = socket;
		Log.i(Main_Service.LOG_TAG, "Cond_Action-Constructor");
	}

	private String calculateMD5(File updateFile) {
		Log.i(Main_Service.LOG_TAG, "calculateMD5--Start");
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.i(Main_Service.LOG_TAG,
					"calculateMD5--Exception while getting Digest");
			return null;
		}

		InputStream is;
		try {
			is = new FileInputStream(updateFile);
		} catch (FileNotFoundException e) {
			Log.i(Main_Service.LOG_TAG,
					"calculateMD5--Exception while getting FileInputStream");
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
				Log.i(Main_Service.LOG_TAG,
						"calculateMD5--Exception on closing MD5 input stream");
			}
		}
	}

	private void readMess(DataInputStream in, String otheruser) {
		Log.i(Main_Service.LOG_TAG, "readMess--Start");
		String Mess = null;
		try {
			Mess = in.readUTF();
			Main_Service.main_Service.noti("Message From " + otheruser, Mess);
			Log.i(Main_Service.LOG_TAG, "Cond_Action-run--RecvMess-" + Mess);
		} catch (IOException e1) {
			Log.i(Main_Service.LOG_TAG, "readMess--error in readins message");
		}
	}

	private void recv(String otheruser, DataInputStream in, DataOutputStream out) {
		int type = 0;
		try {
			type = in.readInt();
			Log.i(Main_Service.LOG_TAG, "Cond_Action-run--type-" + type);
			if (type == 1) {
				recvFile(in, Environment.getExternalStorageDirectory()
						.getPath() + "/recvd/");
				Main_Service.main_Service.noti("File Recevied ", " From : "
						+ otheruser);

			} else if (type == 2) {
				readMess(in, otheruser);

			} else if (type == 3) {
				recvrSync(in, out, Environment.getExternalStorageDirectory()
						.getPath() + "/TriggerSync/");
				Main_Service.main_Service.noti("Sync", "Sucessful");
			}

		} catch (IOException e2) {
			Log.i(Main_Service.LOG_TAG,
					"Cond_Action-recv--Error in reading int");
		}

		try {
			in.close();
		} catch (IOException e) {
			Log.i(Main_Service.LOG_TAG, "Cond_Action-recv--in close error");
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

	private void recvrSync(DataInputStream in, DataOutputStream out,
			String folder) {
		Log.i(Main_Service.LOG_TAG, "recvrSync--Start");
		folder = folder
				+ (folder.charAt(folder.length() - 1) == '/' ? "" : "/");
		File f = new File(folder);
		File file[] = f.listFiles();
		String md5 = null;
		HashMap<String, File> hm = new HashMap<String, File>();
		HashSet<String> B = new HashSet<String>();
		int numB = file.length;
		try {
			out.writeInt(numB);
		} catch (IOException e) {
			Log.i(Main_Service.LOG_TAG,
					"recvrSync--error writing 1st int in sendersync");

		}
		for (int i = 0; i < numB; i++) {
			hm.put(md5 = calculateMD5(file[i]), file[i]);
			B.add(md5);
			try {
				out.writeUTF(md5);
			} catch (IOException e) {
				Log.i(Main_Service.LOG_TAG, "recvrSync--error in sending md5");
			}
		}
		int numNew = 0;// #new files sender is giving to recvr
		try {
			numNew = in.readInt();
		} catch (IOException e) {
			Log.i(Main_Service.LOG_TAG,
					"recvrSync--error reading 1st int in recvrsync");
		}

		for (int i = 0; i < numNew; i++) {
			recvFile(in, folder);
		}
		int numWants = 0;// #new files that sender wants from recvr
		try {
			numWants = in.readInt();
		} catch (IOException e) {
			Log.i(Main_Service.LOG_TAG,
					"recvrSync--error reading 2nd int in recvrsync");
		}

		for (int i = 0; i < numWants; i++) {
			try {
				md5 = in.readUTF();
			} catch (IOException e1) {
				Log.i(Main_Service.LOG_TAG,
						"recvrSync--error in readins md5 recvrSync");
				e1.printStackTrace();
			}
			sendFile(out, hm.get(md5).getPath());
		}
	}

	@Override
	public void run() {
		Log.i(Main_Service.LOG_TAG, "Cond_Action-run--Start Thread");
		DataInputStream in = null;
		DataOutputStream out = null;
		String otherUserName = null;
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			otherUserName = in.readUTF();
			Log.i(Main_Service.LOG_TAG, "Cond_Action-run--Username-"
					+ otherUserName);

		} catch (IOException e) {
			Log.i(Main_Service.LOG_TAG,
					"Cond_Action-run--Error in out stream creation or read othername");
		}

		recv(otherUserName, in, out);
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
}
