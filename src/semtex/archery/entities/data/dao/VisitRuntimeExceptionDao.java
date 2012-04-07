
package semtex.archery.entities.data.dao;

import java.sql.SQLException;
import java.util.List;

import semtex.archery.entities.data.entities.Visit;

import com.j256.ormlite.dao.RuntimeExceptionDao;


public class VisitRuntimeExceptionDao extends RuntimeExceptionDao<Visit, Long> {

  private final IVisitDao dao;


  public VisitRuntimeExceptionDao(final IVisitDao dao) {
    super(dao);
    this.dao = dao;
  }


  public Visit findLastOpenVisit() {
    try {
      return dao.findLastOpenVisit();
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }


  public List<Visit> findAllVisits(final boolean ascending, final long limit) {
    try {
      return dao.findAllVisits(ascending, limit);
    } catch(final SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
