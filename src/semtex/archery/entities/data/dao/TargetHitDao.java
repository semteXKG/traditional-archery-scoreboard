
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.TargetHit;
import semtex.archery.entities.data.entities.UserVisit;
import semtex.archery.entities.data.entities.Visit;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;


public class TargetHitDao extends BaseDaoImpl<TargetHit, UUID> implements ITargetHitDao {

  public TargetHitDao(final ConnectionSource connectionSource) throws SQLException {
    super(connectionSource, TargetHit.class);
  }


  public Integer getLatestTargetNumber(final Visit v) throws SQLException {
    final GenericRawResults<String[]> results =
        queryRaw("SELECT MAX(target.target_number) FROM visit " + "LEFT JOIN user_visit uv ON uv.visit_id = visit.id "
            + "LEFT JOIN target_hit th ON th.user = uv.id " + "LEFT JOIN target ON target.id = th.target "
            + "WHERE visit.id = \"" + v.getId() + "\"");

    try {
      for (final String[] result : results) {
        if (result[0] != null) {
          return Integer.parseInt(result[0]);
        }
      }
    } finally {
      results.close();
    }
    return 1;
  }


  public Integer deleteTargetHitsFromUserVisit(final UserVisit uv) throws SQLException {
    final DeleteBuilder<TargetHit, UUID> deleteBuilder = deleteBuilder();
    deleteBuilder.where().eq(TargetHit.USER_VISIT, uv.getId());
    return delete(deleteBuilder.prepare());
  }


  public TargetHit findTargetHitByUserVisitAndTarget(final UserVisit userVisit, final Target target)
      throws SQLException {
    final QueryBuilder<TargetHit, UUID> qb = queryBuilder();
    final Where<TargetHit, UUID> where = qb.where();

    where.and(where.eq(TargetHit.USER_VISIT, userVisit), where.eq(TargetHit.TARGET, target));

    qb.setWhere(where);
    return qb.queryForFirst();
  }


  public Integer calculatePointsByUser(final UserVisit userVisit) throws SQLException {
    final QueryBuilder<TargetHit, UUID> queryBuilder = queryBuilder();
    queryBuilder.selectRaw("SUM(points)");
    queryBuilder.where().eq(TargetHit.USER_VISIT, userVisit);
    final GenericRawResults<Object[]> results =
        queryRaw(queryBuilder.prepareStatementString(), new DataType[] { DataType.INTEGER });
    return (Integer)results.getFirstResult()[0];
  }


  public List<TargetHit> findTargetHitsByVisitAndTarget(final Visit currentVisit, final Target target)
      throws SQLException {
    final QueryBuilder<TargetHit, UUID> qb = queryBuilder();
    final Where<TargetHit, UUID> where = qb.where();
    where.and(where.eq(TargetHit.TARGET, target), where.in(TargetHit.USER_VISIT, currentVisit.getUserVisit()));
    qb.setWhere(where);
    return qb.query();
  }

}
