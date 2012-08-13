
package semtex.archery;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.ReportGenerator;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;
import semtex.archery.entities.data.reports.ParcourReportData;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;


public class History extends OrmLiteBaseActivity<DatabaseHelper> {

  private ReportGenerator generator;

  private static final int CTX_REMOVE_ITEM_ID = 1;

  private static final int CTX_SHARE = 2;

  private static final int CTX_SHARE_WEB = 3;

  private static final String TAG = History.class.getName();

  public Map<Visit, ParcourReportData> reportCache = new HashMap<Visit, ParcourReportData>();

  private final DateFormat dateFormatter = DateFormat.getDateInstance();

  private ListView lv;

  private TextView txtCurrentStatus;

  private ProgressBar progressBarSearch;

  private ArrayAdapter<Visit> adapter;


  @Override
  public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (v.getId() == R.id.lvVisitHistory) {
      menu.add(Menu.NONE, CTX_REMOVE_ITEM_ID, Menu.NONE, "Remove");
      menu.add(Menu.NONE, CTX_SHARE, Menu.NONE, "Share");
      menu.add(Menu.NONE, CTX_SHARE_WEB, Menu.NONE, "Share with Web");
    }
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
        }
      });

      alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

        public void onClick(final DialogInterface dialog, final int which) {
        }
      });

      alertDialog.show();

    } else if (item.getItemId() == CTX_SHARE) {
      final ArrayList<String> recipients = new ArrayList<String>();
      for (final UserVisit uv : visit.getUserVisit()) {
        if (uv.getUser().getMail() != null && !"".equals(uv.getUser().getMail())) {
          recipients.add(uv.getUser().getMail());
        }
      }
      File report = null;
      try {
        report = generator.generatePDFReportForVisit(visit);
      } catch(final Exception e) {
        e.printStackTrace();
      }
      Log.i(TAG, "Found " + recipients.size() + " recpients");

      final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
      sharingIntent.setType("text/html");
      sharingIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients.toArray(new String[recipients.size()]));
      sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Results from "
          + visit.getVersion().getParcour().getName() + " on " + dateFormatter.format(visit.getBeginTime()));
      sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
          Html.fromHtml(generator.generateHTMLReportForVisit(visit)));
      if (report != null) {
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(report));
      }
      startActivity(Intent.createChooser(sharingIntent, "Share using"));
    } else if (item.getItemId() == CTX_SHARE_WEB) {
      final List<String> generateJsonObjectsForVisit = generator.generateJsonObjectsForVisit(visit);
      for (final String output : generateJsonObjectsForVisit) {
        final HttpClient httpclient = new DefaultHttpClient();
        final HttpPost httppost = new HttpPost("http://shice.it/c/upload.php");
        final List<NameValuePair> pairs = new LinkedList<NameValuePair>();
        final NameValuePair nvp = new BasicNameValuePair("a", output);
        pairs.add(nvp);
        try {
          httppost.setEntity(new UrlEncodedFormEntity(pairs));
          final HttpResponse response = httpclient.execute(httppost);

          if (response.getStatusLine().getStatusCode() == 200) {
            Toast.makeText(getApplicationContext(), "Upload successfull", Toast.LENGTH_LONG).show();
          }

        } catch(final UnsupportedEncodingException e) {
          Log.e(TAG, "unsupp. encoding of: " + output, e);
          Toast.makeText(getApplicationContext(), "Could not upload data", Toast.LENGTH_LONG).show();
        } catch(final ClientProtocolException e) {
          Log.e(TAG, "Client Protocol Exception", e);
          Toast.makeText(getApplicationContext(), "Could not upload data", Toast.LENGTH_LONG).show();
        } catch(final IOException e) {
          Log.e(TAG, "IO Exception");
          Toast.makeText(getApplicationContext(), "Could not upload data", Toast.LENGTH_LONG).show();
        }

      }
    }
    return true;
  }


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.history_visit);

    generator = new ReportGenerator(History.this.getHelper());

    txtCurrentStatus = (TextView)findViewById(R.id.txtCurrentStatus);
    progressBarSearch = (ProgressBar)findViewById(R.id.progressBarSearch);
    lv = (ListView)findViewById(R.id.lvVisitHistory);

    lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    lv.setItemsCanFocus(true);
    lv.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(final AdapterView<?> parentView, final View childView, final int position, final long id) {
        final Visit v = (Visit)lv.getItemAtPosition(position);

        final Intent i = new Intent(getApplicationContext(), Scoreboard.class);
        i.putExtra("visit_id", v.getId());

        startActivity(i);
      } // open parcour
    });

    registerForContextMenu(lv);
    refreshVisitList();
  } // onCreate


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
          txtCurrentStatus.setText("No results found!");
          progressBarSearch.setVisibility(View.GONE);
        } else {
          txtCurrentStatus.setVisibility(View.GONE);
          progressBarSearch.setVisibility(View.GONE);
          lv.setVisibility(View.VISIBLE);
          adapter = new VisitHistoryAdapter(History.this, R.layout.history_visit_row, result);
          lv.setAdapter(adapter);
        }
      }

    };
    task.execute(null);
  } // refreshVisitList

  public class VisitHistoryAdapter extends ArrayAdapter<Visit> {

    private static final String USER_TEXTVIEW = "scoring line";

    public final String TAG = VisitHistoryAdapter.class.getName();

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
