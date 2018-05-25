package mobi.omegacentauri.vectordisplay;

import mobi.omegacentauri.vectordisplay.R;
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
    byte[] outBuf = new byte[8];

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
//                    if (prefs.getBoolean(Options.PREF_RESET_ON_CONNECT, true))
//                        record.feed(new Reset(record.parser.state));
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
        Log.v("VectorDisplay", "onCreate");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        userCommands = new ArrayList<Byte>();
        userLabels = new ArrayList<String>();

        commandHandler = new MyHandler(this);
        record = new RecordAndPlay(this, commandHandler);

        setContentView(R.layout.activity_main);

        commandList = (ListView)findViewById(R.id.commands);
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
                        for (int i=3; i<8; i++)
                            outBuf[i] = 0;
                        synchronized(MainActivity.this) {
                           if (usbService != null)
                               usbService.write(outBuf);
                        }
               }
        });

                setOrientation();
//        record = new RecordAndPlay(this, this);
        resetVectorView(this, record.parser.state.getAspectRatio());

        MainActivity.log( "OnCreate");

    }

    static void resetVectorView(MainActivity main, float aspectRatio) {
        VectorView v = (VectorView)main.findViewById(R.id.vector);
        if (v != null) {
            v.aspectRatio = aspectRatio;
            v.getParent().requestLayout();
            v.forceLayout();
        }
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

    static void sendResetViewMessage(Handler h, DisplayState state) {
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
            }
            else if (msg.what == MainActivity.ACK) {
                byte[] out = "Acknwldg".getBytes();
                synchronized(main) {
                    if (main.usbService != null)
                        main.usbService.write(out);
                }
            }
            else if (msg.what == MainActivity.RESET_VIEW) {
                MainActivity.resetVectorView(main, msg.getData().getFloat(MainActivity.KEY_ASPECT));
            }
            else if (msg.what == MainActivity.INVALIDATE_VIEW) {
                VectorView view = (VectorView)main.findViewById(R.id.vector);
                if (view != null)
                    view.invalidate();
            }
            else if (msg.what == MainActivity.TOAST) {
                String text = msg.getData().getString(MainActivity.KEY_LABEL);
                Log.v("VectorDisplay", "toast "+text);
                Toast.makeText(main, text, Toast.LENGTH_LONG).show();
            }
        }
    }
}

