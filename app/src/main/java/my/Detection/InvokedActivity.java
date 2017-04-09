package my.Detection;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class InvokedActivity extends Activity
{

	ImageView imageview;
	TextView textview;
	Button button;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invoked);
		imageview = (ImageView) this.findViewById(R.id.InvokedActivityImage);
		imageview.setAlpha(255);
		textview = (TextView) this.findViewById(R.id.InvokedActivityText);
		textview.setTextSize(20);
		button = (Button) this.findViewById(R.id.OK);
		button.setTextSize(20);
		button.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg)
			{
				finish();
			}
		});
	}

	protected void onStop()
	{
		super.onStop();
	}
}
