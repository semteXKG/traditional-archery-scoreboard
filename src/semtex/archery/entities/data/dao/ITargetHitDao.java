
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.TargetHit;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;

import com.j256.ormlite.dao.Dao;


public interface ITargetHitDao extends Dao<TargetHit, UUID> {

  TargetHit findTargetHitByUserVisitAndTarget(UserVisit user, Target target) throws SQLException;


  Integer calculatePointsByUser(final UserVisit userVisit) throws SQLException;


  Integer getLatestTargetNumber(final Visit v) throws SQLException;


  Integer deleteTargetHitsFromUserVisit(final UserVisit uv) throws SQLException;


  List<TargetHit> findTargetHitsByVisitAndTarget(Visit currentVisit, Target target) throws SQLException;

}
