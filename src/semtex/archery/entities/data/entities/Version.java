/**********************************************************************************************************************
 * Revision
 * 
 * created Mar 6, 2012 by semteX
 * 
 * (c) 2012 APEX gaming technology GmbH
 **********************************************************************************************************************/

package semtex.archery.entities.data.entities;

import java.io.Serializable;

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


  public Version() {

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
