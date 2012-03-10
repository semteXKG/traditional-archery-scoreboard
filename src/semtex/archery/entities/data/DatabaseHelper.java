/**
 * 
 */

package semtex.archery.entities.data;

import java.sql.SQLException;

import semtex.archery.entities.data.dao.*;
import semtex.archery.entities.data.entities.*;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;


/**
 * @author semteX
 * 
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

  private static final String DATABASE_NAME = "tac.db";

  private static final int DATABASE_VERSION = 6;

  private RuntimeExceptionDao<User, Long> userDao;

  private TargetRuntimeExceptionDao targetDao;

  private TargetHitRuntimeExceptionDao targetHitDao;

  private RuntimeExceptionDao<UserVisit, Long> userVisitDao;

  private VisitRuntimeExceptionDao visitDao;

  private RuntimeExceptionDao<Parcour, Long> parcourDao;

  private RuntimeExceptionDao<Version, Long> versionDao;


  /*
   * (non-Javadoc)
   * 
   * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase,
   * com.j256.ormlite.support.ConnectionSource)
   */

  public DatabaseHelper(final Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }


  @Override
  public void onCreate(final SQLiteDatabase database, final ConnectionSource connectionSource) {
    try {
      TableUtils.createTable(connectionSource, User.class);
      TableUtils.createTable(connectionSource, Parcour.class);
      TableUtils.createTable(connectionSource, Version.class);
      TableUtils.createTable(connectionSource, Visit.class);
      TableUtils.createTable(connectionSource, UserVisit.class);
      TableUtils.createTable(connectionSource, Target.class);
      TableUtils.createTable(connectionSource, TargetHit.class);
    } catch(final SQLException e) {
      Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
    }
  }


  /*
   * (non-Javadoc)
   * 
   * @see com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
   * com.j256.ormlite.support.ConnectionSource, int, int)
   */
  @Override
  public void onUpgrade(final SQLiteDatabase database, final ConnectionSource connectionSource, final int oldVersion,
      final int newVersion) {
    // TODO Auto-generated method stub

  }


  public RuntimeExceptionDao<User, Long> getUserDao() {
    if (userDao == null) {
      try {
        final Dao<User, Long> dao = getDao(User.class);
        userDao = new RuntimeExceptionDao<User, Long>(dao);
      } catch(final SQLException e) {
        Log.e(DatabaseHelper.class.getName(), "unable to create dao", e);
      }
    }
    return userDao;
  }


  public RuntimeExceptionDao<Parcour, Long> getParcourDao() {
    if (parcourDao == null) {
      try {
        final Dao<Parcour, Long> dao = getDao(Parcour.class);
        parcourDao = new RuntimeExceptionDao<Parcour, Long>(dao);
      } catch(final SQLException e) {
        Log.e(DatabaseHelper.class.getName(), "unable to create dao", e);
      }
    }
    return parcourDao;
  }


  public RuntimeExceptionDao<Version, Long> getVersionDao() {
    if (versionDao == null) {
      try {
        final Dao<Version, Long> dao = getDao(Version.class);
        versionDao = new RuntimeExceptionDao<Version, Long>(dao);
      } catch(final SQLException e) {
        Log.e(DatabaseHelper.class.getName(), "unable to create dao", e);
      }
    }
    return versionDao;
  }


  public TargetRuntimeExceptionDao getTargetDao() {
    if (targetDao == null) {
      try {
        final ITargetDao dao = getDao(Target.class);
        targetDao = new TargetRuntimeExceptionDao(dao);
      } catch(final SQLException e) {
        Log.e(DatabaseHelper.class.getName(), "unable to create dao", e);
      }
    }
    return targetDao;
  }


  public TargetHitRuntimeExceptionDao getTargetHitDao() {
    if (targetHitDao == null) {
      try {
        final ITargetHitDao dao = getDao(TargetHit.class);
        targetHitDao = new TargetHitRuntimeExceptionDao(dao);
      } catch(final SQLException e) {
        Log.e(DatabaseHelper.class.getName(), "unable to create dao", e);
      }
    }
    return targetHitDao;
  }


  public RuntimeExceptionDao<UserVisit, Long> getUserVisitDao() {
    if (userVisitDao == null) {
      try {
        final Dao<UserVisit, Long> dao = getDao(UserVisit.class);
        userVisitDao = new RuntimeExceptionDao<UserVisit, Long>(dao);
      } catch(final SQLException e) {
        Log.e(DatabaseHelper.class.getName(), "unable to create dao", e);
      }
    }
    return userVisitDao;
  }


  public VisitRuntimeExceptionDao getVisitDao() {
    if (visitDao == null) {
      try {
        final VisitDao dao = getDao(Visit.class);
        visitDao = new VisitRuntimeExceptionDao(dao);
      } catch(final SQLException e) {
        Log.e(DatabaseHelper.class.getName(), "unable to create dao", e);
      }
    }
    return visitDao;
  }

}
