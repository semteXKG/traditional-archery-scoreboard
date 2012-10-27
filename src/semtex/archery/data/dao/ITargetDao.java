
package semtex.archery.data.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import semtex.archery.data.entities.Target;
import semtex.archery.data.entities.Version;

import com.j256.ormlite.dao.Dao;


public interface ITargetDao extends Dao<Target, UUID> {

  Target findLastTarget(Version v) throws SQLException;


  List<Target> findTargetsByVersion(Version v) throws SQLException;


  Target findTargetByTargetNumber(final Integer targetNumber, final Version v) throws SQLException;

}
