package semtex.archery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TacActivity extends Activity {
	public static final String TAG = TacActivity.class.getName();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button btnUser = (Button) findViewById(R.id.btnUsermanager);
        btnUser.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Log.i(TAG, "switching to user manager!");
				Intent intent = new Intent(v.getContext(), UserManager.class);
				startActivity(intent);
			}
		});
        
        Button btn = (Button) findViewById(R.id.btnStart);
        btn.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Log.i(TAG, "clicked!");
			}
		});       
        
        Button userManager = (Button) findViewById(R.id.btnHistory);
        userManager.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				 
			}
		});

    }
}