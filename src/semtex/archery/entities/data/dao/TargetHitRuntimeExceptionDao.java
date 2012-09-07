
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.TargetHit;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;

import com.j256.ormlite.dao.RuntimeExceptionDao;


public class TargetHitRuntimeExceptionDao extends RuntimeExceptionDao<TargetHit, UUID> {

  ITargetHitDao dao;


  public TargetHitRuntimeExceptionDao(final ITargetHitDao dao) {
    super(dao);
    this.dao = dao;
  }


  public TargetHit findTargetHitByUserVisitAndTarget(final UserVisit userVisit, final Target target) {
    try {
      return dao.findTargetHitByUserVisitAndTarget(userVisit, target);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public Integer calculatePointsByUser(final UserVisit userVisit) {
    try {
      return dao.calculatePointsByUser(userVisit);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public Integer deleteTargetHitsFromUserVisit(final UserVisit uv) {
    try {
      return dao.deleteTargetHitsFromUserVisit(uv);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public Integer getLatestTargetNumber(final Visit v) {
    try {
      return dao.getLatestTargetNumber(v);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public List<TargetHit> findTargetHitsByVisitAndTarget(final Visit currentVisit, final Target target) {
    try {
      return dao.findTargetHitsByVisitAndTarget(currentVisit, target);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
