
package semtex.archery.entities.data.entities;

import java.io.Serializable;
import java.util.UUID;

import semtex.archery.entities.data.dao.TargetHitDao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "target_hit", daoClass = TargetHitDao.class)
public class TargetHit implements Serializable {

  public static final String TARGET = "target";

  public static final String USER_VISIT = "user";

  @DatabaseField(generatedId = true)
  private UUID id = UUID.randomUUID();

  @DatabaseField
  private Integer points;

  @DatabaseField
  private Integer nrOfArrows;

  @DatabaseField(foreign = true, columnName = USER_VISIT)
  private UserVisit user;

  @DatabaseField(foreign = true, columnName = TARGET)
  private Target target;


  public TargetHit() {

  }


  public TargetHit(final UserVisit uv, final Target target) {
    this.user = uv;
    this.target = target;
  }


  public TargetHit(final Integer points, final Integer nrOfArrows, final UserVisit user, final Target target) {
    this.points = points;
    this.nrOfArrows = nrOfArrows;
    this.user = user;
    this.target = target;
  }


  public UUID getId() {
    return id;
  }


  public void setId(final UUID id) {
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


  @Override
  public String toString() {
    return "TargetHit [id=" + id + ", points=" + points + ", nrOfArrows=" + nrOfArrows + "]";
  }


  public Integer getNrOfArrows() {
    return nrOfArrows;
  }


  public void setNrOfArrows(final Integer nrOfArrows) {
    this.nrOfArrows = nrOfArrows;
  }

}
