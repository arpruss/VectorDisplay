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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class WifiService extends ConnectionService {
    public static final int PORT = 7788;
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
    public void disconnectDevice() {
        if (server != null && server.client != null) {
            try {
                server.client.close();
            } catch (IOException e) {
            }
        }
    }

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
    public class WifiServer extends Thread {
        private ServerSocket listener;
        WifiService wifiService;
        Socket client = null;
        InputStream in = null;
        OutputStream out = null;
        char[] buffer = new char[256];
        byte[] byteBuffer = new byte[256];
        Boolean stop;

        public WifiServer(WifiService service, int port) throws IOException {
            listener = new ServerSocket(port);
            wifiService = service;
        }

        @Override
        public void run() {
            stop = false;
            while(! stop) {
                synchronized(wifiService) {
                    if (wifiService.stop)
                        break;
                }
                try {
                    client = listener.accept();
                    Log.v("VectorDisplay", "Accepted");
                    in = client.getInputStream();
                    synchronized(WifiServer.this) {
                        out = client.getOutputStream();
                    }
                    wifiService.broadcast(ConnectionService.ACTION_DEVICE_CONNECTED);
                    while(client.isConnected()) {
                        int n = in.read(byteBuffer);
                        wifiService.record.feed(byteBuffer, n);
                    }
                } catch (IOException e) {
                    Log.v("VectorDisplay", "disconnected client");
                }
                stopClient();
            }
        }

        public void close() {
            stopClient();
            stop = true;
            if (listener != null) {
                try {
                    listener.close();
                } catch (IOException e) {
                    listener = null;
                }
            }
        }

        public void stopClient() {
            Log.v("VectorDisplay", "adc");
            wifiService.broadcast(ACTION_DEVICE_DISCONNECTED);
            synchronized(WifiServer.this) {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e1) {
                    }
                    out = null;
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                }
                in = null;
            }
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e1) {
                }
                client = null;
            }
        }

        synchronized public void write(byte[] data) {
            if (out != null) {
                try {
                    out.write(data);
                } catch (IOException e) {
                }
            }
        }
    }
}
