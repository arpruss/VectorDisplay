package mobi.omegacentauri.vectordisplay;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

abstract class ConnectionService extends Service {
    public static final String ACTION_DEVICE_CONNECTED = "mobi.omegacentauri.vectordisplay.CONNECTED";
    public static final String ACTION_DEVICE_DISCONNECTED = "mobi.omegacentauri.vectordisplay.DISCONNECTED";
    protected IBinder binder = new ConnectionBinder();
    protected RecordAndPlay record;
    protected Context context;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    protected void feed(byte[] data) {
        if (record != null)
            record.feed(data);
    }

    public class ConnectionBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    public void broadcast(String msg) {
        Intent intent = new Intent(msg);
        Log.v("VectorDisplay", "sending "+intent.getAction());
        sendBroadcast(intent);
    }

    public void setRecord(RecordAndPlay r) {
        this.record = r;
    }

    abstract public void write(byte[] data);
    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */

    public void disconnectDevice() {
        broadcast(ACTION_DEVICE_DISCONNECTED);
    }

    abstract public void close();

    @Override
    public void onCreate() {
        this.context = this;
        MainActivity.log("service created "+this.getClass());
    }
}
