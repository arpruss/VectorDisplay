package mobi.omegacentauri.vectordisplay;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
        wifiService.broadcast(ConnectionService.ACTION_DEVICE_DISCONNECTED);
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
