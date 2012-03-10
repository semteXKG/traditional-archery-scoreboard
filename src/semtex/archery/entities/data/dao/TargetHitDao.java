
package semtex.archery.entities.data.dao;

import java.sql.SQLException;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.TargetHit;
import semtex.archery.entities.data.entities.UserVisit;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;


public class TargetHitDao extends BaseDaoImpl<TargetHit, Long> implements ITargetHitDao {

  public TargetHitDao(final ConnectionSource connectionSource) throws SQLException {
    super(connectionSource, TargetHit.class);
  }


  public TargetHit findTargetHitByUserVisitAndTarget(final UserVisit userVisit, final Target target)
      throws SQLException {
    final QueryBuilder<TargetHit, Long> qb = queryBuilder();
    final Where<TargetHit, Long> where = qb.where();

    where.and(where.eq(TargetHit.USER_VISIT, userVisit), where.eq(TargetHit.TARGET, target));

    qb.setWhere(where);
    return qb.queryForFirst();
  }
}
