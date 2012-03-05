package semtex.archery;

import semtex.archery.dialogs.ColorPickerDialog;
import semtex.archery.dialogs.ColorPickerDialog.OnColorChangedListener;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddEditUser extends Activity {
	
	public static final String TAG = AddEditUser.class.getName();
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_add_edit);
		
		final EditText username = (EditText) findViewById(R.id.txtUsername);
		
		final EditText mailText = (EditText) findViewById(R.id.txtMail); 
				
		final View view = findViewById(R.id.dummyview_col);
		
		final int[] persColor = new int[1];
		
		Button button = (Button) findViewById(R.id.btnPicker);
		button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				OnColorChangedListener listener = new OnColorChangedListener() {
					
					public void colorChanged(int color) {
						persColor[0] = color;
						view.setBackgroundColor(color);
					}
				};
				Dialog d = new ColorPickerDialog(v.getContext(), listener, Color.BLACK);
				d.show();
			}
		});
		
		Button btnSaveUser = (Button) findViewById(R.id.btnSaveUser);
		btnSaveUser.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Log.i(TAG, "Creating user with: " + username.getText() + ", mail: " + mailText.getText() + " and color: " + persColor[0]);
			}
		});
	}
}
