package com.ov.smsmail;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor; 

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainMenuActivity extends Activity {

	private TextView msgView;
	private SMSReceiver receiver;
	private IntentFilter filter;

	private EditText fromText;
	private EditText passwordText;
	private EditText toText;

	private Button startBtn;
	private Button stopBtn;

	private String s_from;
	private String s_password;
	private String s_to;

	static final int MSG_SUCCESS = 0x1001;
	static final int MSG_FAILURE = 0x1002;

	private int running = 0;
	private int checkmail_askexit = 0;

	private Thread checkmail_t;

	private Handler mh = new Handler(){
		public void handleMessage (Message msg) {
			switch(msg.what){
			case MSG_SUCCESS:
				msgView.append("SendMail ok\n");
				break;
			case MSG_FAILURE:
				msgView.append("SendMail fail\n");
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("smsmail", "onCreate");
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);		

		msgView = (TextView)findViewById(R.id.msg);
		fromText= (EditText)findViewById(R.id.textfrom);
		passwordText= (EditText)findViewById(R.id.textpassword);
		toText= (EditText)findViewById(R.id.textto);

		//load preference
		SharedPreferences preferences=getSharedPreferences("config", MODE_PRIVATE);
		String cfg_from=preferences.getString("from", "");
		String cfg_password=preferences.getString("password", "");
		String cfg_to=preferences.getString("to", "");
		fromText.setText(cfg_from);
		passwordText.setText(cfg_password);
		toText.setText(cfg_to);

		receiver = new SMSReceiver();
		filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");

		startBtn = (Button)findViewById(R.id.btn_start);
		startBtn.setOnClickListener(new OnClickListener() {  
			public void onClick(View v) {  
				s_from = fromText.getText().toString();
				s_password = passwordText.getText().toString();
				s_to = toText.getText().toString();

				//save preference
				SharedPreferences preferences=getSharedPreferences("config", MODE_PRIVATE);
				Editor editor=preferences.edit();
				editor.putString("from", s_from);
				editor.putString("password", s_password);
				editor.putString("to", s_to);
				editor.commit();

				start();
			}  });  

		stopBtn = (Button)findViewById(R.id.btn_stop);
		stopBtn.setOnClickListener(new OnClickListener() {  
			public void onClick(View v) {  
				stop();
			} });
		stopBtn.setEnabled(false);

		msgView.setText("inited\n");
	}

	public class checkmail_run implements Runnable {
		public void run(){
			CheckMail m = new CheckMail();
			m.Set(s_from, s_password);
			while(checkmail_askexit == 0){
				m.check();
				if(checkmail_askexit != 0){
					break;
				}
				try{
					Thread.sleep(1000 * 15); //sleep 15 seconds
				}catch(Exception e){
				}
			}
			checkmail_askexit = 2;
			return;
		}
	}

	private void start()
	{
		Log.d("smsmail", "start");
		if(running > 0){
			stop();
		}
		checkmail_askexit = 0;
		registerReceiver(receiver, filter);
		checkmail_t = new Thread(new checkmail_run() );
		checkmail_t.start();
		msgView.append("start\n");
		running = 1;

		stopBtn.setEnabled(true);
		startBtn.setEnabled(false);
	}

	private void stop(){
		if(running > 0){
			unregisterReceiver(receiver);
			checkmail_askexit = 1;
			while(checkmail_askexit != 2);
		}
		running = 0;

		stopBtn.setEnabled(false);
		startBtn.setEnabled(true);
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	@Override
	public void onStop() {
		Log.d("smsmail", "onStop");
		stop();
		Log.d("smsmail", "exit");
		finish();
		super.onStop();
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}
	
private class SMSReceiver extends BroadcastReceiver
{

    //android.provider.Telephony.Sms.Intents
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

	public class sendmail_run implements Runnable {
		private SmsMessage msg;
		public sendmail_run(SmsMessage msg) {
			this.msg = msg;
		}

		public void run(){
			SendMail m = new SendMail();
			m.Set(s_from, s_password, s_to);
			boolean ret = m.sms_send_mail(msg);
			if(ret){
				mh.obtainMessage(MSG_SUCCESS).sendToTarget();
			}else{
				mh.obtainMessage(MSG_FAILURE).sendToTarget();
			}
		}
	}

    @Override
    public void onReceive(Context context, Intent intent)
    {
       if (intent.getAction().equals(SMS_RECEIVED_ACTION))
       {
           SmsMessage[] messages = getMessagesFromIntent(intent);
           for (SmsMessage message : messages)
           {
				msgView.append("SMS:" + message.getDisplayOriginatingAddress() + "\n" );
				Thread t = new Thread(new sendmail_run(message) );
				t.start();
           }
       }
    }

    public final SmsMessage[] getMessagesFromIntent(Intent intent)
    {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];
        for (int i = 0; i < messages.length; i++)
        {
            pduObjs[i] = (byte[]) messages[i];
        }

        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;

        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++)
        {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }

        return msgs;
    }
}
}
