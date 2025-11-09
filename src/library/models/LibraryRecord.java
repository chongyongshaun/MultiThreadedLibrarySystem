package library.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class LibraryRecord implements Serializable {
	private static final long serialVersionUID = 1L; 

    private String recordId;                     
    private RecordType recordType;                   
    private LocalDate date;                    
    private String studentId;                      
    private RecordStatus status;              
    private String assignedLibrarianId;
	public LibraryRecord(RecordType recordType, String studentId) {
		super();
		this.recordId = UUID.randomUUID().toString(); //gen uniq id
		this.recordType = recordType;
		this.date = LocalDate.now();
		this.studentId = studentId;
		//set initial status based on type
        if (recordType == RecordType.NEW_BOOK_ENTRY) {
            this.status = RecordStatus.AVAILABLE; //new book always available by def
        } else {
            this.status = RecordStatus.REQUESTED; //borrow request starts as requested
        }
		this.assignedLibrarianId = null; //init empty
	}
	public String getRecordId() {
		return recordId;
	}
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	public RecordType getRecordType() {
		return recordType;
	}
	public void setRecordType(RecordType recordType) {
		this.recordType = recordType;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public String getStudentId() {
		return studentId;
	}
	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}
	public RecordStatus getStatus() {
		return status;
	}
	public void setStatus(RecordStatus status) {
		this.status = status;
	}
	public String getAssignedLibrarianId() {
		return assignedLibrarianId;
	}
	public void setAssignedLibrarianId(String assignedLibrarianId) {
		this.assignedLibrarianId = assignedLibrarianId;
	}
	@Override
	public String toString() {
		return "LibraryRecord [recordId=" + recordId + ", recordType=" + recordType + ", date=" + date + ", studentId="
				+ studentId + ", status=" + status + ", assignedLibrarianId=" + assignedLibrarianId + "]";
	}
    
    
}
