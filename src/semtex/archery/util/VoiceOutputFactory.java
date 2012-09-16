
package semtex.archery.util;

import java.util.Locale;


public class VoiceOutputFactory {

  public static String targetSkipped(final Locale locale) {
    if (locale == Locale.GERMAN) {
      return "ausgelassen";
    } // if
    return "skipped";
  } // targetSkipped
}
