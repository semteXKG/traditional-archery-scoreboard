
package semtex.archery.entities.data.entities;

import java.io.Serializable;
import java.util.Date;

import semtex.archery.entities.data.dao.VisitDao;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(daoClass = VisitDao.class, tableName = "visit")
public class Visit implements Serializable {

  public static final String END_TIME = "end_time";

  public static final String BEGIN_TIME = "begin_time";

  @DatabaseField(generatedId = true)
  private Long id;

  @DatabaseField(columnName = BEGIN_TIME)
  private Date beginTime;

  @DatabaseField(columnName = END_TIME)
  private Date endTime;

  @DatabaseField(foreign = true)
  private Version version;

  @ForeignCollectionField(eager = true)
  private ForeignCollection<UserVisit> userVisit;


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


  @Override
  public String toString() {
    return "Visit [id=" + id + ", beginTime=" + beginTime + ", endTime=" + endTime + "]";
  }


  public ForeignCollection<UserVisit> getUserVisit() {
    return userVisit;
  }


  public void setUserVisit(final ForeignCollection<UserVisit> userVisit) {
    this.userVisit = userVisit;
  }

}
