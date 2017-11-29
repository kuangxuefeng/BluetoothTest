package com.kxf.bluetoothtest;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothUtils {
	public final static int MESSAGE_READ = 2000;
	public final static int MESSAGE_WRITE = 2001;
	public final static int MESSAGE_ERROR = -1000;
	private static BluetoothSocket mmSocket = null;
	public static ConnectedThread btThreadInstance = null;
	
	public static void setBluetoothSocket(BluetoothSocket socket) {
		mmSocket = socket;
		//启动蓝牙数据收发线程
		btThreadInstance = new ConnectedThread(BluetoothUtils.getBluetoothSocket(), null);
		btThreadInstance.start();
	}
	
	public static BluetoothSocket getBluetoothSocket() {
		if(mmSocket != null) {
			return mmSocket;
		}
		return null;
	}

	public static byte[] getHexBytes(String message) {
		byte[] bytes = new byte[0];
		try {
			int len = message.length() / 2;
			char[] chars = message.toCharArray();
			String[] hexStr = new String[len];
			bytes = new byte[len];
			for (int i = 0, j = 0; j < len; i += 2, j++) {
                hexStr[j] = "" + chars[i] + chars[i + 1];
                bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
            }
			Log.i("BluetoothUtils","bytes=" + bytes);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return bytes;
	}

	public static String byte2HexStr(byte[] b) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
//            sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}
}
