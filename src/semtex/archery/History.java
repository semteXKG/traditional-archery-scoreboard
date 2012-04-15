
package semtex.archery;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.ReportGenerator;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;
import semtex.archery.entities.data.reports.ParcourReportData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class History extends OrmLiteBaseActivity<DatabaseHelper> {

  private static final int CTX_REMOVE_ITEM_ID = 1;

  private static final String TAG = History.class.getName();

  public Map<Visit, ParcourReportData> reportCache = new HashMap<Visit, ParcourReportData>();

  private ListView lv;

  private ArrayAdapter<Visit> adapter;


  @Override
  public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (v.getId() == R.id.lvVisitHistory) {
      menu.add(Menu.NONE, CTX_REMOVE_ITEM_ID, Menu.NONE, "Remove");
    }
  }


  @Override
  public boolean onContextItemSelected(final MenuItem item) {
    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    if (item.getItemId() == CTX_REMOVE_ITEM_ID) {
      final Visit visit = adapter.getItem(info.position);
      Toast.makeText(getApplicationContext(), "Disposing " + visit.getId(), Toast.LENGTH_SHORT).show();

      for (final UserVisit uv : visit.getUserVisit()) {
        int disposed = getHelper().getTargetHitDao().deleteTargetHitsFromUserVisit(uv);
        disposed = getHelper().getUserVisitDao().delete(uv);
      }
      getHelper().getVisitDao().delete(visit);
      refreshVisitList();
    }
    return true;
  }


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.history_visit);

    lv = (ListView)findViewById(R.id.lvVisitHistory);
    lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    lv.setItemsCanFocus(true);
    lv.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(final AdapterView<?> parentView, final View childView, final int position, final long id) {
        final Visit v = (Visit)lv.getItemAtPosition(position);

        final Intent i = new Intent(getApplicationContext(), Scoreboard.class);
        i.putExtra("visit_id", v.getId());

        startActivity(i);
      }
    });

    registerForContextMenu(lv);
    refreshVisitList();
  }


  private void refreshVisitList() {
    final List<Visit> visits = getHelper().getVisitDao().findAllVisits(false, 0L);
    adapter = new VisitHistoryAdapter(this, R.layout.history_visit_row, visits);
    lv.setAdapter(adapter);
  }

  public class VisitHistoryAdapter extends ArrayAdapter<Visit> {

    private static final String USER_TEXTVIEW = "scoring line";

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

      final LinearLayout ll = (LinearLayout)v.findViewById(R.id.llUserScores);
      ll.removeAllViews();

      final Map<Integer, Map<String, Double>> data = reportData.getScoringData();

      final Map<String, Double> avgPointsMap = data.get(0);
      final Map<String, Double> totalPointsMap = data.get(-1);

      for (final Map.Entry<String, Double> entries : avgPointsMap.entrySet()) {
        final LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(30, 0, 0, 0);

        final Double totalPoints = totalPointsMap.get(entries.getKey());

        final TextView tv = new TextView(v.getContext());
        tv.setId(View.NO_ID);
        tv.setTag(USER_TEXTVIEW);
        tv.setText(entries.getKey() + " - "
            + (totalPoints != null ? MessageFormat.format("{0,number,#}", totalPoints) : "-") + " - avg "
            + (entries.getValue() != null ? MessageFormat.format("{0,number,#.##}", entries.getValue()) : "-"));

        ll.addView(tv, lp);
      }

      return v;
    }
  }

}
