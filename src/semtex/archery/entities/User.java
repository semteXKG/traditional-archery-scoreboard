package semtex.archery.entities;

public class User {

	private Long id;
	
	private String userName;
	
	private String mail;
	
	private int rgbColor;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}
	
	public int getRgbColor() {
		return rgbColor;
	}

	public void setRgbColor(int rgbColor) {
		this.rgbColor = rgbColor;
	}

	public User(Long id, String userName, String mail, int rgbColor) {
		super();
		this.id = id;
		this.userName = userName;
		this.mail = mail;
		this.rgbColor = rgbColor;
	}
	
	
	
}
