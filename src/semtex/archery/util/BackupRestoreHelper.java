
package semtex.archery.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.ExternalStorageManager;
import android.os.Environment;
import android.util.Log;


public class BackupRestoreHelper {

  public static final String TAG = BackupRestoreHelper.class.toString();

  public static final File DB_LOCATION_INTERN = new File("/data/data/semtex.archery/databases/"
      + DatabaseHelper.DATABASE_NAME);

  public static final File DB_LOCATION_SDCARD = new File(ExternalStorageManager.getApplicationPath() + "/"
      + DatabaseHelper.DATABASE_NAME);


  public static boolean backupDB() {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      return false;
    }
    try {
      ExternalStorageManager.getApplicationPath().mkdirs();
      copyFile(new FileInputStream(DB_LOCATION_INTERN), new FileOutputStream(DB_LOCATION_SDCARD));
    } catch(final Exception e) {
      Log.e(TAG, "backup not successfull", e);
      return false;
    }
    return true;
  } // backupDB


  public static boolean restoreDB() {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      return false;
    }
    try {
      DB_LOCATION_SDCARD.mkdirs();
      copyFile(new FileInputStream(DB_LOCATION_SDCARD), new FileOutputStream(DB_LOCATION_INTERN));
    } catch(final Exception e) {
      Log.e(TAG, "restore not successfull", e);
      return false;
    } // catch
    return true;
  } // restoreDB


  public static void copyFile(final FileInputStream fromFile, final FileOutputStream toFile) throws IOException {
    FileChannel fromChannel = null;
    FileChannel toChannel = null;
    try {
      fromChannel = fromFile.getChannel();
      toChannel = toFile.getChannel();
      fromChannel.transferTo(0, fromChannel.size(), toChannel);
    } finally {
      try {
        if (fromChannel != null) {
          fromChannel.close();
        }
      } finally {
        if (toChannel != null) {
          toChannel.close();
        }
      }
    }
  }

}
