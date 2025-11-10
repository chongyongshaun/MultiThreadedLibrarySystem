package library.models;

import java.io.Serializable;

//wrapper class for payload for login 
public class LoginData implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String email;
	private String password;
	
	public LoginData(String email, String password) {
		super();
		this.email = email;
		this.password = password;
	}
	public String getEmail() {
		return email;
	}
	public String getPassword() {
		return password;
	}
	
	
}
