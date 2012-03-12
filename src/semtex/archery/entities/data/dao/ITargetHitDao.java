
package semtex.archery.entities.data.dao;

import java.sql.SQLException;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.TargetHit;
import semtex.archery.entities.data.entities.UserVisit;

import com.j256.ormlite.dao.Dao;


public interface ITargetHitDao extends Dao<TargetHit, Long> {

  TargetHit findTargetHitByUserVisitAndTarget(UserVisit user, Target target) throws SQLException;


  Integer calculatePointsByUser(final UserVisit userVisit) throws SQLException;
}
