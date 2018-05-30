package mobi.omegacentauri.vectordisplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.felhr.usbserial.UsbSerialInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends ConnectionService {
    public static final String EXTRA_DEVICE_ADDRESS = "deviceAddress";
    public static boolean SERVICE_CONNECTED = false;
    String btAddress = null;
    BluetoothDevice device = null;
    BluetoothSocket mSock = null;
    public static final String ACTION_BLUETOOTH_DEVICE_SELECTED="mobi.omegacentauri.vectordisplay.BLUETOOTH_DEVICE_SELECTED";

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_BLUETOOTH_DEVICE_SELECTED)) {
                btAddress = arg1.getExtras().getString(EXTRA_DEVICE_ADDRESS);
                new ConnectionThread().start();
            }
        }
    };
    /*
     *  Data received from serial port will be received here.
     */
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] data) {
            feed(data);
        }
    };
    private OutputStream out;
    private InputStream in;

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_BLUETOOTH_DEVICE_SELECTED);
        registerReceiver(usbReceiver, filter);
    }
    @Override
    synchronized public void onDestroy() {
        super.onDestroy();
        stopSocket();
        unregisterReceiver(usbReceiver);
        BluetoothService.SERVICE_CONNECTED = false;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        btAddress = null;
        device = null;
        mSock = null;
        Log.v("VectorDisplay", "on create BluetoothService");
        BluetoothService.SERVICE_CONNECTED = true;
        setFilter();
    }

    @Override
    public void disconnectDevice() {
        stopSocket();
    }

    @Override
    synchronized public void close() {
        stopSocket();
        stopSelf();
    }

    @Override
    public void setRecord(RecordAndPlay r) {
        super.setRecord(r);
    }

    public void stopSocket() {
        if (mSock != null)
            broadcast(ACTION_DEVICE_DISCONNECTED);
        if (mSock != null) {
            try {
                mSock.close();
            } catch (IOException e1) {
            }
            mSock = null;
        }
    }

    @Override
    synchronized public void write(byte[] data) {
        if (out != null) {
            try {
                out.write(data);
            } catch (IOException e) {
            }
        }
    }

    /*
     * A simple thread to open a serial port.
     * Although it should be a fast operation. moving usb operations away from UI thread is a good thing.
     */
    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            byte[] byteBuffer = new byte[256];

            BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
            device = null;
            for (BluetoothDevice d : btAdapter.getBondedDevices())
                if (d.getAddress().equals(btAddress)) {
                    device = d;
                    break;
                }

            if (device == null) {
                broadcast(ACTION_DEVICE_UNSUPPORTED);
                return;
            }

            mSock = null;
            try {
                mSock = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            } catch (IOException e) {
                broadcast(ACTION_DEVICE_UNSUPPORTED);
                return;
            }

            try {
                BluetoothSocket sock = mSock; // local copy
                sock.connect();
                out = sock.getOutputStream();
                in = sock.getInputStream();
                broadcast(ConnectionService.ACTION_DEVICE_CONNECTED);

                while(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH || sock.isConnected()) {
                    int n = in.read(byteBuffer);
                    record.feed(byteBuffer, n);
                }
            } catch (IOException e) {
                stopSocket();
            }
        }
    }
}
