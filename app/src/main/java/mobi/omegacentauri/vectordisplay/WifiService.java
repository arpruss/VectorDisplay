package mobi.omegacentauri.vectordisplay;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.felhr.usbserial.CDCSerialDevice;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Map;

public class WifiService extends ConnectionService {
    public static final int PORT = 7788;
    public static final String ACTION_USB_READY = "com.felhr.connectivityservices.USB_READY";
    public static final String ACTION_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_NOT_SUPPORTED = "com.felhr.usbservice.USB_NOT_SUPPORTED";
    public static final String ACTION_NO_USB = "com.felhr.usbservice.NO_USB";
    public static final String ACTION_DEVICE_PERMISSION_GRANTED = "com.felhr.usbservice.USB_PERMISSION_GRANTED";
    public static final String ACTION_DEVICE_PERMISSION_NOT_GRANTED = "com.felhr.usbservice.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_DEVICE_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    public static final String ACTION_CDC_DRIVER_NOT_WORKING = "com.felhr.connectivityservices.ACTION_CDC_DRIVER_NOT_WORKING";
    public static final String ACTION_USB_DEVICE_NOT_WORKING = "com.felhr.connectivityservices.ACTION_USB_DEVICE_NOT_WORKING";
    public static final int MESSAGE_FROM_SERIAL_PORT = 0;
    public static final int CTS_CHANGE = 1;
    public static final int DSR_CHANGE = 2;
    public static final int SYNC_READ = 3;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int BAUD_RATE = 115200; // 115200*4; // 115200; // BaudRate. Change this value if you need
    public static boolean SERVICE_CONNECTED = false;
    public boolean stop = false;
    public String ipAddress = "";

    private WifiManager wifiManager;

    public boolean serialPortConnected;
    /*
     *  Data received from serial port will be received here.
     */
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] data) {
            feed(data);
        }
    };
    private WifiServer server;
    private WifiInfo wifiInfo;

    @Override
    synchronized public void onDestroy() {
        super.onDestroy();
        if (server != null)
            server.close();
        stop = true;
        WifiService.SERVICE_CONNECTED = false;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("VectorDisplay", "on create WiFiService");
        serialPortConnected = false;
        WifiService.SERVICE_CONNECTED = true;
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        new WifiService.ConnectionThread().start();
    }

    /* MUST READ about services
     * http://developer.android.com/guide/components/services.html
     * http://developer.android.com/guide/components/bound-services.html
     */
    /*
     * This function will be called from MainActivity to write data through Serial Port
     */
    @Override
    synchronized public void write(byte[] data) {
        if (server != null) { // TODO: fix synchronization
            server.write(data);
        }
    }

    /*
     * This function will be called from MainActivity to change baud rate
     */

    @Override
    synchronized public void close() {
        if (server != null)
            server.close();
        stop = true;
        stopSelf();
    }

    @Override
    public void setRecord(RecordAndPlay r) {
        super.setRecord(r);
        if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
            int ip = wifiInfo.getIpAddress();
            String ipString = "" + (0xFF & (ip >> 24)) + "." + (0xFF & (ip >> 16)) + "." + (0xFF & (ip >> 8)) + "." + (0xFF & ip);
            if (record != null) {
                record.setDisconnectedStatus(new String[]{"WiFi device not connected", "Connect to " + ipString});
                record.forceUpdate();
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
            boolean done = false;

            do {
                server = null;

                Log.v("VectorDisplay", "net id "+wifiInfo.getNetworkId());
                if (wifiInfo.getNetworkId() != -1) {
                    int ip = wifiInfo.getIpAddress();
                    String ipString = ""+(0xFF&(ip>>24))+"."+(0xFF&(ip>>16))+"."+(0xFF&(ip>>8))+"."+(0xFF&ip);
                    if (record != null) {
                        record.setDisconnectedStatus(new String[]{"WiFi device not connected", "Connect to " + ipString});
                        record.forceUpdate();
                    }

                    try {
                        synchronized (WifiService.this) {
                            done = true;
                            server = new WifiServer(WifiService.this, PORT);
                        }
                        server.start();
                    } catch (Exception e) {
                        if (server != null) {
                            server.stop();
                            server = null;
                        }
                    }

                    if (!done) {
                        synchronized (WifiService.this) {
                            if (WifiService.this.stop)
                                break;
                        }
                    }
                }
                else {
                    record.setDisconnectedStatus(new String[]{"WiFi not connected"});
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } while(!done);

            Intent intent = new Intent(ACTION_DEVICE_DISCONNECTED); // TODO: more informative
            context.sendBroadcast(intent);
        }
    }
}

