package mobi.omegacentauri.vectordisplay;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import mobi.omegacentauri.vectordisplay.commands.Clear;
import mobi.omegacentauri.vectordisplay.commands.Reset;

public class MainActivity extends AppCompatActivity {
    public static final String KEY_COMMAND = "cmd";
    public static final String KEY_LABEL = "label";
    public static final String KEY_ASPECT = "aspect";
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
    public static final int ADD_COMMAND = 1;
    public static final int DELETE_COMMAND = 2;
    public static final int DELETE_ALL_COMMANDS = 3;
    public static final int ACK = 4;
    public static final int RESET_VIEW = 5;
    public static final int INVALIDATE_VIEW = 6;
    public static final int TOAST = 7;
    boolean attached;
    byte[] outBuf = new byte[8];

    static public void log(String s) {
        if (DEBUG)
            Log.v("VectorDisplay", s);
    }

    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("VectorDisplay", "mCR:"+intent.getAction());
            switch (intent.getAction()) {
                case ConnectionService.ACTION_DEVICE_CONNECTED:
                    Toast.makeText(context, "Device ready", Toast.LENGTH_SHORT).show();
//                    if (prefs.getBoolean(Options.PREF_RESET_ON_CONNECT, true))
//                        record.feed(new Reset(record.parser.state));
                    attached = true;
                    break;
                case UsbService.ACTION_DEVICE_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "Device permission not granted", Toast.LENGTH_SHORT).show();
                    attached = false;
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB device connected", Toast.LENGTH_SHORT).show();
                    attached = false;
                    break;
                case ConnectionService.ACTION_DEVICE_UNSUPPORTED: // USB DISCONNECTED
                    Toast.makeText(context, "Device not supported", Toast.LENGTH_SHORT).show();
                    attached = false;
                    break;
                case ConnectionService.ACTION_DEVICE_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "Device disconnected", Toast.LENGTH_SHORT).show();
                    attached = false;
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    attached = false;
                    break;
                default:
                    return;
            }
            if (record != null) {
                record.setConnected(attached);
                record.forceUpdate();
            }
            supportInvalidateOptionsMenu();
        }
    };
    public ConnectionService connectionService;
    private TextView display;
    private EditText editText;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            synchronized (MainActivity.this) {
                connectionService = ((ConnectionService.ConnectionBinder) arg1).getService();
                Log.v("VectorDisplay", "setRecord");
                connectionService.setRecord(record);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            synchronized (MainActivity.this) {
                connectionService = null;
            }
        }
    };

    private int connectionMode() {
        return prefs.getInt(Options.PREF_CONNECTION, Options.OPT_USB);
    }

    private void chooseConnection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] array = getResources().getStringArray(R.array.modes);
        final int oldConn = connectionMode();
        builder.setTitle("Select Connection")
                .setSingleChoiceItems(array, oldConn, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == oldConn)
                            return;
                        prefs.edit().putInt(Options.PREF_CONNECTION, which).commit();
                        if (connectionService != null)
                            connectionService.close();
                        disconnectService();
                        connectService();
                        supportInvalidateOptionsMenu();
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    public void chooseBluetoothDevice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (connectionMode() != Options.OPT_BLUETOOTH)
            return;
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        final ArrayList<BluetoothDevice> devs = new ArrayList<BluetoothDevice>();
        devs.addAll(btAdapter.getBondedDevices());
        if (devs == null) {
            Toast.makeText(this, "No paired Bluetooth devices", Toast.LENGTH_LONG).show();
            return;
        }
        Collections.sort(devs, new Comparator<BluetoothDevice>(){
            @Override
            public int compare(BluetoothDevice lhs, BluetoothDevice rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(lhs.getName(), rhs.getName());
            }});
        int selected = -1;
        String lastAddress = prefs.getString(Options.PREF_LAST_BLUETOOTH_ADDRESS, "");
        CharSequence[] devLabels = new CharSequence[devs.size()];
        for (int i=0; i<devs.size(); i++) {
            BluetoothDevice d = devs.get(i);
            if (d.getAddress().equals(lastAddress))
                selected = i;
            devLabels[i] = d.getName() + " (" + d.getAddress() + ")";
        }
        builder.setTitle("Select Device")
                .setItems(devLabels, /*selected, */new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String address = devs.get(which).getAddress();
                        prefs.edit().putString(Options.PREF_LAST_BLUETOOTH_ADDRESS, address).commit();
                        Intent intent = new Intent(BluetoothService.ACTION_BLUETOOTH_DEVICE_SELECTED);
                        intent.putExtra(BluetoothService.EXTRA_DEVICE_ADDRESS, address);
                        sendBroadcast(intent);
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MainActivity.log("onConfigurationChanged");
    }

    void setOrientation() {
        setRequestedOrientation(prefs.getBoolean(Options.PREF_LANDSCAPE, false) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        stopServices();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        userCommands = new ArrayList<Byte>();
        userLabels = new ArrayList<String>();

        commandHandler = new MyHandler(this);
        record = new RecordAndPlay(this, commandHandler);

        setContentView(R.layout.activity_main);

        commandList = (ListView) findViewById(R.id.commands);
        commandListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                userLabels);
        commandList.setAdapter(commandListAdapter);
        commandList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                outBuf[0] = 'B';
                outBuf[1] = 'T';
                outBuf[2] = userCommands.get(position);
                for (int i = 3; i < 8; i++)
                    outBuf[i] = 0;
                synchronized (MainActivity.this) {
                    if (connectionService != null)
                        connectionService.write(outBuf);
                }
            }
        });

        setOrientation();
//        record = new RecordAndPlay(this, this);
        resetVectorView(this, record.parser.state.getAspectRatio());

        MainActivity.log("OnCreate");
    }

    private void stopServices() {
        stopService(new Intent(this, UsbService.class));
        stopService(new Intent(this, WifiService.class));
        stopService(new Intent(this, BluetoothService.class));
    }

    static void resetVectorView(MainActivity main, float aspectRatio) {
        MainActivity.log("reset to aspect "+aspectRatio);
        VectorView v = (VectorView) main.findViewById(R.id.vector);
        if (v != null) {
            v.aspectRatio = aspectRatio;
            v.getParent().requestLayout();
            v.forceLayout();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        record.updateTimeMillis = 1000 / (long) Integer.parseInt(prefs.getString(Options.PREF_FPS, "30"));
        connectService();
    }

    private void connectService() {
        Log.v("VectorDisplay", "connectService()");
        setFilters();  // Start listening notifications from UsbService
        switch (connectionMode()) {
            case Options.OPT_IP:
                Log.v("VectorDisplay", "starting wifi service");
                startService(WifiService.class, connection, null); // Start WifiService (if it was not started before) and Bind it
                record.setDisconnectedStatus(new String[]{"WiFi device not connected"});
                break;
            case Options.OPT_BLUETOOTH:
                Log.v("VectorDisplay", "starting bluetooth service");
                startService(BluetoothService.class, connection, null); // Start WifiService (if it was not started before) and Bind it
                record.setDisconnectedStatus(new String[]{"Start Bluetooth device and press CONNECT"});
                break;
            default:
                startService(UsbService.class, connection, null); // Start UsbService(if it was not started before) and Bind it
                record.setDisconnectedStatus(new String[]{"USB Disconnected"});
                break;
        }
        VectorView view = (VectorView)findViewById(R.id.vector);
/*        if (view != null) {
            view.invalidate();
        } */
    }

    @Override
    public void onPause() {
        super.onPause();
        disconnectService();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopServices();
    }

    void disconnectService() {
        unregisterReceiver(mConnectionReceiver);
        try {
            unbindService(connection);
        } catch(Exception e) {}
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
        filter.addAction(ConnectionService.ACTION_DEVICE_CONNECTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(ConnectionService.ACTION_DEVICE_DISCONNECTED);
        filter.addAction(ConnectionService.ACTION_DEVICE_UNSUPPORTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_DEVICE_PERMISSION_NOT_GRANTED);
        registerReceiver(mConnectionReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int conn = connectionMode();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem item = menu.findItem(R.id.mode);
        item.setTitle(getResources().getStringArray(R.array.modes)[conn]);
        item = menu.findItem(R.id.disconnect);
        item.setVisible(attached);
        item = menu.findItem(R.id.connect);
        item.setVisible(!attached && conn == Options.OPT_BLUETOOTH);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        else if (id == R.id.license) {
            message("The MIT License",
                            "VectorDisplay Copyright (c) 2018 Omega Centauri Software<br/>" +
                            "UsbSerial library Copyright (c) 2014 Felipe Herranz<br/><br/>" +
                            "Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
                            "of this software and associated documentation files (the \"Software\"), to deal\n" +
                            "in the Software without restriction, including without limitation the rights\n" +
                            "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
                            "copies of the Software, and to permit persons to whom the Software is\n" +
                            "furnished to do so, subject to the following conditions:<br/>\n" +
                            "\n" +
                            "The above copyright notice and this permission notice shall be included in all\n" +
                            "copies or substantial portions of the Software.<br/>\n" +
                            "\n" +
                            "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
                            "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
                            "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
                            "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
                            "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
                            "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
                            "SOFTWARE.", false);
        }
        else if (id == R.id.help) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instructables.com/id/TabletPhone-As-Arduino-Screen-and-a-2-Oscilloscope/"));
            startActivity(browserIntent);
        }
        else if (id == R.id.mode) {
            chooseConnection();
        }
        else if (id == R.id.disconnect) {
            if (connectionService != null)
                connectionService.disconnectDevice();
        }
        else if (id == R.id.connect && connectionMode() == Options.OPT_BLUETOOTH) {
            chooseBluetoothDevice();
        }
        return super.onOptionsItemSelected(item);
    }

    private void message(String title, String msg, final boolean exit) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle(title);
        alertDialog.setMessage(Html.fromHtml(msg));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(exit) finish();
                    } });
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {if(exit) finish();} });
        alertDialog.show();
    }

    public static void sendResetViewMessage(Handler h, DisplayState state) {
        Message msg = h.obtainMessage(RESET_VIEW);
        Bundle b = new Bundle();
        b.putFloat(KEY_ASPECT, state.getAspectRatio());
        msg.setData(b);
        h.sendMessage(msg);
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            super();
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity main = mActivity.get();

            if (main == null || main.commandListAdapter == null)
                return;

            if (msg.what == MainActivity.DELETE_ALL_COMMANDS) {
                main.userCommands.clear();
                main.commandListAdapter.clear();
                main.commandList.setVisibility(main.userCommands.size() > 0 ? View.VISIBLE : View.GONE);
            }
            else if (msg.what == MainActivity.ADD_COMMAND || msg.what == MainActivity.DELETE_COMMAND) {
                byte cmd = msg.getData().getByte(MainActivity.KEY_COMMAND);
                for (int i=0; i<main.userCommands.size(); i++) {
                    if (main.userCommands.get(i) == cmd) {
                        main.commandListAdapter.remove(main.userLabels.get(i));
                        main.userCommands.remove(i);
                    }
                }
                if (msg.what == MainActivity.ADD_COMMAND) {
                    main.userCommands.add(cmd);
                    main.commandListAdapter.add(msg.getData().getString(MainActivity.KEY_LABEL));
                }
                main.commandList.setVisibility(main.userCommands.size() > 0 ? View.VISIBLE : View.GONE);
                MainActivity.resetVectorView(main, main.record.parser.state.getAspectRatio());
            }
            else if (msg.what == MainActivity.ACK) {
                byte[] out = "Acknwld_".getBytes();
                out[7] = (byte)msg.arg1;
                synchronized(main) {
                    if (main.connectionService != null)
                        main.connectionService.write(out);
                }
            }
            else if (msg.what == MainActivity.RESET_VIEW) {
                MainActivity.log("resetting view");
                MainActivity.resetVectorView(main, msg.getData().getFloat(MainActivity.KEY_ASPECT));
            }
            else if (msg.what == MainActivity.INVALIDATE_VIEW) {
                main.record.forceUpdate();
            }
            else if (msg.what == MainActivity.TOAST) {
                String text = msg.getData().getString(MainActivity.KEY_LABEL);
                Log.v("VectorDisplay", "toast "+text);
                Toast.makeText(main, text, Toast.LENGTH_LONG).show();
            }
        }
    }
}

