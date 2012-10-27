
package semtex.archery.data.entities;

import java.io.Serializable;
import java.util.UUID;

import semtex.archery.data.dao.TargetDao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * @author semteX
 * 
 */
@DatabaseTable(tableName = "target", daoClass = TargetDao.class)
public class Target implements Serializable {

  public static final String VERSION = "version";

  public static final String TARGETNUMBER = "target_number";

  @DatabaseField(generatedId = true)
  private final UUID id = UUID.randomUUID();

  @DatabaseField(columnName = TARGETNUMBER, index = true)
  private Integer targetNumber;

  @DatabaseField(foreign = true, columnName = VERSION)
  private Version version;

  @DatabaseField
  private Float latitude;

  @DatabaseField
  private Float longitude;

  @DatabaseField
  private String pictureLocation;

  @DatabaseField
  private String comment;


  public Target() {
  }


  public Target(final Integer targetNumber, final Version version) {
    this.targetNumber = targetNumber;
    this.version = version;
  }


  public UUID getId() {
    return id;
  }


  public Integer getTargetNumber() {
    return targetNumber;
  }


  public void setTargetNumber(final Integer targetNumber) {
    this.targetNumber = targetNumber;
  }


  public Version getVersion() {
    return version;
  }


  public void setVersion(final Version version) {
    this.version = version;
  }


  public Float getLatitude() {
    return latitude;
  }


  public void setLatitude(final Float latitude) {
    this.latitude = latitude;
  }


  public Float getLongitude() {
    return longitude;
  }


  public void setLongitude(final Float longitude) {
    this.longitude = longitude;
  }


  public String getPictureLocation() {
    return pictureLocation;
  }


  public void setPictureLocation(final String pictureLocation) {
    this.pictureLocation = pictureLocation;
  }


  public String getComment() {
    return comment;
  }


  public void setComment(final String comment) {
    this.comment = comment;
  }

}
