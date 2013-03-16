package com.example.xplayer;

import java.io.IOException;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

public class MainService extends Service {
	private MediaPlayer[] mp;
	private boolean playing = false;
	private MediaPlayer current;
	final int n = 5;

	public class MainBinder extends Binder {
		MainService getService() {
			return MainService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new MainBinder();

	private void createMP() {
		if (mp == null) {
			try {
				mp = new MediaPlayer[n];
				mp[0] = MediaPlayer.create(this, R.raw.babynoise);
				mp[1] = MediaPlayer.create(this, R.raw.gangnam);
				mp[2] = MediaPlayer.create(this, R.raw.gorilla);
				mp[3] = MediaPlayer.create(this, R.raw.hakunamatata);
				mp[4] = MediaPlayer.create(this, R.raw.moveit);
				for (int i = 0; i < n; i++) {
					mp[i].prepare();
					mp[i].setOnCompletionListener(new OnCompletionListener() {
						public void onCompletion(MediaPlayer arg0) {
							playing = false;
						}
					});
				}
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		try {
			createMP();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		toggle();
		return START_STICKY;
	}

	public void toggle() {
		if (playing) {
			current.pause();
			playing = false;
		}
		playSong();

	}

	private void playSong()  {
		if (mp == null)
			createMP();
		int rand = (int) (Math.random() * n);
		Log.v("MUSIC", "" + rand);
		current = mp[rand];
		current.seekTo(0);
		current.start();
		playing = true;
	}

}
