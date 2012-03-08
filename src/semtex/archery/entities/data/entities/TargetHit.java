
package semtex.archery.entities.data.entities;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "target_hit")
public class TargetHit implements Serializable {

  @DatabaseField(generatedId = true)
  private Long id;

  @DatabaseField
  private Integer points;

  @DatabaseField(foreign = true)
  private UserVisit user;

  @DatabaseField(foreign = true)
  private Target target;


  public TargetHit() {

  }


  public TargetHit(final Integer points, final UserVisit user, final Target target) {
    this.points = points;
    this.user = user;
    this.target = target;
  }


  public Long getId() {
    return id;
  }


  public void setId(final Long id) {
    this.id = id;
  }


  public Integer getPoints() {
    return points;
  }


  public void setPoints(final Integer points) {
    this.points = points;
  }


  public UserVisit getUser() {
    return user;
  }


  public void setUser(final UserVisit user) {
    this.user = user;
  }


  public Target getTarget() {
    return target;
  }


  public void setTarget(final Target target) {
    this.target = target;
  }

}
