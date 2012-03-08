
package semtex.archery.entities.data.entities;

import java.io.Serializable;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "user_visit")
public class UserVisit implements Serializable {

  @DatabaseField(generatedId = true)
  private Long id;

  @DatabaseField(foreign = true)
  private User user;

  @DatabaseField(foreign = true)
  private Visit visit;

  @ForeignCollectionField(eager = false)
  private ForeignCollection<TargetHit> targetHits;


  public UserVisit() {

  }


  public UserVisit(final User user, final Visit visit) {
    this.user = user;
    this.visit = visit;
  }


  public Long getId() {
    return id;
  }


  public void setId(final Long id) {
    this.id = id;
  }


  public User getUser() {
    return user;
  }


  public void setUser(final User user) {
    this.user = user;
  }


  public Visit getVisit() {
    return visit;
  }


  public void setVisit(final Visit visit) {
    this.visit = visit;
  }


  public ForeignCollection<TargetHit> getTargetHits() {
    return targetHits;
  }


  public void setTargetHits(final ForeignCollection<TargetHit> targetHits) {
    this.targetHits = targetHits;
  }

}
