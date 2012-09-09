
package semtex.archery;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class ArcheryPreferences extends PreferenceActivity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
}
