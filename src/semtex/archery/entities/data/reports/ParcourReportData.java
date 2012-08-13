
package semtex.archery.entities.data.reports;

import java.util.*;


public class ParcourReportData {

  private String parcourName;

  private Date parcourRevisionDate;

  private Date beginTime;

  private Date endTime;

  private final SortedMap<Integer, Map<String, Integer>> scoringData = new TreeMap<Integer, Map<String, Integer>>();

  private Map<String, Double> avgPoints = new HashMap<String, Double>();

  private Map<String, Integer> totalPoints = new HashMap<String, Integer>();


  public String getParcourName() {
    return parcourName;
  }


  public void setParcourName(final String parcourName) {
    this.parcourName = parcourName;
  }


  public Date getParcourRevisionDate() {
    return parcourRevisionDate;
  }


  public void setParcourRevisionDate(final Date parcourRevisionDate) {
    this.parcourRevisionDate = parcourRevisionDate;
  }


  public Map<Integer, Map<String, Integer>> getScoringData() {
    return scoringData;
  }


  public Map<String, Double> getAvgPoints() {
    return avgPoints;
  }


  public Map<String, Integer> getTotalPoints() {
    return totalPoints;
  }


  public void setAvgPoints(final Map<String, Double> avgPoints) {
    this.avgPoints = avgPoints;
  }


  public void setTotalPoints(final Map<String, Integer> totalPoints) {
    this.totalPoints = totalPoints;
  }


  public Date getBeginTime() {
    return beginTime;
  }


  public void setBeginTime(final Date beginTime) {
    this.beginTime = beginTime;
  }


  public Date getEndTime() {
    return endTime;
  }


  public void setEndTime(final Date endTime) {
    this.endTime = endTime;
  }

}
