package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.R;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements RecordAndPlay.Resetter {
    public int physicalWidth = -1;
    public int physicalHeight = -1;
    static public RecordAndPlay record;
    SharedPreferences prefs;

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
            usbService.changeBaudRate(115200);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v("VectorDisplay", "onConfigurationChanged");
    }

    void setOrientation() {
        setRequestedOrientation(prefs.getBoolean(Options.PREF_LANDSCAPE, true) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setOrientation();
        record = new RecordAndPlay(this, this);
        setContentView(R.layout.activity_main);
        resetVectorView(record.parser.state);
        mHandler = new MyHandler(this);

        Log.v("VectorDisplay", "OnCreate");

/*        record.feed(b);



        mHandler = new MyHandler(this);

        display = (TextView) findViewById(R.id.textView1);
        editText = (EditText) findViewById(R.id.editText1);
        Button sendButton = (Button) findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    String data = editText.getText().toString();
                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        usbService.write(data.getBytes());
                    }
                }
            }
        });

        box9600 = (CheckBox) findViewById(R.id.checkBox);
        box9600.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(box9600.isChecked())
                    box38400.setChecked(false);
                else
                    box38400.setChecked(true);
            }
        });

        box38400 = (CheckBox) findViewById(R.id.checkBox2);
        box38400.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(box38400.isChecked())
                    box9600.setChecked(false);
                else
                    box9600.setChecked(true);
            }
        });

        Button baudrateButton = (Button) findViewById(R.id.buttonBaudrate);
        baudrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(box9600.isChecked())
                    usbService.changeBaudRate(9600);
                else
                    usbService.changeBaudRate(38400);
            }
        }); */
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int id = item.getItemId();
        if (id == R.id.clear) {
            Log.v("VectorView", "need to clear");
            record.feed(new Clear(record.parser.state));
        }
        else if (id == R.id.reset) {
            record.feed(new Reset(record.parser.state));
        }
        else if (id == R.id.rotate) {
            boolean landscape = ! prefs.getBoolean(Options.PREF_LANDSCAPE, true);
            prefs.edit().putBoolean(Options.PREF_LANDSCAPE, landscape).commit();
            setOrientation();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void resetVectorView(DisplayState state) {
        VectorView v = (VectorView)findViewById(R.id.vector);
/*        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams)v.getLayoutParams();
        float ratio = state.width*state.pixelAspectRatio/state.height;
        lp.dimensionRatio = ""+ratio;
        v.setLayoutParams(lp); */
        v.aspectRatio = state.width*state.pixelAspectRatio/state.height;
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    byte[] data = (byte[]) msg.obj;
                    Log.v("VectorDisplay", "a:"+new String(data));
                    record.feed(data);
//                    mActivity.get().display.append(data);
                    break;
/*                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break; */
                case UsbService.SYNC_READ:
                    byte[] buffer = (byte[]) msg.obj;
                    Log.v("VectorDisplay", "b:"+new String(buffer));
//                    byte[] b = { 'C', 'L', 0, 0, 0,0, (byte)0xFF, 0x01, (byte)0xFF, 0x01, 'T', 10, 0, 10, 0, 'A', 'B', 'C', 0, 'M', 'H', 'e', 'l', 'l', 'o', 0};
//                    record.feed(b);
                    record.feed(buffer);
//                    mActivity.get().display.append(buffer);
                    break;
            }
        }
    }
}
