package com.example.nfc_enterance_android;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {
	private Button write;
	private Button read;
	public enum mode {READ, WRITE}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		write = (Button)findViewById(R.id.button_write);
		read = (Button)findViewById(R.id.button_read);
		
		write.setOnClickListener(butListener);
		read.setOnClickListener(butListener);
		
	}
	private OnClickListener butListener = new OnClickListener(){
		public void onClick(View v) {
			mode tagMode = null;
			if(v.equals(write)){
				tagMode = mode.WRITE;
			} else if(v.equals(read)){
				tagMode = mode.READ;
			} else {
				Log.d("ERR", "Unexpected Component");
				return;
			}
			String input = ((EditText)findViewById(R.id.input_field)).getText().toString();
			Intent detectActivity = new Intent(MainActivity.this, DetectActivity.class);
			detectActivity.putExtra("input", input);
			detectActivity.putExtra("mode", tagMode);
			startActivity(detectActivity);
		}
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
