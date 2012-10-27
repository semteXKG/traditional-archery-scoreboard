
package semtex.archery.data.dao;

import java.sql.SQLException;
import java.util.UUID;

import semtex.archery.data.entities.Parcour;
import semtex.archery.data.entities.Version;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;


public class VersionDao extends BaseDaoImpl<Version, UUID> implements IVersionDao {

  public VersionDao(final ConnectionSource connectionSource) throws SQLException {
    super(connectionSource, Version.class);
  }


  public Version findLatestVersion(final Parcour parcour) throws SQLException {
    final QueryBuilder<Version, UUID> queryBuilder = queryBuilder();
    final Where<Version, UUID> where = queryBuilder.where();
    where.eq(Version.PARCOUR_NAME, parcour);
    queryBuilder.setWhere(where);
    queryBuilder.orderBy(Version.CREATED_NAME, false);
    queryBuilder.limit(1L);
    return queryBuilder.queryForFirst();
  }
}
