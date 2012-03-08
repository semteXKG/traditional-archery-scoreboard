
package semtex.archery.entities.data.entities;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * @author semteX
 * 
 */
@DatabaseTable(tableName = "target")
public class Target implements Serializable {

  @DatabaseField(generatedId = true)
  private Long id;

  @DatabaseField
  private Integer targetNumber;

  @DatabaseField(foreign = true)
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


  public Long getId() {
    return id;
  }


  public void setId(final Long id) {
    this.id = id;
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
