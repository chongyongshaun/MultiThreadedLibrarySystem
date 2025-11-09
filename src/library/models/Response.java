package library.models;

import java.io.Serializable;

public class Response implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private boolean success;
	private String message; //human readable message for each res
	private Object data; //opt payload like the list of records or sum
	public Response(boolean success, String message, Object data) {
		super();
		this.success = success;
		this.message = message;
		this.data = data;
	}
	//for convenience
	public Response(boolean success, String message) {
		this(success, message, null);
	}
	public boolean isSuccess() {
		return success;
	}
	public String getMessage() {
		return message;
	}
	public Object getData() {
		return data;
	}
	
}
