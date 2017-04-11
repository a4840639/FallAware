package my.Detection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;

public class Setting extends PreferenceActivity{
	
	private static final String PREFS_NAME = "Initial_Ref";
	private boolean mInitial;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Configuration");
        addPreferencesFromResource(R.xml.default_reference);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mInitial = settings.getBoolean("initial", true);
    }
    
	private static final int MENU_CONT = Menu.FIRST;
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		super.onCreateOptionsMenu(menu);
//
//		// Standard menu
//		menu.add(0, MENU_CONT, 0, "Continue")
//			.setIcon(R.drawable.mobile_shake001a) //change the icon
//			.setShortcut('0', 'c');
//		return true;
//	}
	
//// To change icon's title.
//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		super.onPrepareOptionsMenu(menu);
//        if (mInitial) {
//        	menu.findItem(MENU_CONT).setTitle("Continue").setShortcut('0', 'c');
//        }else {
//        	menu.findItem(MENU_CONT).setTitle("Save & Return Back").setShortcut('0', 'r');
//        }
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case MENU_CONT:
//			if(mInitial){
//				update_Pref();
//				Intent intent = new Intent(this, my.Detection.Detector.class);
//				startActivity(intent);
//	   	 		finish();
//			}
//			else{
//				update_Pref();
//				setResult(RESULT_OK);
//				finish();
//			}
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	private void update_Pref(){
//		//Preference default_Pref = findPreference("default_vibrate");
//		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//		String sensitivity = settings.getString("default_Sensitivity", "Medium");
//		String phone = settings.getString("crap", "6145562710");
//		String sound = settings.getString("default_Sound", "one");
//		boolean vibrate = settings.getBoolean("default_vibrate", false);
//
//		settings = getSharedPreferences(PREFS_NAME, 0);
//		SharedPreferences.Editor editor = settings.edit();
//		editor.putBoolean("initial", false);
//		editor.putString("sensitivity", sensitivity);
//		//editor.putString("phone", phone);
//		editor.putString("sound",sound);
//		editor.putBoolean("vibrate", vibrate);
//		// Don't forget to commit your edits!!!
//		editor.apply();
//	}
}
