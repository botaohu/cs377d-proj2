package com.appspot.drivecalm;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private boolean mIsBound = false;
	private MainService mBoundService;

	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        mBoundService = ((MainService.MainBinder)service).getService();
	        if (!mBoundService.getPresent()) {
				b.setEnabled(false);
				b.setText("Voice recognizer not present");
				Toast.makeText(MainActivity.this, "Voice recognizer not present",
						Toast.LENGTH_SHORT).show();
			}
	        mBoundService.init();
			
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        mBoundService = null;	        
	    }
	};
	private Button b;

	void doBindService() {
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent(this, MainService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    doUnbindService();
	}
	
	void doUnBindService() {
		this.unbindService(mConnection);
		mIsBound = false;
	}
	
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		doBindService();
		b = (Button) findViewById(R.id.button1);
		
		
		b.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				mBoundService.toggle();
			}

			
			
		});
		
	}
	

	

	
}
