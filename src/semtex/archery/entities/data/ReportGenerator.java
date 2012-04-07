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

import semtex.archery.entities.data.entities.Parcour;
import semtex.archery.entities.data.entities.Version;
import semtex.archery.entities.data.entities.Visit;
import semtex.archery.entities.data.reports.ParcourReportData;
import android.util.Log;

import com.j256.ormlite.dao.GenericRawResults;


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


  public ParcourReportData generateReportForVisit(final Visit visit) {
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
    Log.i(TAG, "STARTING NEW");
    final GenericRawResults<String[]> queryRaw =
        daoHelper.getParcourDao().queryRaw(
            "SELECT u.userName, target.target_number, target_hit.points FROM visit "
                + "LEFT JOIN version ON visit.version_id = version.id "
                + "LEFT JOIN target ON target.version= version.id "
                + "LEFT JOIN target_hit ON target_hit.target = target.id "
                + "LEFT JOIN user_visit uv ON target_hit.user = uv.id " + "LEFT JOIN user u ON uv.user_id = u.id "
                + "WHERE visit.id=" + visit.getId() + " AND uv.visit_id=" + visit.getId()
                + " ORDER BY target.target_number");
    // new DataType[] { DataType.STRING, DataType.INTEGER_OBJ, DataType.INTEGER_OBJ
    final Map<Integer, Map<String, Double>> scoringData = reportData.getScoringData();

    for (final String[] objects : queryRaw) {
      final String userName = objects[0];
      final Integer targetNumber = Integer.valueOf(objects[1]);
      final Double points = objects[2] != null ? Double.valueOf(objects[2]) : null;
      if (points != null) {
        Map<String, Double> targetHitMap = scoringData.get(targetNumber);
        if (targetHitMap == null) {
          targetHitMap = new HashMap<String, Double>();
          scoringData.put(targetNumber, targetHitMap);
        }
        targetHitMap.put(userName, points);
        totalNumbers.put(userName, safeGet(totalNumbers, userName) + 1);
        totalPoints.put(userName, (int)(safeGet(totalPoints, userName) + points));
      }
    }

    final Map<String, Double> avgPoints = new HashMap<String, Double>();

    for (final Map.Entry<String, Integer> totalNumberEntry : totalNumbers.entrySet()) {
      final double avgCalcPoints = totalPoints.get(totalNumberEntry.getKey()) * 1.0 / totalNumberEntry.getValue();
      avgPoints.put(totalNumberEntry.getKey(), avgCalcPoints);
    }
    scoringData.put(0, avgPoints);
    Log.i(TAG, "Ending calculation");
    return reportData;
  }


  private int safeGet(final Map<String, Integer> map, final String key) {
    final Integer val = map.get(key);
    if (val == null) {
      return 0;
    }
    return val;
  }
}
