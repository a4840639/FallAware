package my.Detection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SensorServ extends Service implements SensorListener
{

	private SensorManager mSensorManager;
	private String TAG = "DJP_testing_SensorServ";

	private final int timePeroidOfStorage = 60; // seconds
	private final int SENSOR_DELAY = 5; /*
										 * SENSOR_DELAY_NORMAL is 5times per
										 * second
										 */
	/* SENSOR_DELAY_UI is 16times per second */
	private int tableIndex = timePeroidOfStorage * SENSOR_DELAY;

	private Long time_COM[] = new Long[tableIndex];
	private double COM[] = new double[tableIndex];
	private double theCOM[][] = new double[tableIndex][3];
	private int index_COM = -1;
	private double threshold_COM = 2.7;

	private Long time_ACC[] = new Long[tableIndex];
	private double ACC[] = new double[tableIndex];
	private double theACC[][] = new double[tableIndex][3];
	private int index_ACC = -1;

	private Long time_ORI[] = new Long[tableIndex];
	private double ORI[][] = new double[tableIndex][3];
	private int index_ORI = -1;

	private double positionValuesum = 0; // the position value is based on y's
											// value
	private int numOfPositionVal = 0;
	private int numOfTry2DetectActivity = 0;

	private Long greatAccelerationTime = 0L;
	private Long beforeFirstGreatAccelerationTime = 0L;
	private Long normalPositionHoldTime = 0L;

	private String battery_status;
	
	//private DataTransfer dt;

	// public double threshold;
	//
	// public boolean simpleDetect = false;

	public boolean toJudgeMagneticField;
	public boolean toJudgeAcceleration;
	private boolean toJudgePosition;
	private boolean toJudgeActive;
	private boolean toComparePosition;
	private boolean toAlert;
	public boolean alerted;
	private boolean greatAcceleration;
	public boolean initial;

	private final static double G = SensorManager.GRAVITY_EARTH;

	private int window = 8;
	private int overlap = 4;
	private int window_index = -1;

	private final IBinder binder = new MyBinder();
	PowerManager pm;
	PowerManager.WakeLock wl;

	private SensorReader myReaderView;
	private Detector detectorObject;
	
	//private String deviceId;
	private long fallTime = System.currentTimeMillis();

	public class MyBinder extends Binder
	{
		SensorServ getService()
		{
			return SensorServ.this;
		}
	}

	@Override
	public void onCreate()
	{
		/** LOG */
		android.util.Log.i(TAG, "service_onCreate!!!!!");

		mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
//		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
//		wl.acquire();
		/** LOG */
		android.util.Log.i(TAG, "acquire!!!!!");

		toJudgeMagneticField = true;
		toJudgeAcceleration = true;
		toJudgePosition = false;
		toJudgeActive = false;
		toComparePosition = false;
		toAlert = false;
		alerted = false;
		greatAcceleration = false;
		initial = true;

		mSensorManager.registerListener(this,
				SensorManager.SENSOR_ACCELEROMETER
						| SensorManager.SENSOR_ORIENTATION
						| SensorManager.SENSOR_MAGNETIC_FIELD, SensorManager.// SENSOR_DELAY_NORMAL/*5times
																				// per
																				// sencond*/);
				SENSOR_DELAY_NORMAL/* 16times per sencond */);
	}

	@Override
	public void onDestroy()
	{
		/** LOG */
		android.util.Log.i(TAG, "service_onDestroy!!!!!");
		mSensorManager.unregisterListener(this);
		//wl.release();
		/** LOG */
		android.util.Log.i(TAG, "release!!!!!");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return binder;
	}

	public void copyObject(SensorReader viewObject, Detector detectorObject)
	{
		this.myReaderView = viewObject;
		this.detectorObject = detectorObject;
	}

	public void onSensorChanged(int sensor, float[] values)
	{
		// Here, according to the rules of judgment, if the sensor value
		// changes...
		//Log.d(TAG, "onSensorChanged()");
		Long CurrentTime = Long.valueOf(System.currentTimeMillis());
		if (toAlert && (!alerted))
		{
			/** LOG */
			android.util.Log.i(TAG, "here start to showAlarms! ");
			detectorObject.showAlarms(CurrentTime);
			/** LOG */
			android.util.Log.i(TAG, "here showAlarms finished! ");
			toAlert = false;
			alerted = true;
			
			Log.i(TAG, "Uploading fall event to server");
			//dt.sendFall(System.currentTimeMillis(), deviceId);
			System.gc();
		}
		synchronized (this)
		{
			// Determine if the table is filled up; if so, we need to
			// upload data to the server.
			if (index_ORI == tableIndex - 1)
			{
				// Empirically, the "last" sensor to receive data at
				// once is orientation. So take its timestamp.
				Log.i(TAG, "Uploading data to server");
				//dt.send(time_ORI, theACC, ORI, theCOM, deviceId);
				
				index_ACC = 0;
				index_COM = 0;
				index_ORI = 0;
				
				System.gc();
			}
			else if (myReaderView != null && myReaderView.mBitmap != null)
			{
				final Canvas canvas = myReaderView.mCanvas;
				final Paint paint = myReaderView.mPaint;
				float deltaX = myReaderView.mSpeed;
				float newX_acc;
				float newX_com;

//				if (sensor == SensorManager.SENSOR_MAGNETIC_FIELD)
//				{
//					//Log.i(TAG, "Sensing mag field...");
//					double Magnetic_value = record_com(CurrentTime, values);
//
//					newX_com = myReaderView.mLastX + deltaX;
//					final float v1 = myReaderView.mYOffset
//							+ (float) (Magnetic_value) * myReaderView.mScale[1];
//					paint.setColor(myReaderView.mColors[1]);
//					canvas.drawLine(myReaderView.mLastX,
//							myReaderView.mLastValues[1], newX_com, v1, paint);
//					myReaderView.mLastValues[1] = v1;
//					myReaderView.mLastX = newX_com;
//
//					if (toJudgeMagneticField)
//					{
//						judgeMagneticField_com(CurrentTime, values);
//					}
//
//				}
				if (sensor == SensorManager.SENSOR_ACCELEROMETER)
				{
					//Log.i(TAG, "Sensing acceleration...");
					double G_value = record_acc(CurrentTime, values);

					newX_acc = myReaderView.mLastX + deltaX;
					final float v = myReaderView.mYOffset
							+ (float) (G_value - G) * myReaderView.mScale[0];
					paint.setColor(myReaderView.mColors[0]);
					canvas.drawLine(myReaderView.mLastX,
							myReaderView.mLastValues[0], newX_acc, v, paint);
					myReaderView.mLastValues[0] = v;
					myReaderView.mLastX = newX_acc;

					if (toJudgeAcceleration)
					{
						judgeAcceleration_acc(CurrentTime, values);
					}
					if (toJudgeActive)
					{
						judgeActive_acc(CurrentTime, values);
					}
				}
				else if (sensor == SensorManager.SENSOR_ORIENTATION)
				{
					//Log.i(TAG, "Sensing orientation...");
					record_ori(CurrentTime, values);

					for (int i = 0; i < 3; i++)
					{
						myReaderView.mOrientationValues[i] = values[i];
					}

					if (toJudgePosition)
					{
						judgePosition_ori(CurrentTime, values);
					}
					if (toComparePosition)
					{
						comparePosition_ori(CurrentTime, values);
					}
				}
				//myReaderView.invalidate();
			}
		}
	}

	// @Override
	public void onAccuracyChanged(int sensor, int accuracy)
	{
		// TODO Auto-generated method stub

	}

	private double record_com(Long Time, float values[])
	{
		if (index_COM == tableIndex - 1)
		{
			Log.e(TAG, "Writing " + tableIndex + "th value (mag)");
			
//			String comStr = "[";
//			for (double c: COM)
//			{
//				comStr = comStr + c + ",";
//			}
//			comStr = comStr + "]";
//			
//			Log.e(TAG, "Magnetic values: " + comStr);
		}
		
		index_COM = (index_COM + 1) % tableIndex;
		time_COM[index_COM] = Time;
		COM[index_COM] = Math.sqrt(values[0] * values[0] + values[1]
				* values[1] + values[2] * values[2]);
		
		for (int i = 0; i < 3; i++)
		{
			theCOM[index_COM][i] = values[i];
		}
				
		return COM[index_COM];
	}

	private void judgeMagneticField_com(Long Time, float values[])
	{
		double a = 0;
		window_index = (window_index + 1) % tableIndex;

		if (this.initial == true)
		{
			if (window_index % window == (window - 1))
			{
				a = (COM[window_index] - COM[(window_index - window + 1)])
						/ (window - 1); // to be updated
				this.initial = false;
			}
		}
		else
		{
			if (window_index % overlap == (overlap - 1))
			{
				a = (COM[window_index] - COM[((window_index - window + 1 + tableIndex) % tableIndex)])
						/ (window - 1); // to be updated
			}
		}

		if (a >= threshold_COM)
		{
			fallTime = System.currentTimeMillis();
			this.toAlert = true;
			this.toJudgeMagneticField = false;
		}
	}

	private double record_acc(Long Time, float values[])
	{
		if (index_ACC == tableIndex - 1)
		{
			Log.e(TAG, "Writing " + tableIndex + "th value (acc)");
		}
		// /To Do: write values[]'s values into the big table of sensor for
		// several minutes.
		index_ACC = (index_ACC + 1) % tableIndex;
		time_ACC[index_ACC] = Time;

		for (int i = 0; i < 3; i++)
		{
			theACC[index_ACC][i] = values[i];
		}
		
		ACC[index_ACC] = Math.sqrt(values[0] * values[0] + values[1]
				* values[1] + values[2] * values[2]);
		return ACC[index_ACC];
	}

	private void judgeAcceleration_acc(Long Time, float values[])
	{
		/** for Judging whether the acc value is big enough. **/
		if (ACC[index_ACC] > 1 * G)
		{
			double current_ACC = ACC[index_ACC];
			double min_ACC = G;
			for (int i = 1; i <= 1.5 * SENSOR_DELAY; i++)
			{ // backward for 1.5 seconds, 7 data //related to sensor frequency
				int index;
				if ((index_ACC - i) >= 0)
				{
					index = index_ACC - i;
				}
				else
				{
					index = index_ACC - i + tableIndex;
					if (ACC[index] == 0)
						break;
				}
				if (ACC[index] < min_ACC)
				{
					min_ACC = ACC[index];
				}
			}

			if ((current_ACC - min_ACC) > myReaderView.threshold)
			{ // 0.6*G){ // modify to 0.8g for debugging //1*G){
				greatAccelerationTime = Time;
				if (beforeFirstGreatAccelerationTime == 0) // record the time 1
															// sec before-
					beforeFirstGreatAccelerationTime = Time - 1000; // -first
																	// great ACC
																	// happens
				greatAcceleration = true;
				/** LOG */
				android.util.Log.i(
						TAG,
						"greatAcce.! now the threshold is "
								+ myReaderView.threshold + " That is "
								+ String.valueOf(current_ACC - min_ACC));
				myReaderView.bigACCDetected = true;
			}
		}

		// great acceleration change and waited for 5 seconds ///2 seconds
		if (greatAcceleration && ((Time - greatAccelerationTime) > 2000))
		{ // 5000
			/** LOG */
			android.util.Log.i(TAG, "2 seconds time up! ");
			greatAcceleration = false;
			toJudgeAcceleration = false;
			normalPositionHoldTime = beforeFirstGreatAccelerationTime; // copy
																		// the
																		// time
																		// 1 sec
																		// before
																		// first
																		// great-
			beforeFirstGreatAccelerationTime = Long.valueOf(0);// -ACC happens
																// as the normal
																// position time
			toJudgePosition = true;// To decide to judge whether the position is
									// horizontal
		}
	}

	private void judgeActive_acc(Long Time, float values[])
	{
		/** for Judging whether the users is active(means having some action) */
		/** according to the acc values. */
		numOfTry2DetectActivity++;
		if (Math.abs(ACC[index_ACC] - G) >= 0.15 * G)
		{ // Is it necessary here to determine whether great acceleration
			toJudgeActive = false; // happened?
			toJudgeAcceleration = true;
			myReaderView.bigACCDetected = false;
			/** LOG */
			android.util.Log.i(
					TAG,
					"nomal acceleration detected! go2 initial. " +
					/** LOG */
					"the numOfTry2DetectActivity is "
							+ String.valueOf(numOfTry2DetectActivity)
							+ ", and will be set to 0" +
							/** LOG */
							" now the index_ACC is "
							+ String.valueOf(index_ACC));
			numOfTry2DetectActivity = 0;
		}
		else
		{
			if (numOfTry2DetectActivity >= 3 * SENSOR_DELAY)
			{ // take 3 seconds as reference
				// that means no-activities in 3 seconds!!
				/** for test important!!!! */
				// test1.showNotification_Tiny(Time);
				/** LOG */
				android.util.Log.i(TAG, "no activities. compare positions.");
				toComparePosition = true; // //////////////////////////////////////////////
				toJudgeActive = false;
				numOfTry2DetectActivity = 0;
			}
		}
	}

	private void record_ori(Long Time, float values[])
	{
		if (index_ORI == tableIndex - 1)
		{
			Log.e(TAG, "Writing " + tableIndex + "th value (orient)");
		}

		// /To Do: write values[]'s values into the big table of sensor for
		// several minutes.
		index_ORI = (index_ORI + 1) % tableIndex;
		time_ORI[index_ORI] = Time;
		ORI[index_ORI][0] = values[0]; // record the x, y and z values
		ORI[index_ORI][1] = values[1];
		ORI[index_ORI][2] = values[2];
	}

	private void judgePosition_ori(Long Time, float values[])
	{
		// /To Do: judge whether the position is horizontal
		if (myReaderView.simpleDetect)
		{ // tricky
			/** LOG */
			android.util.Log.i(TAG,
					"Simple Detect. Skip judging position! Alert!");
			fallTime = System.currentTimeMillis();
			toAlert = true;
			// positionValuesum = 0;
			// numOfPositionVal = 0;
			toJudgePosition = false;
			return;
		}

		positionValuesum = positionValuesum + values[1];
		/** LOG */
		android.util.Log.i(TAG, "y's value is" + String.valueOf(values[1])
				+ "sum is " + String.valueOf(positionValuesum));
		numOfPositionVal++;
		if (numOfPositionVal >= 2 * SENSOR_DELAY)
		{ // take the position value in 2 seconds as reference
			/** LOG */
			android.util.Log.i(
					TAG,
					"average y values is "
							+ String.valueOf(positionValuesum
									/ numOfPositionVal));
			if (judgeHorizontal(positionValuesum / numOfPositionVal))
			{
				/** LOG */
				android.util.Log.i(TAG, "the phone is horizontal! Then alert!");
				toAlert = true;
			}
			else
			{
				toJudgeActive = true;
				/** LOG */
				android.util.Log.i(
						TAG,
						"the phone is tilted! Then detect action in 3 seconds."
								+ "now the index_ACC is "
								+ String.valueOf(index_ACC));
			}
			positionValuesum = 0;
			numOfPositionVal = 0;
			toJudgePosition = false;
		}
	}

	private void comparePosition_ori(Long Time, float values[])
	{
		// To Do: compare the current position with the one at 1 second before
		// fist great acceleration happened
		double current_ori_y = values[1];
		double current_ori_z = values[2];
		int index;
		if ((index_ORI - 1) >= 0)
			index = index_ORI - 1;
		else
			index = index_ORI - 1 + tableIndex;
		while (time_ORI[index] >= normalPositionHoldTime)
		{
			if ((index - 1) >= 0)
				index = index - 1;
			else
				index = index - 1 + tableIndex;
		}
		double normal_ori_y = ORI[(index + 1) % tableIndex][1];
		double normal_ori_z = ORI[(index + 1) % tableIndex][2];
		/** LOG */
		android.util.Log.i(
				TAG,
				"normal y and z are " + String.valueOf(normal_ori_y) + " and "
						+ String.valueOf(normal_ori_z) + "index is "
						+ String.valueOf(index + 1));
		/** LOG */
		android.util.Log.i(TAG,
				"current y and z are " + String.valueOf(current_ori_y)
						+ " and " + String.valueOf(current_ori_z));
		if ((Math.abs(current_ori_y - normal_ori_y) < 5)
				&& (Math.abs(current_ori_z - normal_ori_z) < 5))
		{
			toComparePosition = false;
			toJudgeAcceleration = true;
			myReaderView.bigACCDetected = false;
			/** LOG */
			android.util.Log.i(TAG,
					"position unchanged with that before great ACC happened! " +
					/** LOG */
					"return to initial ");
		}
		else
		{
			/** LOG */
			android.util.Log
					.i(TAG,
							"position changed with that before great ACC happens! alert!");
			fallTime = System.currentTimeMillis();
			toComparePosition = false;
			toAlert = true;
		}
	}

	private boolean judgeHorizontal(double ori_y_value)
	{
		double result;
		result = 90 - Math.abs(Math.abs(ori_y_value) - 90);
		if (result <= 5)
			return true;
		else
			return false;
	}

	private void monitorBatteryState()
	{
		BroadcastReceiver battReceiver = new BroadcastReceiver()
		{
			public void onReceive(Context context, Intent intent)
			{
				StringBuilder sb = new StringBuilder();

				context.unregisterReceiver(this);
				// int status = intent.getIntExtra("status", -1);
				int rawlevel = intent.getIntExtra("level", -1);
				int scale = intent.getIntExtra("scale", -1);
				// int health = intent.getIntExtra("health", -1);
				int voltage = intent.getIntExtra("voltage", -1);
				int level = -1; // percentage, or -1 for unknown
				if (rawlevel >= 0 && scale > 0)
				{
					level = (rawlevel * 100) / scale;
				}

				sb.append("rawlevel: " + String.valueOf(rawlevel) + "      "
						+ "scale: " + String.valueOf(scale) + "      "
						+ "level: " + String.valueOf(level) + "      "
						+ "voltage: " + String.valueOf(voltage) + "mv");

				sb.append('\n');
				battery_status = sb.toString();
				// mSwitcher1.setText(sb.toString());
			}
		};
		IntentFilter battFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(battReceiver, battFilter);
	}
	
//	Thread dtThread = new Thread()
//	{
//		public void run()
//		{
//			dt = new DataTransfer();
//		}
//	};
}
