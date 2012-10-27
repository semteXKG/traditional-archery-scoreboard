
package semtex.archery;

import java.util.regex.Pattern;

import semtex.archery.data.DatabaseHelper;
import semtex.archery.data.entities.User;
import semtex.archery.dialogs.ColorPickerDialog;
import semtex.archery.dialogs.ColorPickerDialog.OnColorChangedListener;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class AddEditUser extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final String TAG = AddEditUser.class.getName();

  final int[] persColor = new int[1];

  private User currentUser = null;

  EditText username;

  EditText mailText;

  TextView txtInfoBox;

  ImageButton btnSaveUser;

  Pattern mailPattern = Patterns.EMAIL_ADDRESS;

  private final TextWatcher textWatcher = new TextWatcher() {

    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
    } // onTextChanged


    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
    } // beforeTextChanged


    public void afterTextChanged(final Editable s) {
      doValidationRun();
    } // afterTextChanged

  };


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.user_add_edit);

    username = (EditText)findViewById(R.id.txtUsername);
    username.addTextChangedListener(textWatcher);

    mailText = (EditText)findViewById(R.id.txtMail);

    mailText.addTextChangedListener(textWatcher);

    final View view = findViewById(R.id.dummyview_col);

    final ImageButton button = (ImageButton)findViewById(R.id.btnPicker);
    button.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final OnColorChangedListener listener = new OnColorChangedListener() {

          public void colorChanged(final int color) {
            persColor[0] = color;
            view.setBackgroundColor(color);
          } // colorChanged
        }; // onColorChangeListener

        final Dialog d = new ColorPickerDialog(v.getContext(), listener, Color.BLACK);
        d.show();
      } // onClick
    }); // setOnClickListener

    btnSaveUser = (ImageButton)findViewById(R.id.btnSaveUser);
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
        } else { // if
          getHelper().getUserDao().create(
              new User(username.getText().toString(), mailText.getText().toString(), persColor[0]));
        } // else
        setResult(RESULT_OK);
        finish();
      } // onClick
    }); // setOnClickListener

    final ImageButton btnCancelSave = (ImageButton)findViewById(R.id.btnCancelUser);
    btnCancelSave.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        setResult(RESULT_CANCELED);
        finish();
      }
    }); // setOnCLickListener
    prefillBoxes();

    doValidationRun();
  } // onCreate


  protected void doValidationRun() {
    if (username.getText() == null || "".equals(username.getText().toString())) {
      invalidUsername();
      return;
    } // if

    // check if there is either no mail address at all OR if the inserted is a valid mail address
    if (!mailText.getText().toString().equals("") && !mailPattern.matcher(mailText.getText()).matches()) {
      invalidMail();
      return;
    } // if
    btnSaveUser.setEnabled(true);
  } // doValidationRun


  protected void invalidMail() {
    mailText.setError(getString(R.string.empty_or_valid));
    btnSaveUser.setEnabled(false);
  } // invalidMail


  protected void invalidUsername() {
    username.setError(getString(R.string.required));
    btnSaveUser.setEnabled(false);
  } // invalidUsername


  private void prefillBoxes() {
    final Bundle extras = getIntent().getExtras();
    if (extras != null) {
      final long val = extras.getLong("userid", -1);
      if (val != -1) {
        currentUser = getHelper().getUserDao().queryForId(val);

        username = (EditText)findViewById(R.id.txtUsername);
        username.setText(currentUser.getUserName());

        mailText = (EditText)findViewById(R.id.txtMail);
        mailText.setText(currentUser.getMail());

        final View view = findViewById(R.id.dummyview_col);
        view.setBackgroundColor(currentUser.getRgbColor());
        persColor[0] = currentUser.getRgbColor();
      } // if
    } // if
  } // prefillBoxes

}
