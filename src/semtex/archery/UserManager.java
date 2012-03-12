
package semtex.archery;

import java.util.List;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.entities.User;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;


public class UserManager extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final int REQUEST_CODE = 0;


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.usermanager);

    final Button addUser = (Button)findViewById(R.id.btnAdd);
    addUser.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final Intent intent = new Intent(v.getContext(), AddEditUser.class);
        startActivityForResult(intent, REQUEST_CODE);
      }
    });

    fillUsers();

    final ListView listView = (ListView)findViewById(R.id.lvUsers);
    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

      public boolean onItemLongClick(final AdapterView<?> av, final View v, final int i, final long l) {
        final User user = (User)listView.getAdapter().getItem(i);
        final Intent intent = new Intent(v.getContext(), AddEditUser.class);
        intent.putExtra("userid", user.getId());
        startActivityForResult(intent, REQUEST_CODE);
        return true;
      }

    });
  }


  @Override
  protected void onResume() {
    super.onResume();
    fillUsers();
  }


  private void fillUsers() {
    final RuntimeExceptionDao<User, Long> userDao = getHelper().getUserDao();
    final List<User> users = userDao.queryForAll();
    final ListView listView = (ListView)findViewById(R.id.lvUsers);
    final ArrayAdapter<User> adapter = new UserAdapter(this, R.layout.user_row, users);
    listView.setAdapter(adapter);
  }

  public class UserAdapter extends ArrayAdapter<User> {

    public UserAdapter(final Context context, final int textViewResourceId, final List<User> objects) {
      super(context, textViewResourceId, objects);
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      View v = convertView;
      final User currentUser = getItem(position);
      if (v == null) {
        final LayoutInflater li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        v = li.inflate(R.layout.user_row, null);
      }

      final TextView userName = (TextView)v.findViewById(R.id.txtUsername);
      userName.setText(currentUser.getUserName());

      final TextView mail = (TextView)v.findViewById(R.id.txtMail);
      mail.setText(currentUser.getMail());

      final GradientDrawable gd =
          new GradientDrawable(Orientation.RIGHT_LEFT, new int[] { currentUser.getRgbColor() & 0x00FFFFFF | 0xAA000000,
              0x0 });
      v.setBackgroundDrawable(gd);
      return v;
    }

  }
}
