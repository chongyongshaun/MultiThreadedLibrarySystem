package library.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

public class FileUtil {
	
	//save an obj to a filepath overwriting it
	public static void saveObjectToFile(Object obj, String path) throws IOException{
		try (FileOutputStream fos = new FileOutputStream(path); // open file stream code from youtube brocode
	         ObjectOutputStream oos = new ObjectOutputStream(fos)) { // wrap with object stream
	            oos.writeObject(obj); //write to .ser
	            oos.flush();
		} //try with resource so i dont needa close manually
	}
	
	//same thing but reverse, returns Optional.empty if file not found
	//use optional because it's clear that Object might not exist
	public static Optional<Object> loadObjectFromFile(String path) throws IOException, ClassNotFoundException {
        File f = new File(path);    
        if (!f.exists()) { // if file missing nothing to load
            return Optional.empty();
        }
        try (FileInputStream fis = new FileInputStream(f); 
             ObjectInputStream ois = new ObjectInputStream(fis)) { 
            Object obj = ois.readObject(); 
            return Optional.of(obj); // can't be null cuz i handled it so use .of instead of ofNullable
        }
    }
}
