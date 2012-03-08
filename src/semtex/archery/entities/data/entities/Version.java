
package semtex.archery.entities.data.entities;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * @author semteX
 * 
 */
@DatabaseTable()
public class Version implements Serializable {

  @DatabaseField(generatedId = true)
  private Long id;

  @DatabaseField(canBeNull = false, foreign = true)
  private Parcour parcour;

  @DatabaseField()
  private String name;

  @DatabaseField(columnName = "created_at")
  private Date created;


  public Version() {

  }


  public Version(final String name, final Parcour parcour) {
    this.name = name;
    this.parcour = parcour;
    created = new Date();
  }


  public Long getId() {
    return id;
  }


  public void setId(final Long id) {
    this.id = id;
  }


  public Parcour getParcour() {
    return parcour;
  }


  public void setParcour(final Parcour parcour) {
    this.parcour = parcour;
  }


  public String getName() {
    return name;
  }


  public void setName(final String name) {
    this.name = name;
  }

}
