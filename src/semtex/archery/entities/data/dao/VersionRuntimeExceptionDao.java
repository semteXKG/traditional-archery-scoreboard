
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.UUID;

import semtex.archery.entities.data.entities.Parcour;
import semtex.archery.entities.data.entities.Version;

import com.j256.ormlite.dao.RuntimeExceptionDao;


public class VersionRuntimeExceptionDao extends RuntimeExceptionDao<Version, UUID> {

  private IVersionDao versionDao = null;


  public VersionRuntimeExceptionDao(final IVersionDao versionDao) {
    super(versionDao);
    this.versionDao = versionDao;
  }


  public Version findLatestVersion(final Parcour parcour) {
    try {
      return versionDao.findLatestVersion(parcour);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
