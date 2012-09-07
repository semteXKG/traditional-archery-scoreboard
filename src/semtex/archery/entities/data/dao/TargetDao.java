
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.Version;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;


public class TargetDao extends BaseDaoImpl<Target, UUID> implements ITargetDao {

  public TargetDao(final ConnectionSource connectionSource) throws SQLException {
    super(connectionSource, Target.class);
  }


  public Target findLastTarget(final Version v) throws SQLException {
    final QueryBuilder<Target, UUID> qb = queryBuilder();
    qb.where().eq(Target.VERSION, v);
    qb.limit(1L);
    qb.orderBy(Target.TARGETNUMBER, false);
    return qb.queryForFirst();
  }


  public List<Target> findTargetsByVersion(final Version v) throws SQLException {
    final QueryBuilder<Target, UUID> qb = queryBuilder();
    qb.where().eq(Target.VERSION, v);
    qb.orderBy(Target.TARGETNUMBER, true);
    return qb.query();
  }


  public Target findTargetByTargetNumber(final Integer targetNumber, final Version v) throws SQLException {
    final QueryBuilder<Target, UUID> qb = queryBuilder();
    final Where<Target, UUID> where = qb.where();
    where.and(where.eq(Target.VERSION, v), where.eq(Target.TARGETNUMBER, targetNumber));
    qb.setWhere(where);
    return qb.queryForFirst();
  }
}
