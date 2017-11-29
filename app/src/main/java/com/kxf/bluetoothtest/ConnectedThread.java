package com.kxf.bluetoothtest;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kxf on 2017/11/08.
 */

public class ConnectedThread extends Thread {
    private static final String TAG = "ConnectedThread";
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private Handler mHandler;

    public void setMHandler(Handler handler) {
        mHandler = handler;
    }

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        mHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.i(TAG,"IOException", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {

        int len; // len returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (true) {
            byte[] buffer = new byte[1024];
            try {
                Log.i(TAG,"准备接收数据！");
                // Read from the InputStream
                len = mmInStream.read(buffer);
                // Send the obtained len to the UI activity
                byte[] bufferR = new byte[len];
                System.arraycopy(buffer, 0, bufferR, 0, len);
                Log.i(TAG,"接收到数据[" + len + "]:" + new String(bufferR));
                if (null != mHandler) {
                    mHandler.obtainMessage(BluetoothUtils.MESSAGE_READ, len, -1, bufferR)
                            .sendToTarget();
                }
            } catch (IOException e) {
                Log.i(TAG,"IOException", e);
                try {
                    mmSocket.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                mHandler.obtainMessage(BluetoothUtils.MESSAGE_ERROR)
                        .sendToTarget();
                BluetoothUtils.btThreadInstance = null;
                break;
            }
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(final byte[] bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mmOutStream.write(bytes);
                    mmOutStream.flush();
                    Log.i(TAG,"发送成功，发送字节：" + bytes.length);
                } catch (IOException e) {
                    mHandler.obtainMessage(BluetoothUtils.MESSAGE_ERROR)
                            .sendToTarget();
                    try {
                        mmSocket.close();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    BluetoothUtils.btThreadInstance = null;
                    Log.i(TAG,"IOException", e);
                }
            }
        }).start();
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }

}
