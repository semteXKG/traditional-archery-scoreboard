
package semtex.archery;

import semtex.archery.entities.data.DatabaseHelper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class TacActivity extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final String TAG = TacActivity.class.getName();


  /** Called when the activity is first created. */
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    final Button btnUser = (Button)findViewById(R.id.btnUsermanager);
    btnUser.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        Log.i(TAG, "switching to user manager!");
        final Intent intent = new Intent(v.getContext(), UserManager.class);
        startActivity(intent);
      }
    });

    final Button btn = (Button)findViewById(R.id.btnStart);
    btn.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final Intent intent = new Intent(v.getContext(), StartParcour.class);
        startActivity(intent);
      }
    });

    final Button btnHistory = (Button)findViewById(R.id.btnHistory);
    btnHistory.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final Intent intent = new Intent(v.getContext(), History.class);
        startActivity(intent);
      }
    });

    final Button btnSettings = (Button)findViewById(R.id.btnSettings);
    btnSettings.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final Intent intent = new Intent(TacActivity.this, ArcheryPreferences.class);
        startActivity(intent);
      }
    });

  }
}