
package semtex.archery.util;

import java.io.FileInputStream;

import android.app.backup.BackupManager;
import android.content.Context;
import android.util.Log;


public class ArcheryBackupManager extends BackupManager {

  private static final String TAG = ArcheryBackupManager.class.getName();

  public static final String BACKUP_LOCATION = "tac.db.backup";

  private Context context = null;


  public ArcheryBackupManager(final Context context) {
    super(context);
    this.context = context;
  }


  @Override
  public void dataChanged() {
    // Copy Database to temp location
    try {
      FileUtils.copyFile(new FileInputStream(context.getDatabasePath("tac.db")),
          context.openFileOutput(BACKUP_LOCATION, Context.MODE_PRIVATE));
    } catch(final Exception e) {
      Log.e(TAG, "Could not copy database to backup location", e);
    }
    super.dataChanged();
  } // dataChanged

}
