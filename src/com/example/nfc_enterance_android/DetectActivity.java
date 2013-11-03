package com.example.nfc_enterance_android;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;

public class DetectActivity extends Activity {
	String input;
	MainActivity.mode mode;
	NfcAdapter mnfc;
	PendingIntent pending;
	IntentFilter[] IntentFilterArray;
	String[][] techList;
	private int targetSecter = 2;
	private AlertDialog.Builder builder;

	/** Called when the activity is first created. */
	@SuppressWarnings("static-access")
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.detect_activity);
	    Intent intent = getIntent();
	    
	    input = intent.getStringExtra("input");
	    mode = (MainActivity.mode)intent.getSerializableExtra("mode");
	    if(mode==null){
	    	mode = mode.READ;
	    	detectProcess(intent);
	    }
	    mnfc = NfcAdapter.getDefaultAdapter(this);
		pending = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter mifare = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
		try {
			mifare.addDataType("*/*");
			IntentFilterArray = new IntentFilter[]{mifare};
		} catch (MalformedMimeTypeException e) {
			Log.d("ERR", "mifare Type Error", e);
		}
		techList = new String[][]{new String[]{MifareClassic.class.getName()}};
	}
	public void onResume(){
		super.onResume();
		mnfc.enableForegroundDispatch(this, pending, IntentFilterArray, techList);
	}
	public void onPause(){
		super.onPause();
		mnfc.disableForegroundDispatch(this);
	}
	//Print Message and destroy activity
		private void exitMessage(String Message){
			builder = new AlertDialog.Builder(this);
			builder.setMessage(Message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					DetectActivity.this.finish();					
				}
			});
			builder.create().show();
		}
		//Encode Message to 16 byte -> Mifare classic 1 block
		private byte[] encodeMessage(MifareClassic classic, String input){
			byte[] encode = input.getBytes(Charset.forName("utf-8"));
			int BlockSize = 16;
			byte[] inputstream = new byte[BlockSize];
			if(encode.length>BlockSize)
				exitMessage("문자열의 길이가 너무 깁니다.");
			for(int i=0;i<BlockSize;i++){
				if(i<encode.length){
					inputstream[i]=encode[i];
				} else {
					inputstream[i]=0;
				}
			}
			return inputstream;
		}
		//On Detect tag in foreground
		public void onNewIntent(Intent intent){
			detectProcess(intent);
		}
		@SuppressWarnings("static-access")
		private void detectProcess(Intent intent){
			//Get Mifare Classic tag from intent
			Tag tagFromIntent = intent.getParcelableExtra(mnfc.EXTRA_TAG);
			MifareClassic classic = MifareClassic.get(tagFromIntent);
			try{
				classic.connect();
				//Decrypt target Sector
				boolean suc1 = classic.authenticateSectorWithKeyA(targetSecter, classic.KEY_DEFAULT);
				boolean suc2 = classic.authenticateSectorWithKeyB(targetSecter, classic.KEY_DEFAULT);
				if(mode==mode.READ){
					String result = "실패";
					if(suc1&&suc2){
						byte[] output = classic.readBlock(classic.sectorToBlock(targetSecter));
						result = new String(output, Charset.forName("utf-8"));
					} else {
						Log.d("ERR", "Authenticate Failure");
						exitMessage("인증에 실패하였습니다");
					}
					classic.close();
					exitMessage(result);
				} else if(mode==mode.WRITE){
					if(suc1&&suc2){
						byte[] message = encodeMessage(classic, input);
						classic.writeBlock(classic.sectorToBlock(targetSecter), message);
						exitMessage("작성에 성공하였습니다.");
					}
				}
				classic.close();
			} catch(IOException e){
				Log.d("ERR", "NFC I/O Failure", e);
				exitMessage("실패하였습니다");
			}
		}
}
