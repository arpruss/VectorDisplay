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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements RecordAndPlay.Resetter {
    public static final String KEY_COMMAND = "cmd";
    public static final String KEY_LABEL = "label";
    public int physicalWidth = -1;
    public int physicalHeight = -1;
    public RecordAndPlay record;
    SharedPreferences prefs;
    List<Byte> userCommands;
    List<String> userLabels;
    ArrayAdapter<String> commandListAdapter = null;
    static final boolean DEBUG = false;
    ListView commandList;
    MyHandler commandHandler;
    static final int ADD_COMMAND = 1;
    static final int DELETE_COMMAND = 2;

    static public void log(String s) {
        if (DEBUG)
            Log.v("VectorDisplay", s);
    }

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    if (prefs.getBoolean(Options.PREF_RESET_ON_CONNECT, true))
                        record.feed(new Reset(record.parser.state));
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
    public UsbService usbService;
    private TextView display;
    private EditText editText;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            synchronized(MainActivity.this) {
                usbService = ((UsbService.UsbBinder) arg1).getService();
                usbService.setRecord(record);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            synchronized(MainActivity.this) {
                usbService = null;
            }
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MainActivity.log( "onConfigurationChanged");
    }

    void setOrientation() {
        setRequestedOrientation(prefs.getBoolean(Options.PREF_LANDSCAPE, false) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        userCommands = new ArrayList<Byte>();
        userLabels = new ArrayList<String>();

        commandHandler = new MyHandler(this);
        record = new RecordAndPlay(this, this, commandHandler);

        setContentView(R.layout.activity_main);

        commandList = (ListView)findViewById(R.id.commands);
        commandListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                userLabels);
        commandList.setAdapter(commandListAdapter);

        setOrientation();
//        record = new RecordAndPlay(this, this);
        resetVectorView(record.parser.state);

        MainActivity.log( "OnCreate");

    }

    @Override
    public void onResume() {
        super.onResume();
        record.updateTimeMillis = (long)Integer.parseInt(prefs.getString(Options.PREF_UPDATE_SPEED, "60"));
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
            record.feed(new Clear(record.parser.state));
        }
        else if (id == R.id.reset) {
            record.feed(new Reset(record.parser.state));
        }
        else if (id == R.id.rotate) {
            boolean landscape = ! prefs.getBoolean(Options.PREF_LANDSCAPE, false);
            prefs.edit().putBoolean(Options.PREF_LANDSCAPE, landscape).commit();
            setOrientation();
        }
        else if (id == R.id.settings) {
            startActivity(new Intent(this, Options.class));
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

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity main = mActivity.get();

            if (main == null || main.commandListAdapter == null)
                return;

            if (msg.what == MainActivity.ADD_COMMAND || msg.what == MainActivity.DELETE_COMMAND) {
                byte cmd = msg.getData().getByte(MainActivity.KEY_COMMAND);
                for (int i=0; i<main.userCommands.size(); i++) {
                    if (main.userCommands.get(i) == cmd) {
                        main.commandListAdapter.remove(main.userLabels.get(i));
                        main.userCommands.remove(i);
                    }
                }
                if (msg.what == MainActivity.ADD_COMMAND) {
                    main.userCommands.add(cmd);
                    main.commandListAdapter.add((String) msg.getData().getString(MainActivity.KEY_LABEL));
                }
                main.commandList.setVisibility(main.userCommands.size() > 0 ? View.VISIBLE : View.GONE);
            }
        }
    }
}

