
package semtex.archery;

import semtex.archery.dialogs.ColorPickerDialog;
import semtex.archery.dialogs.ColorPickerDialog.OnColorChangedListener;
import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.entities.User;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class AddEditUser extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final String TAG = AddEditUser.class.getName();

  final int[] persColor = new int[1];

  private User currentUser = null;


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.user_add_edit);

    final EditText username = (EditText)findViewById(R.id.txtUsername);

    final EditText mailText = (EditText)findViewById(R.id.txtMail);

    final View view = findViewById(R.id.dummyview_col);

    final Button button = (Button)findViewById(R.id.btnPicker);
    button.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final OnColorChangedListener listener = new OnColorChangedListener() {

          public void colorChanged(final int color) {
            persColor[0] = color;
            view.setBackgroundColor(color);
          }
        };
        final Dialog d = new ColorPickerDialog(v.getContext(), listener, Color.BLACK);
        d.show();
      }
    });

    final Button btnSaveUser = (Button)findViewById(R.id.btnSaveUser);
    btnSaveUser.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        Log.i(TAG, "Creating user with: " + username.getText() + ", mail: " + mailText.getText() + " and color: "
            + persColor[0]);
        if (currentUser != null) {
          currentUser.setUserName(username.getText().toString());
          currentUser.setRgbColor(persColor[0]);
          currentUser.setMail(mailText.getText().toString());
          getHelper().getUserDao().update(currentUser);
          currentUser = null;
        } else {
          getHelper().getUserDao().create(
              new User(username.getText().toString(), mailText.getText().toString(), persColor[0]));
        }
        setResult(RESULT_OK);
        finish();
      }

    });

    final Button btnCancelSave = (Button)findViewById(R.id.btnCancelUser);
    btnCancelSave.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });

    prefillBoxes();
  }


  private void prefillBoxes() {
    final Bundle extras = getIntent().getExtras();
    if (extras != null) {
      final long val = extras.getLong("userid", -1);
      if (val != -1) {
        currentUser = getHelper().getUserDao().queryForId(val);

        final EditText username = (EditText)findViewById(R.id.txtUsername);
        username.setText(currentUser.getUserName());

        final EditText mailText = (EditText)findViewById(R.id.txtMail);
        mailText.setText(currentUser.getMail());

        final View view = findViewById(R.id.dummyview_col);
        view.setBackgroundColor(currentUser.getRgbColor());
        persColor[0] = currentUser.getRgbColor();
      }
    }
  }

}
