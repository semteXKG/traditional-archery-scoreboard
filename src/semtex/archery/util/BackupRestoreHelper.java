
package semtex.archery.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.ExternalStorageManager;
import android.os.Environment;
import android.util.Log;


public class BackupRestoreHelper {

  public static final String TAG = BackupRestoreHelper.class.toString();

  public static final String DB_LOCATION_INTERN_PATH = "/data/data/semtex.archery/databases/";

  public static final File DB_LOCATION_INTERN = new File(DB_LOCATION_INTERN_PATH + DatabaseHelper.DATABASE_NAME);

  public static final File DB_LOCATION_SDCARD = new File(ExternalStorageManager.getApplicationPath() + "/"
      + DatabaseHelper.DATABASE_NAME);


  public static boolean backupDB() {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      return false;
    }
    try {
      ExternalStorageManager.getApplicationPath().mkdirs();
      FileUtils.copyFile(new FileInputStream(DB_LOCATION_INTERN), new FileOutputStream(DB_LOCATION_SDCARD));
    } catch(final Exception e) {
      Log.e(TAG, "backup not successfull", e);
      return false;
    }
    return true;
  } // backupDB


  public static boolean restoreDB() {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !DB_LOCATION_SDCARD.exists()) {
      return false;
    }
    try {
      new File(DB_LOCATION_INTERN_PATH).mkdirs();
      FileUtils.copyFile(new FileInputStream(DB_LOCATION_SDCARD), new FileOutputStream(DB_LOCATION_INTERN));
    } catch(final Exception e) {
      Log.e(TAG, "restore not successfull", e);
      return false;
    } // catch
    return true;
  } // restoreDB

}
