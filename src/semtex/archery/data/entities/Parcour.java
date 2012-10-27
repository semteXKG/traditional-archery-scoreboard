
package semtex.archery.data.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * @author semteX
 * 
 */
@DatabaseTable(tableName = "parcour")
public class Parcour implements Serializable {

  @DatabaseField(generatedId = true)
  private UUID id = UUID.randomUUID();

  @DatabaseField(unique = true)
  private String name;

  @DatabaseField()
  private String city;

  @DatabaseField(columnName = "created_at")
  private Date created;

  @DatabaseField()
  private Float latitude;

  @DatabaseField()
  private Float longitude;


  public Parcour() {

  }


  public UUID getId() {
    return id;
  }


  public void setId(final UUID id) {
    this.id = id;
  }


  public String getName() {
    return name;
  }


  public void setName(final String name) {
    this.name = name;
  }


  public String getCity() {
    return city;
  }


  public void setCity(final String city) {
    this.city = city;
  }


  public Date getCreated() {
    return created;
  }


  public void setCreated(final Date created) {
    this.created = created;
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


  @Override
  public String toString() {
    return name;
  }

}
