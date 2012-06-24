
package semtex.archery;

import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.ReportGenerator;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;
import semtex.archery.entities.data.reports.ParcourReportData;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class Scoreboard extends OrmLiteBaseActivity<DatabaseHelper> {

  private final class ScoringComparator implements Comparator<Entry<Integer, Map<String, Integer>>> {

    public int compare(final Entry<Integer, Map<String, Integer>> lhs, final Entry<Integer, Map<String, Integer>> rhs) {
      if (lhs == null) {
        return -1;
      }
      if (rhs == null) {
        return 1;
      }

      return lhs.getKey().compareTo(rhs.getKey());
    }
  }

  public static final int COLOR_1 = 0xFF696969;

  public static final int COLOR_2 = 0xFF808080;


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

    final TableRow tr = new TableRow(this);

    TextView tv = new TextView(this);
    tv.setText("");
    tv.setTypeface(null, Typeface.BOLD);
    tv.setBackgroundColor(COLOR_1);

    tr.addView(tv, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));

    for (final UserVisit uv : v.getUserVisit()) {
      tv = new TextView(this);
      tv.setText(uv.getUser().getUserName());
      tv.setTypeface(null, Typeface.BOLD);
      tv.setBackgroundColor(COLOR_1);
      tr.addView(tv, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));
    }

    tl.addView(tr);
    final Map<Integer, Map<String, Integer>> scoringData = reportData.getScoringData();

    final Set<Entry<Integer, Map<String, Integer>>> entrySet = scoringData.entrySet();
    final TreeSet<Entry<Integer, Map<String, Integer>>> sortedSet =
        new TreeSet<Map.Entry<Integer, Map<String, Integer>>>(new ScoringComparator());

    sortedSet.addAll(entrySet);
    ArrayList<Double> values = new ArrayList<Double>();
    for (final UserVisit uv : v.getUserVisit()) {
      values.add(reportData.getAvgPoints().get(uv.getUser().getUserName()));
    }
    addLineToTable(tl, "average", values, true);

    values = new ArrayList<Double>();
    for (final UserVisit uv : v.getUserVisit()) {
      values.add(reportData.getTotalPoints().get(uv.getUser().getUserName()) * 1.0);
    }
    addLineToTable(tl, "total", values, true);

    for (final Map.Entry<Integer, Map<String, Integer>> entry : sortedSet) {
      values = new ArrayList<Double>();
      for (final UserVisit uv : v.getUserVisit()) {
        values.add(entry.getValue().get(uv.getUser().getUserName()) * 1.0);
      }
      addLineToTable(tl, entry.getKey().toString(), values, entry.getKey() % 2 == 0);
    }

    final Button btnVisitClose = (Button)findViewById(R.id.btnVisitClose);

    // don't show button if visit is closed
    if (v.getEndTime() != null) {
      btnVisitClose.setVisibility(View.GONE);
    }

    btnVisitClose.setOnClickListener(new View.OnClickListener() {

      public void onClick(final View view) {
        v.setEndTime(new Date());
        getHelper().getVisitDao().update(v);

        Toast.makeText(view.getContext(), "Visit successfully ended!", Toast.LENGTH_SHORT).show();

        setResult(RESULT_OK);
        finish();
      }
    });

  }


  private void addLineToTable(final TableLayout tl, final String key, final ArrayList<Double> values,
      final boolean isOdd) {
    final TableRow tr = new TableRow(this);
    final int col = isOdd ? COLOR_2 : COLOR_1;
    TextView tv = new TextView(this);
    tv.setText(key);
    tv.setTypeface(null, Typeface.BOLD);
    tv.setBackgroundColor(col);
    tr.addView(tv, new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0.2f));

    for (final Double value : values) {
      tv = new TextView(this);
      tv.setText(value != null ? MessageFormat.format("{0,number,#.##}", value) : "-");
      tv.setBackgroundColor(col);
      tr.addView(tv, new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.2f));
    }
    tl.addView(tr);
  }
}
