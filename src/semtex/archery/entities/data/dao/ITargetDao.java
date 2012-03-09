
package semtex.archery.entities.data.dao;

import java.sql.SQLException;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.Version;

import com.j256.ormlite.dao.Dao;


public interface ITargetDao extends Dao<Target, Long> {

  Target findLastTarget(Version v) throws SQLException;
}
