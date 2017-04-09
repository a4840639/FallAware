package my.Detection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DataTransfer
{

	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	private final String TAG = "dataTransfer";

	public DataTransfer()
	{
		try
		{
			socket = new Socket("164.107.116.207", 8080);
			//in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (UnknownHostException e)
		{
			Log.e(TAG, "Can not find host.");
		}
		catch (IOException e)
		{
			Log.e(TAG, "IO exception");
		}
		catch (Exception e)
		{
			Log.e(TAG, "Exception occurred");
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}
	}
	
	public void sendFall(long t, String deviceId)
	{
		String tStr = "timestamp 1 [" + t + "]";
		String idStr = "IMEI " + deviceId;
		String typeStr = "type fall";
		
		try
		{
			Log.e(TAG, "t = " + t);
			out.writeObject(typeStr);
			out.writeObject(idStr); // device ID
			out.writeObject(tStr); // a timestamp object
			out.flush();
		}
		catch (Exception e)
		{
			Log.e(TAG, "Exception while sending");
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}		
	}

	public void send(long timestamp, String deviceId)
	{
		String tStr = "timestamp 1 [" + timestamp + "]";
		String idStr = "IMEI " + deviceId;
		String typeStr = "type register";
		
		try
		{
			Log.e(TAG, "t = " + timestamp);
			out.writeObject(typeStr);
			out.writeObject(idStr); // device ID
			out.writeObject(tStr); // a timestamp object
			out.flush();
		}
		catch (Exception e)
		{
			Log.e(TAG, "Exception while sending");
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}		
	}
	

	public void send(Long[] t, double[][] a, double[][] o, double[][] mag, String deviceId)
	{
		String tStr = "[";
		
		for (long aTime: t)
		{
			tStr = tStr + aTime + ",";
		}
				
		tStr = tStr.substring(0, tStr.length() - 1);
		tStr = "timestamp 50 " + tStr + "]";
		String aStr = generateString(a);
		String magStr = generateString(mag);
		String oStr = generateString(o);
		String idStr = "IMEI " + deviceId;
		String typeStr = "type falldata";
		
		aStr = "accelerometer 50 3 " + aStr;
		magStr = "magnetic 50 3 " + magStr;
		oStr = "orientation 50 3 " + oStr;
		
		try
		{
			Log.e(TAG, "t = " + t + "; a = " + a + "; o = " + o);
			out.writeObject(typeStr);
			out.writeObject(idStr); // device ID
			out.writeObject(tStr); // a timestamp object
			out.writeObject(aStr); // accelerometer array float[50][3]
			out.writeObject(magStr); // magnetic sensor array float[50][3]
			out.writeObject(oStr); // orentation sensor array float[50][3]
			out.flush();
		}
		catch (Exception e)
		{
			Log.e(TAG, "Exception while sending");
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Requires: Array has at least one row, one column
	 * Ensures: Return value is String representation of array
	 * @param x Two dimensional array of floats
	 * @return
	 */
	private String generateString(double[][] x)
	{
		String str = "";
		
		str = str + "[";

		for (int i = 0; i < x.length; i++)
		{
			
			str = str + "[";

			for (int j = 0; j < x[i].length; j++)
			{
				str = str + x[i][j];
				
				if (j < x[j].length - 1)
				{
					str = str + ",";
				}
			}
			
			str = str + "]";
						
			if (i < x.length - 1)
			{
				str = str + ";";
			}
		}

		str = str + "]";
		
		return str;
	}
}
