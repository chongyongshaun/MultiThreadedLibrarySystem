package library.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import library.models.LibraryRecord;
import library.models.RecordStatus;
import library.models.RecordType;
import library.utils.FileUtil;

public class RecordManager {
	private final Map<String, LibraryRecord> recordsById; //key:val = id : record
    private final String persistPath; //file path to persist records
	public RecordManager(String persistPath) {
		super();
		this.recordsById = new ConcurrentHashMap<String, LibraryRecord>();
		this.persistPath = persistPath;
		
		try {
			Optional<Object> loaded = FileUtil.loadObjectFromFile(persistPath);
			//check if file exists
			if (!loaded.isPresent()) {
                System.out.println("No persisted record data found. Starting fresh.");
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
                if (o instanceof LibraryRecord) {
                    LibraryRecord r = (LibraryRecord) o;
                    recordsById.put(r.getRecordId(), r);
                    loadedCount++;
                }
            }            
            System.out.println("Successfully loaded " + loadedCount + " records.");
            
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Unable to load records due to file/serialization error: " + e.getMessage());
		}
	}
    
	//create new record and persist
    public synchronized LibraryRecord createRecord(RecordType type, String studentId) {
        LibraryRecord rec = new LibraryRecord(type, studentId); 
        recordsById.put(rec.getRecordId(), rec); //add to map
        persist(); //write to .ser
        return rec; //return created record
    }

    //get all records as a list, it's a copy 
    public synchronized List<LibraryRecord> getAllRecords() {
        return new ArrayList<>(recordsById.values());
    }

    //assign a borrow request to a librarian and update status.
    public synchronized boolean assignRecord(String recordId, String librarianStudentId) {
        LibraryRecord r = recordsById.get(recordId); // lookup
        if (r == null) return false; // not found
        // Only assign if request and not already borrowed
        if (r.getRecordType() == RecordType.BORROW_REQUEST && r.getStatus() == RecordStatus.REQUESTED) {
            r.setAssignedLibrarianId(librarianStudentId); // assign
            r.setStatus(RecordStatus.BORROWED);           // example update
            persist();
            return true;
        }
        return false;
    }

    //get records assigned to a specific librarian
    public synchronized List<LibraryRecord> getRecordsAssignedTo(String librarianStudentId) {
        List<LibraryRecord> out = new ArrayList<>();
        for (LibraryRecord r : recordsById.values()) {
            if (librarianStudentId.equals(r.getAssignedLibrarianId())) {
                out.add(r);
            }
        }
        return out;
    }

    //helper func like in usermanager to write to file
    private void persist() {
		List<LibraryRecord> recordList = new ArrayList<>(recordsById.values());
		try {
			FileUtil.saveObjectToFile(recordList, persistPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Failed to persist records: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
