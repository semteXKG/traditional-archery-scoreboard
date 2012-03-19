/**********************************************************************************************************************
 * ReportGenerator
 * 
 * created Mar 18, 2012 by semteX
 * 
 * (c) 2012 APEX gaming technology GmbH
 **********************************************************************************************************************/

package semtex.archery.entities.data;

import java.util.HashMap;
import java.util.Map;

import semtex.archery.entities.data.entities.*;
import semtex.archery.entities.data.reports.ParcourReportData;
import android.util.Log;


/**
 * @author semteX
 * 
 */
public class ReportGenerator {

  private static final String TAG = ReportGenerator.class.getName();

  private final DatabaseHelper daoHelper;


  public ReportGenerator(final DatabaseHelper daoHelper) {
    this.daoHelper = daoHelper;
  }


  public void generateReportForVisit(final Visit visit) {
    Log.i(TAG, "Beginning calculation");
    final ParcourReportData reportData = new ParcourReportData();

    final Map<String, Integer> totalNumbers = new HashMap<String, Integer>();
    final Map<String, Integer> totalPoints = new HashMap<String, Integer>();
    daoHelper.getVersionDao().refresh(visit.getVersion());
    final Version version = visit.getVersion();
    daoHelper.getParcourDao().refresh(version.getParcour());
    final Parcour parcour = version.getParcour();

    reportData.setParcourName(parcour.getName());
    reportData.setParcourRevisionDate(version.getCreated());

    final Map<Integer, Map<String, Double>> scoringData = reportData.getScoringData();

    for (final Target target : daoHelper.getTargetDao().findTargetsByVersion(version)) {
      final HashMap<String, Double> targetHitMap = new HashMap<String, Double>();
      for (final UserVisit uv : visit.getUserVisit()) {
        final TargetHit th = daoHelper.getTargetHitDao().findTargetHitByUserVisitAndTarget(uv, target);
        if (th != null && th.getPoints() != null) {
          targetHitMap.put(uv.getUser().getUserName(), (double)th.getPoints());
          totalNumbers.put(uv.getUser().getUserName(), safeGet(totalNumbers, uv.getUser().getUserName()) + 1);
          totalPoints
              .put(uv.getUser().getUserName(), safeGet(totalPoints, uv.getUser().getUserName()) + th.getPoints());
        }
      }
      scoringData.put(target.getTargetNumber(), targetHitMap);
    }

    final Map<String, Double> avgPoints = new HashMap<String, Double>();

    for (final Map.Entry<String, Integer> totalNumberEntry : totalNumbers.entrySet()) {
      final double avgCalcPoints = totalPoints.get(totalNumberEntry.getKey()) * 1.0 / totalNumberEntry.getValue();
      avgPoints.put(totalNumberEntry.getKey(), avgCalcPoints);
    }
    scoringData.put(0, avgPoints);
    Log.i(TAG, "Ending calculation");
  }


  private int safeGet(final Map<String, Integer> map, final String key) {
    final Integer val = map.get(key);
    if (val == null) {
      return 0;
    }
    return val;
  }
}
