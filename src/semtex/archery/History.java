
package semtex.archery;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import semtex.archery.business.SharingDispatcher;
import semtex.archery.business.interfaces.CallbackAdapter;
import semtex.archery.data.DatabaseHelper;
import semtex.archery.data.ReportGenerator;
import semtex.archery.data.entities.UserVisit;
import semtex.archery.data.entities.Visit;
import semtex.archery.data.reports.ParcourReportData;
import semtex.archery.util.BackupRestoreHelper;
import semtex.archery.util.ProcessUtils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;


public class History extends OrmLiteBaseListActivity<DatabaseHelper> {

  private static final int REQ_CODE_RET_FROM_SCORING = 0;

  private static final int CTX_REMOVE_ITEM_ID = 1;

  private static final int CTX_SHARE = 2;

  private static final int CTX_SHARE_WEB = 3;

  private static final int CTX_REOPEN = 4;

  private static final String TAG = History.class.getName();

  public Map<Visit, ParcourReportData> reportCache = new HashMap<Visit, ParcourReportData>();

  public List<Visit> visitCache = new LinkedList<Visit>();

  private final DateFormat dateFormatter = DateFormat.getDateInstance();

  private ListView lv;

  private TextView txtCurrentStatus;

  private ProgressBar progressBarSearch;

  private ArrayAdapter<Visit> adapter;

  private ReportGenerator generator;

  private SharingDispatcher dispatcher;


  @Override
  public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    // if (v.getId() == R.id.lvVisitHistory) {
    menu.add(Menu.NONE, CTX_REOPEN, Menu.NONE, "Reopen");
    menu.add(Menu.NONE, CTX_REMOVE_ITEM_ID, Menu.NONE, "Remove");
    menu.add(Menu.NONE, CTX_SHARE, Menu.NONE, "Share");
    menu.add(Menu.NONE, CTX_SHARE_WEB, Menu.NONE, "Share with Web");
    // }
  }


  @Override
  public boolean onContextItemSelected(final MenuItem item) {
    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    final Visit visit = adapter.getItem(info.position);

    if (item.getItemId() == CTX_REMOVE_ITEM_ID) {

      final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      alertDialog.setTitle("Remove Visit");
      alertDialog.setMessage("Are you sure?");
      alertDialog.setPositiveButton("Remove", new DialogInterface.OnClickListener() {

        public void onClick(final DialogInterface dialog, final int which) {
          for (final UserVisit uv : visit.getUserVisit()) {
            getHelper().getTargetHitDao().deleteTargetHitsFromUserVisit(uv);
            getHelper().getUserVisitDao().delete(uv);
          }

          getHelper().getVisitDao().delete(visit);
          refreshVisitList();
          Toast.makeText(getApplicationContext(), "Disposed " + visit.getId(), Toast.LENGTH_SHORT).show();
        } // onClick
      }); // setPositiveButton

      alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

        public void onClick(final DialogInterface dialog, final int which) {
        } // onClick
      }); // setNegativeButton

      alertDialog.show();

    } else if (item.getItemId() == CTX_SHARE) {
      dispatcher = new SharingDispatcher(getHelper(), visit);
      final Intent sharingIntent = dispatcher.shareMail();
      startActivity(Intent.createChooser(sharingIntent, "Share using"));
    } else if (item.getItemId() == CTX_SHARE_WEB) { // else if
      dispatcher = new SharingDispatcher(getHelper(), visit);
      dispatcher.shareServer(new CallbackAdapter<Void>() {

        @Override
        public void onSuccess(final Void data) {
          Toast.makeText(getApplicationContext(), "Data uploaded successfully!", Toast.LENGTH_LONG).show();
        }


        @Override
        public void onFailure(final Throwable tr) {
          Toast.makeText(getApplicationContext(), "Could not upload data to server!", Toast.LENGTH_LONG).show();
          Log.e(TAG, "Could not upload to server!", tr);
        }
      });
    } else if (item.getItemId() == CTX_REOPEN) { // else if
      if (getHelper().getVisitDao().findLastOpenVisit() != null) {
        Toast.makeText(getApplicationContext(), "close the current open visit first!", Toast.LENGTH_LONG).show();
      } else {
        visit.setEndTime(null);
        getHelper().getVisitDao().update(visit);
        final Intent intent = new Intent(getBaseContext(), StartParcour.class);
        startActivityForResult(intent, REQ_CODE_RET_FROM_SCORING);
      } // else
    } // else if
    return true;
  }


  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    if (requestCode == REQ_CODE_RET_FROM_SCORING) {
      refreshVisitList();
    } // if
  } // onActivityResult


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.history_visit);
    generator = new ReportGenerator(History.this.getHelper());

    txtCurrentStatus = (TextView)findViewById(R.id.txtCurrentStatus);
    progressBarSearch = (ProgressBar)findViewById(R.id.progressBarSearch);

    lv = getListView();
    lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    lv.setItemsCanFocus(true);
    lv.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(final AdapterView<?> parentView, final View childView, final int position, final long id) {
        final Visit v = (Visit)lv.getItemAtPosition(position);

        final Intent i = new Intent(getApplicationContext(), Scoreboard.class);
        i.putExtra("visit_id", new ParcelUuid(v.getId()));

        startActivity(i);
      } // open parcour
    }); // setOnItemClickListener
    registerForContextMenu(lv);
    refreshVisitList();
  } // onCreate


  @Override
  protected void onNewIntent(final Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      final String query = intent.getStringExtra(SearchManager.QUERY);
      if (visitCache == null) {
        visitCache = getHelper().getVisitDao().findAllVisits(false, 0L);
      } // if
      final List<Visit> filteredVisits = performFiltering(visitCache, query);
      adapter = new VisitHistoryAdapter(History.this, R.layout.history_visit_row, filteredVisits);
      setListAdapter(adapter);
    } // if
  } // onNewIntent


  protected List<Visit> performFiltering(final Collection<Visit> originalCollection, final CharSequence constraint) {
    final String comp = constraint.toString().toLowerCase();

    final ArrayList<Visit> filtered = new ArrayList<Visit>();

    for (final Visit v : originalCollection) {
      if (v.getVersion().getParcour().getName().toLowerCase().contains(comp)) {
        filtered.add(v);
        continue;
      } // for - parcour name

      for (final UserVisit uv : v.getUserVisit()) {
        if (uv.getUser().getUserName().toLowerCase().contains(comp)) {
          filtered.add(v);
          break;
        } // if
      } // for - each userVisit
    } // for each visit
    return filtered;
  } // performFiltering


  @SuppressLint("NewApi")
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    final MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.options_menu, menu);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      final SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
      final SearchView searchView = (SearchView)menu.findItem(R.id.search).getActionView();
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
      searchView.setIconifiedByDefault(false);
    } // if
    return true;
  } // onCreateOptionsMenu


  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.search:
        onSearchRequested();
        return true;
      case R.id.importdb:
        importDBfromSD();
        return true;
      case R.id.exportdb:
        exportDbToSDCard();
        return true;
      default:
        return false;
    }
  }


  private void exportDbToSDCard() {
    if (!BackupRestoreHelper.backupDB()) {
      Toast.makeText(getApplicationContext(), "Backup to SD Card failed!", Toast.LENGTH_LONG).show();
    } else { // if
      Toast.makeText(getApplicationContext(), "Backup successfull", Toast.LENGTH_LONG).show();
    }
  } // exportDbToSDScard


  private void importDBfromSD() {
    if (!BackupRestoreHelper.restoreDB()) {
      Toast.makeText(getApplicationContext(), "Restored to internal storage failed!", Toast.LENGTH_LONG).show();
    } else { // if
      Toast.makeText(getApplicationContext(), "Restore successfull, application will restart instantly",
          Toast.LENGTH_LONG).show();
      ProcessUtils.getInstance().killTasWithTimeout(3, getApplicationContext());
    }
  } // importDBFromSDCard


  private void refreshVisitList() {
    final AsyncTask<Void, Void, List<Visit>> task = new AsyncTask<Void, Void, List<Visit>>() {

      @Override
      protected void onPreExecute() {
        txtCurrentStatus.setVisibility(View.VISIBLE);
        txtCurrentStatus.setText("Loading Results...");
        progressBarSearch.setVisibility(View.VISIBLE);
        lv.setVisibility(View.GONE);
      }


      @Override
      protected List<Visit> doInBackground(final Void... params) {
        return getHelper().getVisitDao().findAllVisits(false, 0L);
      }


      @Override
      protected void onPostExecute(final List<Visit> result) {
        if (result == null || result.size() == 0) {
          visitCache = new LinkedList<Visit>();
          txtCurrentStatus.setText("No results found!");
          progressBarSearch.setVisibility(View.GONE);
        } else {
          visitCache = result;
          txtCurrentStatus.setVisibility(View.GONE);
          progressBarSearch.setVisibility(View.GONE);
          lv.setVisibility(View.VISIBLE);
          adapter = new VisitHistoryAdapter(History.this, R.layout.history_visit_row, result);
          lv.setAdapter(adapter);
        }
      }

    };
    task.execute((Void)null);
  } // refreshVisitList

  public class VisitHistoryAdapter extends ArrayAdapter<Visit> {

    private static final String USER_TEXTVIEW = "scoring line";

    public final String TAG = VisitHistoryAdapter.class.getName();

    private final DateFormat dateFormatter = DateFormat.getDateInstance();

    private final DateFormat dateTimeFormatter = new SimpleDateFormat();

    final List<Visit> objects;


    public VisitHistoryAdapter(final Context context, final int textViewResourceId, final List<Visit> objects) {
      super(context, textViewResourceId, objects);
      this.objects = objects;
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
      View v = convertView;

      final Visit visit = getItem(position);

      if (v == null) {
        final LayoutInflater li = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        v = li.inflate(R.layout.history_visit_row, null);
      } // if

      ParcourReportData reportData = reportCache.get(visit);
      if (reportData == null) {
        reportData = generator.generateReportForVisit(visit);
        reportCache.put(visit, reportData);
      } // reportData

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

      final Map<String, Double> avgPointsMap = reportData.getAvgPoints();
      final Map<String, Integer> totalPointsMap = reportData.getTotalPoints();

      for (final Map.Entry<String, Double> entries : avgPointsMap.entrySet()) {
        final LinearLayout.LayoutParams lp =
            new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(30, 0, 0, 0);

        final Integer totalPoints = totalPointsMap.get(entries.getKey());

        final TextView tv = new TextView(v.getContext());
        tv.setId(View.NO_ID);
        tv.setTag(USER_TEXTVIEW);
        tv.setText(entries.getKey() + " - "
            + (totalPoints != null ? MessageFormat.format("{0,number,#}", totalPoints) : "-") + " - avg "
            + (entries.getValue() != null ? MessageFormat.format("{0,number,#.##}", entries.getValue()) : "-"));

        ll.addView(tv, lp);
      } // for

      return v;
    } // getView

  } // VisitHistoryAdapter
}
