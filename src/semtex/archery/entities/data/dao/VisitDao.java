
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.List;

import semtex.archery.entities.data.entities.Visit;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;


public class VisitDao extends BaseDaoImpl<Visit, Long> implements IVisitDao {

  public VisitDao(final ConnectionSource connectionSource, final Class dataClass) throws SQLException {
    super(connectionSource, dataClass);
  }


  public List<Visit> findAllVisits(final boolean ascending, final long limit) throws SQLException {
    final QueryBuilder<Visit, Long> qb = queryBuilder();
    if (limit != 0) {
      qb.limit(limit);
    }
    qb.orderBy(Visit.END_TIME, ascending);
    return qb.query();
  }


  public Visit findLastOpenVisit() throws SQLException {
    final QueryBuilder<Visit, Long> qb = queryBuilder();
    qb.where().isNull(Visit.END_TIME);
    qb.orderBy(Visit.BEGIN_TIME, false);
    final Visit visit = qb.queryForFirst();
    return visit;
  }

}
