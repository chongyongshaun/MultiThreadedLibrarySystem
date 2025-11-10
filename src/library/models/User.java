package library.models;

import java.io.Serializable;

public class User implements Serializable{
	//versioning the class so you dont deserialize an outdated ver of the class i.e changed name or whatever
	private static final long serialVersionUID = 1L; 
	
	private String name;                              
    private String studentId;                        
    private String email;                             
    private String password;                          
    private String department;                       
    private UserRole role;
    
	public User(String name, String studentId, String email, String password, String department, UserRole role) {
		super();
		this.name = name;
		this.studentId = studentId;
		this.email = email;
		this.password = password;
		this.department = department;
		this.role = role;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "User [name=" + name + ", studentId=" + studentId + ", email=" + email + ", password=" + password
				+ ", department=" + department + ", role=" + role + "]";
	}   
    
    
}
