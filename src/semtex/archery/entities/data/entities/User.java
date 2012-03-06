
package semtex.archery.entities.data.entities;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "user")
public class User implements Serializable {

  @DatabaseField(generatedId = true)
  private Long id;

  @DatabaseField
  private String userName;

  @DatabaseField
  private String mail;

  @DatabaseField
  private int rgbColor;


  public Long getId() {
    return id;
  }


  public void setId(final Long id) {
    this.id = id;
  }


  public String getUserName() {
    return userName;
  }


  public void setUserName(final String userName) {
    this.userName = userName;
  }


  public String getMail() {
    return mail;
  }


  public void setMail(final String mail) {
    this.mail = mail;
  }


  public int getRgbColor() {
    return rgbColor;
  }


  public void setRgbColor(final int rgbColor) {
    this.rgbColor = rgbColor;
  }


  public User(final String userName, final String mail, final int rgbColor) {
    this.userName = userName;
    this.mail = mail;
    this.rgbColor = rgbColor;
  }


  public User() {

  }


  @Override
  public String toString() {
    return userName;
  }

}
