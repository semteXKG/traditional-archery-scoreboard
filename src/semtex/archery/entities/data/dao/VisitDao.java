
package semtex.archery.entities.data.dao;

import java.sql.SQLException;

import semtex.archery.entities.data.entities.Visit;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;


public class VisitDao extends BaseDaoImpl<Visit, Long> implements IVisitDao {

  public VisitDao(final ConnectionSource connectionSource, final Class dataClass) throws SQLException {
    super(connectionSource, dataClass);
  }


  public Visit findLastOpenVisit() throws SQLException {
    final QueryBuilder<Visit, Long> qb = queryBuilder();
    qb.where().isNull(Visit.END_TIME);
    qb.orderBy(Visit.BEGIN_TIME, false);
    final Visit visit = qb.queryForFirst();
    return visit;
  }

}
