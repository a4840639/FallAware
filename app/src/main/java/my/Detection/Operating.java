package my.Detection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class Operating extends Activity
{

	TextView textview;
	TextView blank1;
	TextView blank2;
	TextView blank3;
	ImageButton button1;
	ImageButton button2;
	ImageButton button3;
	Context packageContext = this;

	public void onCreate(Bundle savedInstanceState)
	{

		Intent intent = new Intent(packageContext,
				my.Detection.Detector.class);
		startActivity(intent);
		finish();
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.operating);
//		textview = (TextView) this.findViewById(R.id.OperatingActivityText);
//		textview.setTextSize(30);
//		button1 = (ImageButton) this
//				.findViewById(R.id.OperatingActivityButton1);
//		button1.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View arg)
//			{
//				Intent intent = new Intent(packageContext,
//						my.Detection.Detector.class);
//				startActivity(intent);
//				finish();
//			}
//		});
//
//		button2 = (ImageButton) this
//				.findViewById(R.id.OperatingActivityButton2);
//		// button2.setTextSize(25);
//		button2.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View arg)
//			{
//
//				Intent intent = new Intent(packageContext,
//						my.Detection.Setting.class);
//				startActivity(intent);
//				finish();
//			}
//		});
//
//		button3 = (ImageButton) this
//				.findViewById(R.id.OperatingActivityButton3);
//		// button3.setTextSize(25);
//		button3.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View arg)
//			{
//				finish();
//			}
//		});
//
//		blank1 = (TextView) this.findViewById(R.id.OperatingActivityBlank1);
//		blank1.setTextSize(25);
//		blank2 = (TextView) this.findViewById(R.id.OperatingActivityBlank2);
//		blank2.setTextSize(25);
//		blank3 = (TextView) this.findViewById(R.id.OperatingActivityBlank3);
//		blank3.setTextSize(25);
	}

	protected void onStop()
	{
		super.onStop();
	}
}
