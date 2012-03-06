/**********************************************************************************************************************
 * StartParcour
 * 
 * created Mar 6, 2012 by semteX
 * 
 * (c) 2012 APEX gaming technology GmbH
 **********************************************************************************************************************/

package semtex.archery;

import java.util.List;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.entities.Parcour;
import semtex.archery.entities.data.entities.User;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;


/**
 * @author semteX
 * 
 */
public class StartParcour extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final String TAG = String.class.getName();


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start_parcour);
    final StartParcour instance = this;
    final Button btnAdd = (Button)findViewById(R.id.btnAddParcour);
    btnAdd.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final Dialog d = new Dialog(instance);
        d.setContentView(R.layout.parcour_add);
        d.setTitle("Add Parcour");

        final Button btnSave = (Button)d.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {

          public void onClick(final View v) {
            final EditText et = (EditText)d.findViewById(R.id.txtParcour);
            final Parcour p = new Parcour();
            p.setName(et.getText().toString());
            getHelper().getParcourDao().create(p);
            d.dismiss();
            fillParcours();
          }
        });
        d.show();
      }
    });

    final ListView lv = (ListView)findViewById(R.id.lvUsers);
    lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      public void onItemClick(final AdapterView<?> arg0, final View v, final int i, final long l) {
        final User user = (User)lv.getAdapter().getItem(i);
        v.setBackgroundColor(0xFFAAEEAA);
      }
    });

    final Button btnStart = (Button)findViewById(R.id.btnStart);
    btnStart.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        Toast.makeText(instance, "Item Clicked " + lv.getCheckedItemPositions().size(), Toast.LENGTH_SHORT).show();
      }
    });

    fillParcours();
    fillUsers();
  }


  private void fillUsers() {
    final RuntimeExceptionDao<User, Long> userDao = getHelper().getUserDao();
    final List<User> users = userDao.queryForAll();
    final ListView listView = (ListView)findViewById(R.id.lvUsers);
    final ArrayAdapter<User> adapter = new UserAdapter(this, R.layout.user_row, users);
    // final ArrayAdapter<User> adapter =
    // new ArrayAdapter<User>(this, android.R.layout.simple_list_item_multiple_choice, users);
    listView.setAdapter(adapter);
  }


  @Override
  protected void onResume() {
    super.onResume();
    fillParcours();
    fillUsers();
  }


  private void fillParcours() {
    final List<Parcour> parcours = getHelper().getParcourDao().queryForAll();
    final Spinner sp = (Spinner)findViewById(R.id.spParcours);
    final ArrayAdapter<Parcour> adapter =
        new ArrayAdapter<Parcour>(this, android.R.layout.simple_spinner_dropdown_item, parcours);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp.setAdapter(adapter);
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

      final View view = v.findViewById(R.id.dummyview_bg);
      view.setBackgroundColor(currentUser.getRgbColor());
      view.invalidate();

      return v;
    }
  }
}
