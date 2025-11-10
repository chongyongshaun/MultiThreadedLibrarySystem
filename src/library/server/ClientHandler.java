package library.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import library.models.LibraryRecord;
import library.models.LoginData;
import library.models.RecordType;
import library.models.Request;
import library.models.RequestAction;
import library.models.Response;
import library.models.User;
import library.models.UserRole;

public class ClientHandler implements Runnable{
	private final Socket socket;           // client socket
    private final UserManager userManager; // shared user manager
    private final RecordManager recordManager; // shared record manager
    private User authenticatedUser = null; // holds the authenticated user for session
    
    
	public ClientHandler(Socket socket, UserManager userManager, RecordManager recordManager) {
		super();
		this.socket = socket;
		this.userManager = userManager;
		this.recordManager = recordManager;
	}


	@Override
	public void run() {
		try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
	         ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
			while (true) {
                Object obj = ois.readObject(); //read Request from client
                if (!(obj instanceof Request)) { //validate type
                    oos.writeObject(new Response(false, "Invalid request object"));
                    oos.flush();
                    continue;
                }
                
                Request req = (Request) obj;
                RequestAction action = req.getAction();
                //handle all actions
                switch (action) {
                case REGISTER:
                	//expect User as payload
                	Object uobj = req.getData();
                	if (uobj instanceof User) {
                		User user = (User) uobj;
                		boolean ok = userManager.register(user);
                		if (ok) {
                			oos.writeObject(new Response(true, "Registration Succesful"));
                		} else {
                			oos.writeObject(new Response(false, "Email or Student ID already exists"));
                		}
                	} else {
                		oos.writeObject(new Response(false, "Malformed register data"));
                	}
                	oos.flush();
                	break;
                case LOGIN:
                	//expect LoginData
                	 Object loginObj = req.getData();
                     if (loginObj instanceof LoginData) {
                         LoginData ld = (LoginData) loginObj;
                         User u = userManager.authenticate(ld.getEmail(), ld.getPassword());
                         if (u != null) {
                             this.authenticatedUser = u; //set session user
                             oos.writeObject(new Response(true, "Login successful", u));
                         } else {
                             oos.writeObject(new Response(false, "Invalid credentials"));
                         }
                     } else {
                         oos.writeObject(new Response(false, "Malformed login data"));
                     }
                     oos.flush();
                     break;
                case CREATE_RECORD:
                	if (!isAuthenticated(oos)) break; //ensure auth-ed
                	//expect recordtype enum or string
                	Object rObj = req.getData();
                    RecordType rt = null;
                    if (rObj instanceof RecordType) {
                        rt = (RecordType) rObj;
                    } else if (rObj instanceof String) {
                        try {
                            rt = RecordType.valueOf((String) rObj);
                        } catch (IllegalArgumentException e) { 
                        	rt = null; 
                        }
                    }
                    if (rt == null) {
                        oos.writeObject(new Response(false, "Invalid record type"));
                        oos.flush();
                        break;
                    }
                    LibraryRecord created = recordManager.createRecord(rt, authenticatedUser.getStudentId());
                    oos.writeObject(new Response(true, "Record created", created));
                    oos.flush();
                    break;
                case GET_ALL_RECORDS:
                	if (!isAuthenticated(oos)) break;
                	var all = recordManager.getAllRecords();
                	oos.writeObject(new Response(true, "All records", all));
                	oos.flush();
                	break;
                case ASSIGN_RECORD:
                	if (!isAuthenticated(oos)) break;
                    //only librarian can do this so check role
                    if (authenticatedUser.getRole() != UserRole.LIBRARIAN) {
                        oos.writeObject(new Response(false, "Only librarians can assign records"));
                        oos.flush();
                        break;
                    }
                    Object idObj = req.getData();
                    if (!(idObj instanceof String)) {
                        oos.writeObject(new Response(false, "Expected recordId string"));
                        oos.flush();
                        break;
                    }
                    String recordId = (String) idObj;
                    boolean assigned = recordManager.assignRecord(recordId, authenticatedUser.getStudentId());
                    if (assigned) {
                        oos.writeObject(new Response(true, "Record assigned"));
                    } else {
                        oos.writeObject(new Response(false, "Failed to assign record (check type/status/ID)"));
                    }
                    oos.flush();
                    break;
                case UPDATE_PASSWORD:
                	if (!isAuthenticated(oos)) break;
                    Object passObj = req.getData();
                    if (!(passObj instanceof String)) {
                        oos.writeObject(new Response(false, "Expected new password string"));
                        oos.flush();
                        break;
                    }
                    String newPass = (String) passObj;
                    boolean updated = userManager.updatePassword(authenticatedUser.getEmail(), newPass);
                    if (updated) {
                        oos.writeObject(new Response(true, "Password updated"));
                    } else {
                        oos.writeObject(new Response(false, "Password update failed"));
                    }
                    oos.flush();
                    break;				
				case GET_ALL_USERS:
					if (!isAuthenticated(oos)) break;                    
                    var users = userManager.getAllUsers();
                    oos.writeObject(new Response(true, "Users list", users));
                    oos.flush();                    
					break;
				case GET_ASSIGNED_RECORDS:
					if (!isAuthenticated(oos)) break;
                    if (authenticatedUser.getRole() != UserRole.LIBRARIAN) {
                        oos.writeObject(new Response(false, "Only librarians can view assigned records"));
                        oos.flush();
                        break;
                    }
                    var assignedList = recordManager.getRecordsAssignedTo(authenticatedUser.getStudentId());
                    oos.writeObject(new Response(true, "Assigned records", assignedList));
                    oos.flush();
                    break;
				case LOGOUT:
					this.authenticatedUser = null; // clear session
                    oos.writeObject(new Response(true, "Logged out"));
                    oos.flush();
					break;
				case EXIT:
					oos.writeObject(new Response(true, "Goodbye"));
                    oos.flush();
                    socket.close(); //close socket which will break loop
                    return; //end run()
				default:
					oos.writeObject(new Response(false, "Unknown action: " + action));
                    oos.flush();
					break;
                
                }
			}
		} catch (EOFException eof) {
            //client closed connection cleanly
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client handler error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
		
	}
	
	//helper func to check authentication and send failure response if not.
    private boolean isAuthenticated(ObjectOutputStream oos) throws IOException {
        if (authenticatedUser == null) {
            oos.writeObject(new Response(false, "Authentication required"));
            oos.flush();
            return false;
        }
        return true;
    }
}
