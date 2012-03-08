
package semtex.archery.entities.data.entities;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;


public class Visit implements Serializable {

  @DatabaseField(generatedId = true)
  private Long id;

  @DatabaseField
  private Date beginTime;

  @DatabaseField
  private Date endTime;

  @DatabaseField(foreign = true)
  private Version version;


  public Visit() {

  }


  public Visit(final Date beginTime, final Version version) {
    this.beginTime = beginTime;
    this.version = version;
  }


  public Long getId() {
    return id;
  }


  public void setId(final Long id) {
    this.id = id;
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


  public Version getVersion() {
    return version;
  }


  public void setVersion(final Version version) {
    this.version = version;
  }

}
