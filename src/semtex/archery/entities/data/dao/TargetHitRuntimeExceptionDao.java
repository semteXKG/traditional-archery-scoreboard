
package semtex.archery.entities.data.dao;

import java.sql.SQLException;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.TargetHit;
import semtex.archery.entities.data.entities.UserVisit;

import com.j256.ormlite.dao.RuntimeExceptionDao;


public class TargetHitRuntimeExceptionDao extends RuntimeExceptionDao<TargetHit, Long> {

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

}
