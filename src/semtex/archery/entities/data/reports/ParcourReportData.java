
package semtex.archery.entities.data.reports;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ParcourReportData {

  private String parcourName;

  private Date parcourRevisionDate;

  private final Map<Integer, Map<String, Double>> scoringData = new HashMap<Integer, Map<String, Double>>();


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


  public Map<Integer, Map<String, Double>> getScoringData() {
    return scoringData;
  }

}
