package com.kxf.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 1827;
    private static final String TAG = "MainActivity";
    private Button btn_send, btn_lianjie;
    private TextView tv_re;
    private ScrollView sv;
    private EditText et_send;
    private CheckBox cb_16_re, cb_16_send;
    private BluetoothAdapter mBluetoothAdapter;
    private Activity mActivity;
    private StringBuffer sb = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity = this;

        init();
        // 获取蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "该设备不支持蓝牙", Toast.LENGTH_SHORT).show();
        }

        //请求开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else {
            if (null == BluetoothUtils.getBluetoothSocket()){
                //进入蓝牙设备连接界面
                Intent intent = new Intent();
                intent.setClass(mActivity, DevicesListActivity.class);
                startActivity(intent);
            }
        }
    }

    private void init() {
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
        btn_lianjie = (Button) findViewById(R.id.btn_lianjie);
        btn_lianjie.setOnClickListener(this);
        tv_re = (TextView) findViewById(R.id.tv_re);
        et_send = (EditText) findViewById(R.id.et_send);
        sv = (ScrollView) findViewById(R.id.sv);
        cb_16_re = (CheckBox) findViewById(R.id.cb_16_re);
        cb_16_send = (CheckBox) findViewById(R.id.cb_16_send);
    }

    @Override
    protected void onResume() {
        super.onResume();
//		checkBTCon();
        //回到主界面后检查是否已成功连接蓝牙设备
        if (BluetoothUtils.btThreadInstance != null) {
            BluetoothUtils.btThreadInstance.setMHandler(handler);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BluetoothUtils.btThreadInstance != null) {
            BluetoothUtils.btThreadInstance.setMHandler(null);
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BluetoothUtils.MESSAGE_READ:
                    byte[] bys = (byte[]) msg.obj;
                    String re = "";
                    if (cb_16_re.isChecked()){
                        re = BluetoothUtils.byte2HexStr(bys);
                    }else {
                        re = new String(bys);
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    sb.append("[" + sdf.format(new Date()) + "]  " + re + "\n\r");
                    tv_re.setText(sb.toString());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            sv.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                    break;
                case BluetoothUtils.MESSAGE_WRITE:
                    String send = (String) msg.obj;
                    if (null != BluetoothUtils.btThreadInstance){
                        if (cb_16_send.isChecked()){
                            BluetoothUtils.btThreadInstance.write(BluetoothUtils.getHexBytes(send));
                        }else {
                            BluetoothUtils.btThreadInstance.write(send.getBytes());
                        }
                    }
                    break;
                case BluetoothUtils.MESSAGE_ERROR:
                    //进入蓝牙设备连接界面
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, DevicesListActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

    private void sendByBT(String str) {
        Log.i(TAG,"str=" + str);
        if (TextUtils.isEmpty(str)) {
            Log.e(TAG, "发送数据为空！");
            return;
        }
        Message message = handler.obtainMessage(BluetoothUtils.MESSAGE_WRITE);
        message.obj = str;
        message.sendToTarget();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_send:
                sendByBT(et_send.getText().toString().trim());
                break;
            case R.id.btn_lianjie:
                //进入蓝牙设备连接界面
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), DevicesListActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("MainActivity", "requestCode=" + requestCode);
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (null == BluetoothUtils.getBluetoothSocket()){
                    //进入蓝牙设备连接界面
                    Intent intent = new Intent();
                    intent.setClass(mActivity, DevicesListActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }
}
