
package semtex.archery.util;

import java.io.*;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;
import android.util.Log;


public class BasicGoogleBackupAgent extends BackupAgent {

  private final String ARCHERY_BACKUP_KEY = "tas_database_dumps";

  public final String TAG = BasicGoogleBackupAgent.class.getName();


  @Override
  public void onBackup(final ParcelFileDescriptor oldState, final BackupDataOutput data,
      final ParcelFileDescriptor newState) throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final FileInputStream fis = new FileInputStream(getDatabasePath("tac.db"));
    final byte[] buffer = new byte[1024];
    int len = 0;
    while ((len = fis.read(buffer)) != -1) {
      baos.write(buffer, 0, len);
    } // while

    fis.close();

    final byte[] result = baos.toByteArray();
    data.writeEntityHeader(ARCHERY_BACKUP_KEY, result.length);
    data.writeEntityData(result, result.length);
    Log.i(TAG, "Sent " + result.length + " bytes to backup");
  } // onBackup


  @Override
  public void onRestore(final BackupDataInput data, final int appVersionCode, final ParcelFileDescriptor newState)
      throws IOException {
    while (data.readNextHeader()) {
      if (data.getKey().equals(ARCHERY_BACKUP_KEY)) {
        Log.i(TAG, "Writing back " + data.getDataSize() + " bytes");
        final byte[] content = new byte[data.getDataSize()];
        data.readEntityData(content, 0, data.getDataSize());

        final File directory = new File(BackupRestoreHelper.DB_LOCATION_INTERN_PATH);
        directory.mkdirs();

        final FileOutputStream fos = new FileOutputStream(BackupRestoreHelper.DB_LOCATION_INTERN);
        fos.write(content);
        fos.close();

        ProcessUtils.getInstance().killTasWithTimeout(10, getApplicationContext());
      }
    }
  }

}
