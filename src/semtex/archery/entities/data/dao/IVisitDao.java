
package semtex.archery.entities.data.dao;

import java.sql.SQLException;

import semtex.archery.entities.data.entities.Visit;

import com.j256.ormlite.dao.Dao;


public interface IVisitDao extends Dao<Visit, Long> {

  public Visit findLastOpenVisit() throws SQLException;

}
