
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.List;

import semtex.archery.entities.data.entities.Visit;

import com.j256.ormlite.dao.Dao;


public interface IVisitDao extends Dao<Visit, Long> {

  public Visit findLastOpenVisit() throws SQLException;


  public List<Visit> findAllVisits(boolean ascending, long limit) throws SQLException;

}
