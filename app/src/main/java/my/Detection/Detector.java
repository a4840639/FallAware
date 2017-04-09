package my.Detection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

public class Detector extends Activity{// implements ViewFactory {

	private static final String PREFS_NAME = "Initial_Ref";
//    private SensorManager mSensorManager;
    private NotificationManager nm;
    private boolean vibrationOrNot;
    private long vibrating_time[] = new long[]{100,250,100,500};
    
	private final int RG_REQUEST = 0; 
	private String soundType;
	private Uri soundUri;
    private long vibration[];
	private String alarmInfo;
	private String sensitivity;// = "Medium";
	private final static double G = SensorManager.GRAVITY_EARTH;
	
	private String TAG = "DJP_testing_Detector";
	
	private SensorReaderView myReaderView;
	private SensorServ mService;
	private Detector detectorObject = this;
	  
    private ServiceConnection mConnection = new ServiceConnection(){
	   public void onServiceConnected(ComponentName classname,IBinder serviceBinder){
		   //Called when the connection is made.
		   mService = ((SensorServ.MyBinder)serviceBinder).getService();
		   mService.copyObject(myReaderView, detectorObject);
	   }
	   public void onServiceDisconnected(ComponentName className){
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
		Intent bindIntent = new Intent(this,SensorServ.class);
		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        
		/** Show this screen */
        Context context = getApplicationContext();
        myReaderView = new SensorReaderView(context);
        setContentView(myReaderView);
        get_Pref();
//        mService.copyObject(myReaderView, this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	if (requestCode == RG_REQUEST) {
    	      if(resultCode == RESULT_OK) {
    	    	  get_Pref();
    	      }
    	 }
    }
    
	/**  Display a Alarm */
    public void showAlarms(Long Time){
    	/**LOG*/		android.util.Log.i(TAG, "received call. here start to showAlarms! ");
        showNotification(Time);
        /**LOG*/		android.util.Log.i(TAG, "here showNotification finished! ");
    	//showAlert();
    	/**LOG*/		//android.util.Log.i(TAG, "here showAlert finished! ");
    	myReaderView.blockedstate = true;
    }
    
    private void showAlert(){
    	/**  generate the dialog! */
        new AlertDialog.Builder(this)
        .setIcon(R.drawable.alert_dialog_icon)
        .setTitle("Alarm!")
        .setMessage(alarmInfo)
        .setPositiveButton("Ouch", 
       		new DialogInterface.OnClickListener() {
       	 		public void onClick(DialogInterface dialog, int whichButton) {
       	 			//Do Nothing.
       	 		}
        	}).show();
    	//pop-up dialog
//	    Toast.makeText(this, "for test",Toast.LENGTH_LONG).show();
    }
    
    private void showNotification(Long Time){
    	// The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,   
                new Intent(this, my.Detection.InvokedActivity.class), 0);   
        		//test.class is the class will be invoked in next intent(just the "contentIntent")
        		//It could be make phone call
        // construct the Notification object.
    	Notification notif = new Notification(R.drawable.alert_dialog_icon,   
                "Alarm!\n"+alarmInfo+"!!", Time);   
    	// Set the info for the views that show in the notification panel.
        // must set this for content view, or will throw a exception   
    	notif.setLatestEventInfo(this, "Alarm!",   
    			alarmInfo+"!!", contentIntent);   
    	//an array of longs of times to turn the vibrator off and on.
    	// after a 100ms delay, vibrate for 250ms, pause for 100 ms and
        // then vibrate for 500ms.
    	// for being able to vibrate, the permission should be add in manifest.xml
        notif.vibrate = vibration;
    	// play alarm sound 
    	notif.defaults = notif.DEFAULT_SOUND;
        notif.sound = soundUri;
//      notif.sound = Uri.parse("file:///system/media/audio/alarms/Alarm_Beep_01.ogg");
        nm.notify(R.string.notifier, notif);   
    }
    
    private void get_Pref(){
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		soundType = settings.getString("sound","one");
		if(soundType.equals("one")){
			soundUri = Uri.parse("file:///system/media/audio/alarms/Alarm_Beep_01.ogg");
		}
		else if(soundType.equals("two")){
			soundUri = Uri.parse("file:///system/media/audio/alarms/Alarm_Buzzer.ogg");
		}
		else if(soundType.equals("silent")){
			soundUri = null;
		}
        vibrationOrNot = settings.getBoolean("vibrate", false);
        if(vibrationOrNot) {
        	vibration = vibrating_time;
        }
        else{
        	vibration = null;
        }
        alarmInfo = settings.getString("alarmInfo", "Fall down");
        sensitivity = settings.getString("sensitivity", "Medium");
/**LOG*/android.util.Log.i(TAG, "sensitivity's value is "+sensitivity);
        if (sensitivity.equals("High")){
        	myReaderView.threshold = 4.0*G;
        	myReaderView.simpleDetect = false;
        }
        else if (sensitivity.equals("Medium")){
        	myReaderView.threshold = 0.6*G;
        	myReaderView.simpleDetect = false;
		}
		else {								//if (sensitivity == "Low"){
			myReaderView.threshold = 0.4*G;
			myReaderView.simpleDetect = true;
		}
    }

///////////////////////////////////////////////////////////////////////////
    /** Add some menus for...							  *////////////////
///////////////////////////////////////////////////////////////////////////
    
	private static final int MENU_RESET = Menu.FIRST;
	private static final int MENU_SETTINGS = Menu.FIRST + 1;
	private static final int MENU_QUIT = Menu.FIRST + 2;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Standard menu
		menu.add(0, MENU_RESET, 0, "Reset")
			.setIcon(R.drawable.mobile_shake001a)//change the icon
			.setShortcut('0', 'r');
		menu.add(1, MENU_SETTINGS, 0, "Configuration")
			.setIcon(R.drawable.mobile_shake001a) //change the icon
			.setShortcut('0', 'c');
		menu.add(2, MENU_QUIT, 0, "Quit")//change the icon
			.setIcon(R.drawable.mobile_shake001a)
			.setShortcut('1', 'q');
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_RESET:
			mService.alerted = false;
			mService.toJudgeAcceleration = true;
			mService.toJudgeMagneticField = true;
	        nm.cancel(R.string.notifier);  
	        myReaderView.blockedstate = false;
	        myReaderView.bigACCDetected = false;
			return true;
			
		case MENU_SETTINGS:
			Intent mIntent = new Intent(this, my.Detection.Setting.class);
			startActivityForResult(mIntent,RG_REQUEST); 
			return true;
			
		case MENU_QUIT:
	        new AlertDialog.Builder(this)
	        .setIcon(R.drawable.alert_dialog_icon)  //change the icon
	        .setTitle("Quit")
	        .setMessage("Are you sure?")// index is "+index)
	        .setPositiveButton("YES", 
	       		new DialogInterface.OnClickListener() {
	       	 		public void onClick(DialogInterface dialog, int whichButton) {
//	       	 			setResult(RESULT_OK);
	       	 			unbindService(mConnection);
	       	 			System.exit(0);
	       	 			finish();
	       	 		}
	        	})
	        .setNegativeButton("NO", 
	       		new DialogInterface.OnClickListener() {
	       	 		public void onClick(DialogInterface dialog, int whichButton) {
	       	 			//Do Nothing.
	       	 		}
	        	})
	        .show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
