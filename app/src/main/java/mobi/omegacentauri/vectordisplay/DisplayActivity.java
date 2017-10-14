
package mobi.omegacentauri.vectordisplay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.text.DateFormat.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class DisplayActivity extends Activity {
	public int physicalWidth = -1;
	public int physicalHeight = -1;
	public boolean works = false;
    VectorView view;
    private RecordAndPlay record;

    byte[] b = { 'C', 'L', 0, 0, 0,0, (byte)0xFF, 0x01, (byte)0xFF, 0x01, 'T', 10, 0, 10, 0, 'A', 'B', 'C', 0};

    private void message(String title, String msg, final boolean exit) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
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

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
        Log.v("VectorDisplay", "onConfigurationChanged");
        view = new VectorView(this, record);
        view.redraw = true;
        setContentView(view);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		setContentView(R.layout.main);

        record = new RecordAndPlay();
        view = new VectorView(this, record);
        setContentView(view);

		Log.v("VectorDisplay", "OnCreate");

        record.feed(b);
	}

	@Override
	public void onResume() {
		super.onResume();
        Log.v("VectorDisplay", "onResume");
	}
}
