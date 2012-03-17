
package semtex.archery;

import android.os.Bundle;
import android.widget.TableLayout;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class Scoreboard extends OrmLiteBaseActivity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scoreboard);

    final TableLayout tl = (TableLayout)findViewById(R.id.tblScoring);

    // TableRow = new TableRow(this);
  }
}
