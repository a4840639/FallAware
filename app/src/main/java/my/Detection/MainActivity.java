package my.Detection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detector);

    }

    public void setting(View view) {
        Intent intent = new Intent(this, Setting.class);
        startActivity(intent);
    }

    public void quit(View view) {
        finish();
    }

}
