package library.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import library.models.User;
import library.utils.FileUtil;

public class UserManager {
	//use hashmap for easy lookup, no duplicates like leetcode stuff
	private final Map<String, User> usersByEmail;   // key : val = email : user
    private final Map<String, User> usersByStudentId; // key: val = st id : user
    private final String persistPath;               // file path to store users
    
	public UserManager(String persistPath) {
		super();
		this.usersByEmail = new ConcurrentHashMap<String, User>();
		this.usersByStudentId = new ConcurrentHashMap<String, User>();
		this.persistPath = persistPath;
		//try to load save state from .ser
		try {
			Optional<Object> loaded = FileUtil.loadObjectFromFile(persistPath);
			//check if file exists
			if (!loaded.isPresent()) {
                System.out.println("No persisted user data found. Starting fresh.");
                return;
            }
			Object obj = loaded.get();
			//check if loaded obj is a list as expected
			if (!(obj instanceof List)) {
                System.err.println("Persisted file is not a List. Starting fresh.");
                return;
            }
			List<?> list = (List<?>) obj;
            int loadedCount = 0;
            for (Object o : list) {
                if (o instanceof User) {
                    User u = (User) o;
                    usersByEmail.put(u.getEmail(), u);
                    usersByStudentId.put(u.getStudentId(), u);
                    loadedCount++;
                }
            }            
            System.out.println("Successfully loaded " + loadedCount + " users.");
            
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Unable to load users due to file/serialization error: " + e.getMessage());
		}
	}
    
	public synchronized boolean register(User user) {
		if (usersByEmail.containsKey(user.getEmail()) || usersByStudentId.containsKey(user.getStudentId())) {
			return false;
		}
		usersByEmail.put(user.getEmail(), user);
		usersByStudentId.put(user.getStudentId(), user);
		persist();
		return true;
	}
	
	public synchronized User authenticate(String email, String password) {
        User u = usersByEmail.get(email); //find by email
        if (u != null && u.getPassword().equals(password)) {
            return u; //success
        }
        return null; //fail
    }

    public synchronized boolean updatePassword(String email, String newPassword) {
        User u = usersByEmail.get(email);
        if (u == null) return false;
        u.setPassword(newPassword);
        persist();
        return true;
    }

    // return copy of list to avoid exposing internal map
    public synchronized List<User> getAllUsers() {
        return new ArrayList<>(usersByEmail.values());
    }

	private void persist() {
		List<User> userList = new ArrayList<User>(usersByEmail.values());
		try {
			FileUtil.saveObjectToFile(userList, persistPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Failed to persist users: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
}
