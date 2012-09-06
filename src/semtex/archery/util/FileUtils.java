
package semtex.archery.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class FileUtils {

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
