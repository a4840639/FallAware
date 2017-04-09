package my.Detection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.widget.ImageView;
import android.widget.TextView;

public class Welcome extends Activity
{

	private static final String PREFS_NAME = "Initial_Ref";
	private Handler mHandler = new Handler();

	ImageView imageview;
	TextView textview;
	int alpha = 255;
	int b = 0;
	
	private TelephonyManager telephonyManager;
	private String deviceId;
	private DataTransfer dt;


	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
        dtThread.run();
        
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		deviceId = telephonyManager.getDeviceId();

		try
		{
			ByteArrayInputStream ins = new ByteArrayInputStream(
					deviceId.getBytes());
			MessageDigest digester = MessageDigest.getInstance("SHA-512");
			byte[] bytes = new byte[8192];
			int byteCount;
			while ((byteCount = ins.read(bytes)) > 0)
			{
				digester.update(bytes, 0, byteCount);
			}

			byte[] digest = digester.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < digest.length; i++)
			{
				sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16)
						.substring(1));
			}

			deviceId = new String(sb.toString());
		}
		catch (NoSuchAlgorithmException nsae)
		{
			nsae.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		dt.send(System.currentTimeMillis(), deviceId);

		
		
		imageview = (ImageView) this.findViewById(R.id.WelcomeImage);
		imageview.setAlpha(alpha);
		textview = (TextView) this.findViewById(R.id.WelcomeText);
		textview.setTextSize(20);

		new Thread(new Runnable()
		{
			public void run()
			{
				while (b < 2)
				{
					try
					{
						if (b == 0)
						{
							Thread.sleep(1000);
							b = 1;
						}
						else
						{
							Thread.sleep(50);
						}
						updateApp();
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			}
		}).start();

		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				super.handleMessage(msg);
				imageview.setAlpha(alpha);
				imageview.invalidate();
			}
		};
	}

	protected void onStop()
	{
		super.onStop();
	}
	
	Thread dtThread = new Thread()
	{
		public void run()
		{
			dt = new DataTransfer();
		}
	};     

	public void updateApp()
	{
		alpha -= 5;

		if (alpha <= 0)
		{
			b = 2;
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("initial", true);
			// Don't forget to commit your edits!!!
			editor.commit();

			// Intent intent = new Intent(this, my.Detection.Setting.class);
			Intent intent = new Intent(this, my.Detection.Operating.class);
			startActivity(intent);
			finish();
		}
		mHandler.sendMessage(mHandler.obtainMessage());
	}
}
