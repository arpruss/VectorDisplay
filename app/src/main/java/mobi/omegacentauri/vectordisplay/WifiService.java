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
        WifiService.SERVICE_CONNECTED = true;
        new WifiService.ConnectionThread().start();
    }

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
        if (server != null && server.mClient != null) {
            try {
                server.mClient.close();
                server.mClient = null;
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
            if (record != null) {
                record.setDisconnectedStatus(new String[]{"WiFi device not connected", "Connect to " + ipString(ip)});
                record.forceUpdate();
            }
        }
    }

    private static String ipString(int ip) {
        return "" + (0xFF & ip) + "." + (0xFF & (ip >> 8)) + "." + (0xFF & (ip >> 16)) + "." + (0xFF & (ip >> 24));
    }

    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            boolean done = false;

            do {
                server = null;

                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                int net = -1;
                int ip = 0;
                if (wifiManager != null) {
                    wifiInfo = wifiManager.getConnectionInfo();
                    net = wifiInfo.getNetworkId();
                    if (net != -1)
                        ip = wifiInfo.getIpAddress();
                }
                else
                    wifiInfo = null;

                if (ip != 0) {
                    if (record != null) {
                        record.setDisconnectedStatus(new String[]{"WiFi device not connected", "Connect to " + ipString(ip)});
                        MainActivity.log("ip "+ipString(ip));
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
                    if (record != null)
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
        Socket mClient = null;
        InputStream in = null;
        OutputStream out = null;
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
                    WifiServer.this.mClient = listener.accept();
                    Socket client = WifiServer.this.mClient; // local copy
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
            if (mClient != null) {
                try {
                    mClient.close();
                } catch (IOException e1) {
                }
                mClient = null;
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
