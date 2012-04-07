
package semtex.archery;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.ReportGenerator;
import semtex.archery.entities.data.entities.Visit;
import semtex.archery.entities.data.reports.ParcourReportData;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class History extends OrmLiteBaseActivity<DatabaseHelper> {

  public Map<Visit, ParcourReportData> reportCache = new HashMap<Visit, ParcourReportData>();


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.history_visit);

    final ListView lv = (ListView)findViewById(R.id.lvVisitHistory);
    final List<Visit> visits = getHelper().getVisitDao().findAllVisits(false, 0L);

    final ArrayAdapter<Visit> adapter = new VisitHistoryAdapter(this, R.layout.history_visit_row, visits);
    lv.setAdapter(adapter);

  }

  public class VisitHistoryAdapter extends ArrayAdapter<Visit> {

    public final String TAG = VisitHistoryAdapter.class.getName();

    private final ReportGenerator generator = new ReportGenerator(History.this.getHelper());

    private final DateFormat dateFormatter = DateFormat.getDateInstance();

    private final DateFormat dateTimeFormatter = new SimpleDateFormat();


    public VisitHistoryAdapter(final Context context, final int textViewResourceId, final List<Visit> objects) {
      super(context, textViewResourceId, objects);
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      View v = convertView;

      final Visit visit = getItem(position);

      if (v == null) {
        final LayoutInflater li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        v = li.inflate(R.layout.history_visit_row, null);
      }

      ParcourReportData reportData = reportCache.get(visit);
      if (reportData == null) {
        reportData = generator.generateReportForVisit(visit);
        reportCache.put(visit, reportData);
      }

      final String beginTime = visit.getBeginTime() != null ? dateTimeFormatter.format(visit.getBeginTime()) : "";
      final String endTime = visit.getEndTime() != null ? dateTimeFormatter.format(visit.getEndTime()) : "";
      final String revision =
          reportData.getParcourRevisionDate() != null ? dateFormatter.format(reportData.getParcourRevisionDate()) : "";

      final TextView txtParcourName = (TextView)v.findViewById(R.id.lblParcourName);
      txtParcourName.setText(reportData.getParcourName());
      final TextView txtParcourVersion = (TextView)v.findViewById(R.id.lblVersionDate);
      txtParcourVersion.setText(beginTime + " - " + endTime);
      final TextView txtRevisionNumber = (TextView)v.findViewById(R.id.lblRevision);
      txtRevisionNumber.setText(revision);

      final LinearLayout ll = (LinearLayout)v.findViewById(R.id.historyVisitLinearLayout);

      final Map<Integer, Map<String, Double>> data = reportData.getScoringData();
      final Map<String, Double> avgPointsMap = data.get(0);
      if (ll.getChildCount() < 4) {
        for (final Map.Entry<String, Double> entries : avgPointsMap.entrySet()) {

          final LinearLayout.LayoutParams lp =
              new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
                  android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
          lp.setMargins(30, 0, 0, 0);

          final TextView tv = new TextView(v.getContext());
          tv.setTag(visit.getId() + " " + entries.getKey());
          tv.setText(entries.getKey() + " - "
              + (entries.getValue() != null ? MessageFormat.format("{0,number,#.##}", entries.getValue()) : "-"));

          ll.addView(tv, lp);
        }
      }
      return v;
    }
  }

}
