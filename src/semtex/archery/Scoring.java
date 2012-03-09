
package semtex.archery;

import java.util.HashMap;
import java.util.Map;

import semtex.archery.entities.data.DatabaseHelper;
import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;
import android.os.Bundle;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.ForeignCollection;


public class Scoring extends OrmLiteBaseActivity<DatabaseHelper> {

  public static final String TAG = Scoring.class.getName();

  private Visit currentVisit;

  private Target currentTarget;

  Map<UserVisit, Long> userScoring = new HashMap<UserVisit, Long>();


  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.scoring);

    fetchSetupData();

  }


  private void fetchSetupData() {
    currentVisit = getHelper().getVisitDao().findLastOpenVisit();
    Log.i(TAG, "Found: " + currentVisit);
    currentTarget = getHelper().getTargetDao().findLastTarget(currentVisit.getVersion());
    Log.i(TAG, "current Target set to: " + currentTarget);
    final ForeignCollection<UserVisit> uv = currentVisit.getUserVisit();
    Log.i(TAG, "added " + uv.size() + " players");
  }


  private void getCurrentVisit() {
    // final QueryBuilder<Visit, Long> qb = getHelper().getVisitDao().queryBuilder();
    // try {
    // qb.where().eq(Visit.END_TIME, null);
    // qb.orderBy(Visit.BEGIN_TIME, true);
    // Visit visit2 = qb.queryForFirst();
    // if(visit2 != null)
    // } catch(SQLException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

  }
}
