
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.UUID;

import semtex.archery.entities.data.entities.Parcour;
import semtex.archery.entities.data.entities.Version;

import com.j256.ormlite.dao.Dao;


public interface IVersionDao extends Dao<Version, UUID> {

  Version findLatestVersion(Parcour parcour) throws SQLException;

}
