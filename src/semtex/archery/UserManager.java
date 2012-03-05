package semtex.archery;

import java.util.ArrayList;
import java.util.List;

import semtex.archery.entities.User;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class UserManager extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.usermanager);
		
		User blahr = new User(1L, "lalalal", "mail@gmx.at", 3333);
		User blub = new User(2L, "asdasdfasdf", "hudeeeeeeeeeee@orf.at", 776776);

		ArrayList<User> users = new ArrayList<User>();
		users.add(blahr);
		users.add(blub);
		Button addUser = (Button) findViewById(R.id.btnAdd);
		addUser.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), AddEditUser.class);
				startActivity(intent);
			}
		});

		Object obj = findViewById(R.id.lvUsers);
		ListView listView = (ListView) findViewById(R.id.lvUsers);
		ArrayAdapter<User> adapter = new UserAdapter(this, R.layout.user_row, users);
		listView.setAdapter(adapter);
	}
	
	private class UserAdapter extends ArrayAdapter<User> {

		public UserAdapter(Context context, int textViewResourceId,
				List<User> objects) {
			super(context, textViewResourceId, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			User currentUser = getItem(position);
			if(v == null) {
				LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.user_row, null);
			}
			
			TextView userName = (TextView) v.findViewById(R.id.txtUsername);
			userName.setText(currentUser.getUserName());
			
			TextView mail = (TextView) v.findViewById(R.id.txtMail);
			mail.setText(currentUser.getMail());
			
			View view = v.findViewById(R.id.dummyview_bg);
			view.setBackgroundColor(Color.GREEN);
			view.invalidate();
			
			return v;
		}
		
	}
}
