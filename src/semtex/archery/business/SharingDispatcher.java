/**********************************************************************************************************************
 * SharingDispatcher
 * 
 * created 06.09.2012 by semtex
 * 
 * (c) 2012 APEX gaming technology GmbH
 **********************************************************************************************************************/

package semtex.archery.business;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import semtex.archery.business.interfaces.ICallback;
import semtex.archery.data.DatabaseHelper;
import semtex.archery.data.ReportGenerator;
import semtex.archery.data.entities.UserVisit;
import semtex.archery.data.entities.Visit;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;


/**
 * @author semtex
 * 
 */
public class SharingDispatcher {

  private static final String TAG = SharingDispatcher.class.getName();

  private final DateFormat dateFormatter = DateFormat.getDateInstance();

  private final DatabaseHelper helper;

  private final ReportGenerator generator;

  private final Visit v;


  public SharingDispatcher(final DatabaseHelper helper, final Visit v) {
    this.helper = helper;
    this.v = v;
    this.generator = new ReportGenerator(helper);
  }


  public Intent shareMail() {
    final ArrayList<String> recipients = new ArrayList<String>();
    for (final UserVisit uv : v.getUserVisit()) {
      if (uv.getUser().getMail() != null && !"".equals(uv.getUser().getMail())) {
        recipients.add(uv.getUser().getMail());
      } // if
    } // for

    File report = null;
    try {
      report = generator.generatePDFReportForVisit(v);
    } catch(final Exception e) {
      e.printStackTrace();
    } // try / catch

    Log.i(TAG, "Found " + recipients.size() + " recpients");

    final Intent sharingIntent = new Intent(Intent.ACTION_SEND);
    sharingIntent.setType("text/html");
    sharingIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients.toArray(new String[recipients.size()]));
    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Results from "
        + v.getVersion().getParcour().getName() + " on " + dateFormatter.format(v.getBeginTime()));
    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(generator.generateHTMLReportForVisit(v)));
    if (report != null) {
      sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(report));
    } // if
    return sharingIntent;
  } // shareMail


  public void shareServer(final ICallback<Void> callback) {
    final AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

      private Throwable exception;


      @Override
      protected Boolean doInBackground(final Void... params) {
        final List<String> generateJsonObjectsForVisit = generator.generateJsonObjectsForVisit(v);
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
              return true;
            }// if
            return false;
          } catch(final Exception ex) {
            this.exception = ex;
            return false;
          } // try / catch
        } // for
        return false;
      } // doInBackground


      @Override
      protected void onPostExecute(final Boolean result) {
        if (callback != null) {
          if (result) {
            callback.onSuccess(null);
          } else {
            callback.onFailure(exception);
          }
        }
      }
    };
    task.execute((Void)null);
  } // shareServer
}
