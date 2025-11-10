package library.models;

import java.io.Serializable;

//wrap the request in an object instead of sending scanner scaned string directly
public class Request implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private RequestAction action; //req type kinda like GET POST etc.
	private Object data; //optional payload
	public Request(RequestAction action, Object data) {
		super();
		this.action = action;
		this.data = data;
	}
	//no need for setters
	public RequestAction getAction() {
		return action;
	}
	public Object getData() {
		return data;
	}
	
}
