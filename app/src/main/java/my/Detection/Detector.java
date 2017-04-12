package my.Detection;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Toast;

public class Detector extends AppCompatActivity {// implements ViewFactory {

	private static final String PREFS_NAME = "Initial_Ref";
	//    private SensorManager mSensorManager;
	private NotificationManager nm;
	private boolean vibrationOrNot;
	private long vibrating_time[] = new long[]{100, 250, 100, 500};

	private final int RG_REQUEST = 1;
	private String soundType;
	private Uri soundUri;
	private long vibration[];
	private String phoneNo;
	private String sensitivity;// = "Medium";
	private final static double G = SensorManager.GRAVITY_EARTH;

	private String TAG = "DJP_testing_Detector";
	private long delay;
	private Location myLocation;
	private LocationManager lm;

	private SensorReader myReaderView;
	private SensorServ mService;
	private Detector detectorObject = this;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName classname, IBinder serviceBinder) {
			//Called when the connection is made.
			mService = ((SensorServ.MyBinder) serviceBinder).getService();
			mService.copyObject(myReaderView, detectorObject);
		}

		public void onServiceDisconnected(ComponentName className) {
			//Received when the service unexpectedly disconnects.
			mService = null;
		}
	};

	/**
	 * Initialization of the Activity after it is first created.  Must at least
	 * call {@link android.app.Activity#setContentView setContentView()} to
	 * describe what is to be displayed in the screen.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
/**LOG*/android.util.Log.i(TAG, "BeginInDetector!!!!!");

		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        ////////////////////////////////////////////////////////////////////////////////
//        // Test if OpenIntents is present (for sensor settings)
//        OpenIntents.requiresOpenIntents(this);
//        // !! Very important !!
//        // Before calling any of the Simulator data,
//        // the Content resolver has to be set !!
//        Hardware.mContentResolver = getContentResolver();
//        // Link sensor manager to OpenIntents Sensor simulator
//        // mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        mSensorManager = (SensorManager) new SensorManagerSimulator((SensorManager) getSystemService(SENSOR_SERVICE));
//		/////////////////////////////////////////////////////////////////////////////////

		//Bind to the service
		Intent bindIntent = new Intent(this, SensorServ.class);
		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

		lm = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);

		/** Show this screen */
		Context context = getApplicationContext();
		myReaderView = new SensorReader();
		setContentView(R.layout.detector);
		get_Pref();
//        mService.copyObject(myReaderView, this);

		if (ContextCompat.checkSelfPermission(context,
				Manifest.permission.SEND_SMS)
				!= PackageManager.PERMISSION_GRANTED) {
			if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.SEND_SMS},
						0);
			}

		}
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }

        }
	}

	@Override
	protected void onResume() {
		super.onResume();
//        mSensorManager.registerListener(myReaderView, 
//                SensorManager.SENSOR_ACCELEROMETER | 
////                SensorManager.SENSOR_MAGNETIC_FIELD | 
//                SensorManager.SENSOR_ORIENTATION,
//                SensorManager.SENSOR_DELAY_NORMAL/*5times per second*/);
//							//SENSOR_DELAY_UI/*16times per second*/);
	}

	@Override
	protected void onStop() {
//        mSensorManager.unregisterListener(myReaderView);
		super.onStop();
		nm.cancel(R.string.notifier);
	}

	@Override
//will be called when the *Detector* activity receives *setting* activity result. 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RG_REQUEST) {
			get_Pref();
		}
	}

	/**  Display a Alarm */
	public void showAlarms(Long Time) {
		/**LOG*/android.util.Log.i(TAG, "received call. here start to showAlarms! ");
		showNotification();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, ll);
        }
		handler.postDelayed(gpsRun, delay);
		handler.postDelayed(sms, delay);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		/**LOG*/android.util.Log.i(TAG, "here showNotification finished! ");
		//showAlert();
		/**LOG*/        //android.util.Log.i(TAG, "here showAlert finished! ");
		myReaderView.blockedstate = true;
	}

	private void showAlert() {
		/**  generate the dialog! */
		new AlertDialog.Builder(this)
				//.setIcon(R.drawable.alert_dialog_icon)
				.setTitle("Alarm!")
				.setMessage("fall down")
				.setPositiveButton("Ouch",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								//Do Nothing.
							}
						}).show();
		//pop-up dialog
//	    Toast.makeText(this, "for test",Toast.LENGTH_LONG).show();
	}

	private void showNotification() {

		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, Detector.class);
		resultIntent.setFlags(resultIntent.FLAG_ACTIVITY_SINGLE_TOP);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(Detector.class);
// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
				stackBuilder.getPendingIntent(
						0,
						PendingIntent.FLAG_UPDATE_CURRENT
				);


		Notification.Builder mBuilder =
				new Notification.Builder(this)
						.setContentTitle("Alert!")
						.setContentText("Fall detected!")
						.setVibrate(vibration)
						.setSound(soundUri)
						.setPriority(2)
						.setAutoCancel(true)
						.setOngoing(true)
						.setContentIntent(resultPendingIntent)
                        .setShowWhen(true)
                        .setSmallIcon(R.drawable.warning_xxl);
		nm.notify(0, mBuilder.build());
	}

	private void get_Pref() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		soundType = settings.getString("default_Sound", "one");
		if (soundType.equals("one")) {
			soundUri = Uri.parse("file:///system/media/audio/alarms/Xenon.ogg");
		} else if (soundType.equals("two")) {
			soundUri = Uri.parse("file:///system/media/audio/alarms/Osmium.ogg");
		} else if (soundType.equals("silent")) {
			soundUri = null;
		}
		vibrationOrNot = settings.getBoolean("default_Vibrate", true);
		if (vibrationOrNot) {
			vibration = vibrating_time;
		} else {
			vibration = null;
		}
		delay = Long.parseLong(settings.getString("default_Delay", "30")) * 1000;
		phoneNo = settings.getString("default_Phone", "");
		sensitivity = settings.getString("default_Sensitivity", "Medium");
/**LOG*/android.util.Log.i(TAG, "sensitivity's value is " + sensitivity);
		if (sensitivity.equals("Low")) {
			myReaderView.threshold = 4.0 * G;
			myReaderView.simpleDetect = false;
		} else if (sensitivity.equals("Medium")) {
			myReaderView.threshold = 2.0 * G;
			myReaderView.simpleDetect = false;
		} else {                                //if (sensitivity == "High"){
			myReaderView.threshold = 1.5 * G;
			myReaderView.simpleDetect = true;
		}
	}


	public void setting(View view) {
		Intent intent = new Intent(this, my.Detection.Setting.class);
		startActivityForResult(intent, RG_REQUEST);
	}

	public void quit(View view) {
		nm.cancel(0);
		unbindService(mConnection);
		System.exit(0);
		finish();
	}

	public void reset(View view) {

		nm.cancel(0);
		reinit();
	}

	protected void sendSMSMessage() {
		String message = "A fall is detected!";
        if(myLocation != null) {
            message += "\nLocation: http://maps.google.com/?q=" + myLocation.getLatitude() + "," + myLocation.getLongitude();
        }
		if (phoneNo != "" && ContextCompat.checkSelfPermission(getApplicationContext(),
				Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phoneNo, null, message, null, null);
			Toast.makeText(getApplicationContext(), "SMS sent.",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		reinit();

		unbindService(mConnection);
	}

	protected void reinit() {
		mService.alerted = false;
		mService.toJudgeAcceleration = true;
		mService.toJudgeMagneticField = true;
		myReaderView.blockedstate = false;
		myReaderView.bigACCDetected = false;
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		handler.removeCallbacks(sms);
		Toast.makeText(getApplicationContext(), "Reseted.",
				Toast.LENGTH_LONG).show();
	}

	Handler handler = new Handler();
	Runnable sms = new Runnable() {
		@Override
		public void run() {
			android.util.Log.i(TAG, "SMS!!!!!");
			sendSMSMessage();
		}
	};

	Runnable gpsRun = new Runnable() {
		@Override
		public void run() {
            lm.removeUpdates(ll);
            android.util.Log.i(TAG, "Location!!!!!");
		}
	};


	private final LocationListener ll = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			myLocation = location;
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}
	};

}
