
package semtex.archery.util;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;


public class ProcessUtils {

  private final static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

  private static ProcessUtils instance;

  public final class RestartRunnable implements Runnable {

    private final Context context;


    public RestartRunnable(final Context context) {
      this.context = context;
    } // RestartRunnable


    public void run() {
      killTas();
    } // run


    private void killTas() {
      final ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
      final List<RunningAppProcessInfo> activityes = manager.getRunningAppProcesses();
      for (final RunningAppProcessInfo runningAppProcessInfo : activityes) {
        if (runningAppProcessInfo.processName.equals("semtex.archery")) {
          android.os.Process.killProcess(runningAppProcessInfo.pid);
        }
      }
    } // killTAS
  } // RestartRunnable


  public void killTasWithTimeout(final int delay, final Context context) {
    executorService.schedule(new RestartRunnable(context), delay, TimeUnit.SECONDS);
  } // killTasWithTimeout


  private ProcessUtils() {
  } // ProcessUtils


  public static ProcessUtils getInstance() {
    if (instance == null) {
      instance = new ProcessUtils();
    } // if
    return instance;
  } // getInstance

}
