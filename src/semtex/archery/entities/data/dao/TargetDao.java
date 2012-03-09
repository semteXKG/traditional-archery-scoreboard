
package semtex.archery.entities.data.dao;

import java.sql.SQLException;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.Version;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;


public class TargetDao extends BaseDaoImpl<Target, Long> implements ITargetDao {

  public TargetDao(final ConnectionSource connectionSource) throws SQLException {
    super(connectionSource, Target.class);
  }


  public Target findLastTarget(final Version v) throws SQLException {
    final QueryBuilder<Target, Long> qb = queryBuilder();
    qb.where().eq(Target.VERSION, v);
    qb.limit(1L);
    qb.orderBy(Target.TARGETNUMBER, false);
    return qb.queryForFirst();
  }

}
