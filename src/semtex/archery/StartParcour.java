
package semtex.archery;

import java.util.*;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.entities.*;
import android.app.Dialog;
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


/**
 * @author semteX
 * 
 */
public class StartParcour extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final String TAG = String.class.getName();

  private final Set<User> selectedUsers = new HashSet<User>();


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
            p.setCreated(new Date());
            getHelper().getParcourDao().create(p);
            d.dismiss();
            fillParcours();
          }
        });
        d.show();
      }
    });

    fillUsers();

    final ListView lv = (ListView)findViewById(R.id.lvUsersAddToParcour);

    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      public void onItemClick(final AdapterView<?> av, final View v, final int pos, final long id) {
        final User user = (User)lv.getAdapter().getItem(pos);
        final CheckBox checkBox = (CheckBox)v.findViewById(R.id.ckbUserSelected);
        if (checkBox.isChecked()) {
          selectedUsers.remove(user);
          checkBox.setChecked(false);
        } else {
          selectedUsers.add(user);
          checkBox.setChecked(true);
        }

      }

    });

    lv.setItemsCanFocus(false);
    lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    final Button btnStart = (Button)findViewById(R.id.btnStart);
    btnStart.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final Spinner sp = (Spinner)findViewById(R.id.spParcours);
        startNewParcourRound((Parcour)sp.getSelectedItem());
      }
    });

    fillParcours();
  }


  protected void startNewParcourRound(final Parcour parcour) {

    if (parcour == null) {
      Toast.makeText(getApplicationContext(), "Please select a parcour first!", Toast.LENGTH_SHORT).show();
      return;
    }

    if (selectedUsers.size() == 0) {
      Toast.makeText(getApplicationContext(), "Select at least one participant!", Toast.LENGTH_SHORT).show();
      return;
    }

    // Create new variant
    final Version vTmp = new Version(UUID.randomUUID().toString(), parcour);
    getHelper().getVersionDao().create(vTmp);

    // Insert the first target
    final Target target = new Target(1, vTmp);
    getHelper().getTargetDao().create(target);

    // now let's create a visit
    final Visit visit = new Visit(new Date(), vTmp);
    getHelper().getVisitDao().create(visit);

    // now let's add our fellow friends
    for (final User user : selectedUsers) {
      final UserVisit uv = new UserVisit(user, visit);
      getHelper().getUserVisitDao().create(uv);
    }

    final Intent i = new Intent(getApplicationContext(), Scoring.class);
    startActivity(i);
    finish();
  }


  @Override
  protected void onResume() {
    super.onResume();
    fillParcours();
    fillUsers();
  }


  private void fillUsers() {
    final RuntimeExceptionDao<User, Long> userDao = getHelper().getUserDao();
    final List<User> users = userDao.queryForAll();
    final ListView listView = (ListView)findViewById(R.id.lvUsersAddToParcour);
    final ArrayAdapter<User> adapter = new UserAdapter(this, R.layout.user_selection_row, users);
    listView.setAdapter(adapter);
    selectedUsers.clear();
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
        v = li.inflate(R.layout.user_selection_row, null);
      }

      final TextView userName = (TextView)v.findViewById(R.id.txtUsername);
      userName.setText(currentUser.getUserName());

      final TextView mail = (TextView)v.findViewById(R.id.txtMail);
      mail.setText(currentUser.getMail());

      final GradientDrawable gd =
          new GradientDrawable(Orientation.RIGHT_LEFT, new int[] { currentUser.getRgbColor() & 0x77FFFFFF, 0x0 });
      v.setBackgroundDrawable(gd);
      return v;
    }
  }
}
