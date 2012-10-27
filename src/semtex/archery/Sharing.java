
package semtex.archery;

import semtex.archery.business.SharingDispatcher;
import semtex.archery.business.interfaces.ICallback;
import semtex.archery.data.DatabaseHelper;
import semtex.archery.data.entities.Visit;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class Sharing extends OrmLiteBaseActivity<DatabaseHelper> {

  private Visit visit;

  private static final int MAIL_REQUEST_CODE = 17;

  private SharingDispatcher dispatcher;

  private Button shareServer;

  private Button shareMail;

  private final View.OnClickListener shareServerClickListener = new View.OnClickListener() {

    public void onClick(final View v) {

      dispatcher.shareServer(new ICallback<Void>() {

        public void onSuccess(final Void data) {
          Toast.makeText(getApplicationContext(), "Upload succeeded!", Toast.LENGTH_LONG).show();
          shareServer.setVisibility(View.INVISIBLE);
        }


        public void onFailure(final Throwable tr) {
          Toast.makeText(getApplicationContext(), "Operation failed!", Toast.LENGTH_LONG).show();
          shareServer.setEnabled(true);
        }


        public void inProgress() {
          shareServer.setEnabled(false);
        }
      });
    } // onClick
  };


  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // if (requestCode == MAIL_REQUEST_CODE) {
    // if (resultCode == Activity.RESULT_OK) {
    // Toast.makeText(getApplicationContext(), "Mail successfully sent", Toast.LENGTH_LONG).show();
    // shareMail.setVisibility(View.INVISIBLE);
    // } else {
    // Toast.makeText(getApplicationContext(), "Mail not sent", Toast.LENGTH_LONG).show();
    // shareMail.setEnabled(true);
    // }
    // }
    shareMail.setVisibility(View.INVISIBLE);
  }


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sharing);

    final Intent intent = getIntent();
    final ParcelUuid visitUuid = intent.getParcelableExtra("visit_id");
    if (visitUuid != null) {
      visit = getHelper().getVisitDao().queryForId(visitUuid.getUuid());
    } else {
      finish();
      setResult(RESULT_OK);
    } // visit

    dispatcher = new SharingDispatcher(getHelper(), visit);

    final Button exitButton = (Button)findViewById(R.id.btnShareQuit);
    exitButton.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        finish();
        setResult(RESULT_OK);
      } // onClick
    });

    shareMail = (Button)findViewById(R.id.btnShareMail);
    shareMail.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View v) {
        final Intent mailIntent = dispatcher.shareMail();
        startActivityForResult(Intent.createChooser(mailIntent, "Share using"), MAIL_REQUEST_CODE);
      } // on Click
    }); // shareMail

    shareServer = (Button)findViewById(R.id.btnShareServer);
    shareServer.setOnClickListener(shareServerClickListener);
  } // onCreate
} // Sharing
