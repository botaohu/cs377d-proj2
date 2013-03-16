package com.appspot.drivecalm;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;

public class MainService extends Service implements OnInitListener {
	private MediaPlayer mp;
	private boolean recorded = false;
	private boolean present = true;
	private String recorded_str = "";
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
	private SpeechRecognizer sr;
	public String text;
	private TextToSpeech tts;
	private boolean wait_c;
	public void checkVoiceRecognition() {
		  // Check if voice recognition is present
		sr = SpeechRecognizer.createSpeechRecognizer(this);
		sr.setRecognitionListener(new listener());  
		 if (sr == null) {
		   present = false;
		 }
	 }
	

	   class listener implements RecognitionListener          
	   {
	            private static final String TAG = "SR";
				public void onReadyForSpeech(Bundle params)
	            {
	                     Log.d(TAG, "onReadyForSpeech");
	             		
	            }
	            public void onBeginningOfSpeech()
	            {
	                     Log.d(TAG, "onBeginningOfSpeech");
	            }
	            public void onRmsChanged(float rmsdB)
	            {
	            }
	            public void onBufferReceived(byte[] buffer)
	            {
	                     Log.d(TAG, "onBufferReceived");
	            }
	            public void onEndOfSpeech()
	            {
	                     Log.d(TAG, "onEndofSpeech");
	                     
	            }
	            public void onError(int error)
	            {
	                     Log.d(TAG,  "error " +  error);
	                     text = "error " + error;
	            }
	            public void onResults(Bundle results)                   
	            {
	                     String str = new String();
	                     Log.d(TAG, "onResults " + results);
	                     ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
	                     for (int i = 0; i < data.size(); i++)
	                     {
	                              
	                               str += data.get(i);
	                     }
	                     Log.d(TAG, "result:" + str);
	                     if (wait_c) {
	                    	 if (str.contains("confirm") || str.contains("send")) {
	                    		 new SendTask().execute(recorded_str);
	                    		 
	                    	 } else {
	                    		 speakOut("Message Not Sent");
	                    	 }
	                    	 sr.stopListening();
	                    	 wait_c = false;
	                     } else {
	                    	 speakOut(data.get(0).toString());
	                    	 recorded_str = data.get(0).toString();
	                    	 wait_c = true;
	                    	 MainService.this.listen(false);
	                     } 
	            }
	            public void onPartialResults(Bundle results)
	            {
	                     
	            }
	            public void onEvent(int eventType, Bundle params)
	            {
	                     Log.d(TAG, "onEvent " + eventType);
	            }
	   }
	   
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    super.onStartCommand(intent, flags, startId);
	    init();
	    toggle();
	    return Service.START_STICKY;
	}

	public void init() {
		if (sr == null)
			checkVoiceRecognition();
	    if (tts == null)
	    	tts = new TextToSpeech(this, this);
	}
	
	public void toggle() {
		
			this.listen(false);
			wait_c = false;
		
	}
	private void send(String str) {
		
	}
		
	class SendTask extends AsyncTask<String, Void, Void> {

	    private String response = null;

	    protected Void doInBackground(String... str) {
			try {        
			        HttpClient client = new DefaultHttpClient();
			        HttpPost request = new HttpPost();
			        byte[] bytesOfMessage = str[0].getBytes("UTF-8");
			        MessageDigest md = MessageDigest.getInstance("MD5");
			        byte[] thedigest = md.digest(bytesOfMessage);
			        String md5 = toHex(thedigest);
			        long time = System.currentTimeMillis() / 1000;
			        String signature = signed("POST\n/apps/39390/channels/test_channel/events\nauth_key=b243166ec0263612dcde&auth_timestamp=" + String.valueOf(time) + "&auth_version=1.0&body_md5=" +md5 +"&name=my_event");
			        String url = "http://api.pusherapp.com/apps/39390/channels/test_channel/events?" +
			        		"name=my_event&" +
			        		"body_md5=" + md5 +
			        		"&auth_version=1.0&"+
			        		"auth_key=b243166ec0263612dcde&"+
			        		"auth_timestamp="+ time + 
			        		"&auth_signature=" + signature + "&";
			        
			        request.setURI(new URI(url));
			        request.setHeader("Content-type", "application/json");
			        StringEntity se = new StringEntity(str[0]);
			        request.setEntity(se);
			        ResponseHandler<String> responseHandler=new BasicResponseHandler();
			        response = client.execute(request, responseHandler);
			    } catch (URISyntaxException e) {
			        e.printStackTrace();
			    } catch (ClientProtocolException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    } catch (IOException e) {
			        // TODO Auto-generated catch block
			        e.printStackTrace();
			    } catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
			
			return null;
	    }

	    protected void onPostExecute(Void feed) {
	    	if (response != null) {
				Log.e("SR", response);
				
				speakOut("Message Sent");
	    	}
	    }

	
	 }

	
	private String signed(String str) {
		SecretKeySpec keySpec = new SecretKeySpec(
		        "766ab98a6680f2a9a532".getBytes(),
		        "HmacSHA256");

		Mac mac;
		try {
			mac = Mac.getInstance("HmacSHA256");
			mac.init(keySpec);
			byte[] result = mac.doFinal(str.getBytes());
			return toHex(result);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return "";
	}

	public static String toHex(byte[] buf) {
        if (buf == null)
                return "";
        StringBuffer result = new StringBuffer(2*buf.length);
        for (int i = 0; i < buf.length; i++) {
                appendHex(result, buf[i]);
        }
        return result.toString();
	}
	private final static String HEX = "0123456789abcdef";
    private static void appendHex(StringBuffer sb, byte b) {
            sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
    }
	

	public void listen(boolean t) {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);        
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"voice.recognition.test");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3);
        
        sr.startListening(intent);
        
        
	}

	public void stopListen() {
		sr.stopListening();
	}

	public boolean getPresent() {
		return present;
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			 
            int result = tts.setLanguage(Locale.US);
 
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
 
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
		
	}

	private void speakOut(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		
	}
    
}


