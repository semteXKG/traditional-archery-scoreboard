
package semtex.archery.entities.data;

import java.io.File;

import android.os.Environment;


public class ExternalStorageManager {

  public static final String APP_FOLDER = "/TAS/";


  public static boolean isExternalStorageAvail() {
    return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
  }


  public static File getApplicationPath() {
    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      return null;
    }
    final File targetDir = new File(Environment.getExternalStorageDirectory(), APP_FOLDER);
    return targetDir;
  }
}
