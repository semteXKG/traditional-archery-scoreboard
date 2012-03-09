
package semtex.archery.entities.data.dao;

import java.sql.SQLException;

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

}
