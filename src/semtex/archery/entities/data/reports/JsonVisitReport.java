
package semtex.archery.entities.data.reports;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


public class JsonVisitReport {

  private String name;

  private String label;

  private Date parcourDate;

  @JsonProperty(value = "date")
  private Date visitDate;

  @JsonSerialize(using = KeyValueSerializer.class)
  private Map<Integer, Integer> data;


  public String getName() {
    return name;
  }


  public void setName(final String name) {
    this.name = name;
  }


  public String getLabel() {
    return label;
  }


  public void setLabel(final String label) {
    this.label = label;
  }


  public Date getParcourDate() {
    return parcourDate;
  }


  public void setParcourDate(final Date parcourDate) {
    this.parcourDate = parcourDate;
  }


  public Date getVisitDate() {
    return visitDate;
  }


  public void setVisitDate(final Date visitDate) {
    this.visitDate = visitDate;
  }


  public Map<Integer, Integer> getData() {
    return data;
  }


  public void setData(final Map<Integer, Integer> data) {
    this.data = data;
  }

}
