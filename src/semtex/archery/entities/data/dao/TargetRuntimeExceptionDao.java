
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import semtex.archery.entities.data.entities.Target;
import semtex.archery.entities.data.entities.Version;

import com.j256.ormlite.dao.RuntimeExceptionDao;


public class TargetRuntimeExceptionDao extends RuntimeExceptionDao<Target, UUID> {

  private final ITargetDao dao;


  public TargetRuntimeExceptionDao(final ITargetDao dao) {
    super(dao);
    this.dao = dao;
  }


  public Target findLastTarget(final Version v) {
    try {
      return dao.findLastTarget(v);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public List<Target> findTargetsByVersion(final Version version) {
    try {
      return dao.findTargetsByVersion(version);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public Target findTargetByTargetNumber(final Integer targetNumber, final Version v) {
    try {
      return dao.findTargetByTargetNumber(targetNumber, v);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
