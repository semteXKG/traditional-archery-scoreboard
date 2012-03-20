
package semtex.archery;

import java.text.MessageFormat;
import java.util.Map;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.ReportGenerator;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;
import semtex.archery.entities.data.reports.ParcourReportData;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class Scoreboard extends OrmLiteBaseActivity<DatabaseHelper> {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scoreboard);
    final Intent intent = getIntent();
    if (intent == null) {
      return;
    }

    final Long visit = intent.getLongExtra("visit_id", -1);
    if (visit == -1) {
      return;
    }

    final Visit v = getHelper().getVisitDao().queryForId(visit);

    final ReportGenerator generator = new ReportGenerator(getHelper());
    final ParcourReportData reportData = generator.generateReportForVisit(v);

    final TextView name = (TextView)findViewById(R.id.lblParcourName);
    final TextView date = (TextView)findViewById(R.id.lblParcourRevisionDate);

    name.setText(reportData.getParcourName());
    date.setText(reportData.getParcourRevisionDate().toLocaleString());

    final TableLayout tl = (TableLayout)findViewById(R.id.tblScoring);

    TableRow tr = new TableRow(this);

    TextView tv = new TextView(this);
    tv.setText("");

    tr.addView(tv, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
        android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

    for (final UserVisit uv : v.getUserVisit()) {
      tv = new TextView(this);
      tv.setText(uv.getUser().getUserName());

      tr.addView(tv, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
          android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    tl.addView(tr);
    final Map<Integer, Map<String, Double>> scoringData = reportData.getScoringData();

    for (final Map.Entry<Integer, Map<String, Double>> entry : scoringData.entrySet()) {
      tr = new TableRow(this);

      tv = new TextView(this);
      tv.setText(entry.getKey().toString());
      tr.addView(tv, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
          android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

      for (final UserVisit uv : v.getUserVisit()) {
        tv = new TextView(this);
        final Double value = entry.getValue().get(uv.getUser().getUserName());
        tv.setText(value != null ? MessageFormat.format("{0,number,#.##}", value) : "-");

        tr.addView(tv, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
      }
      tl.addView(tr);
    }

  }
}
