package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information needed to construct a ControlledVocabulary object
 * 
 * @author Micha Hulsbosch
 * @version jul 2010
 */
public class CVRecord {
	private String cv_id;
	private String description;		/** for old, single language CVs only.*/
	private String extRefId;
	private ArrayList<CVEntryRecord> entries;
	private List<CVDescriptionRecord> descriptions;
	
	/**
	 * Construct an empty CVRecord, sets cv_id
	 * 
	 * @param cv_id the id
	 */
	public CVRecord(String cv_id) {
		setCv_id(cv_id);
		description = null;
		extRefId = null;
		entries = new ArrayList<CVEntryRecord>();
		descriptions = new ArrayList<CVDescriptionRecord>();
	}
	
	/**
	 * @return a boolean saying whether this record has content
	 */
	public boolean hasContents() {
		if(description != null && !description.isEmpty()) {
			return true;
		}
		if(entries.size() > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return the cv_id
	 */
	public String getCv_id() {
		return cv_id;
	}
	/**
	 * @param cvId the cv_id to set
	 */
	public void setCv_id(String cvId) {
		cv_id = cvId;
	}
	/**
	 * @return the description, for old, single language CVs only.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set.
	 * For old, single language CVs only.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the extRefId
	 */
	public String getExtRefId() {
		return extRefId;
	}
	/**
	 * @param extRefId the extRefId to set
	 */
	public void setExtRefId(String extRefId) {
		this.extRefId = extRefId;
	}
	/**
	 * @return the entries
	 */
	public ArrayList<CVEntryRecord> getEntries() {
		return entries;
	}
	/**
	 * @param entries the entries to set
	 */
	public void setEntries(ArrayList<CVEntryRecord> entries) {
		this.entries = entries;
	}
	
	/**
	 * @param cvEntryRecord the entry record to add
	 */
	public void addEntry(CVEntryRecord cvEntryRecord) {
		entries.add(cvEntryRecord);
	}
	
	/**
	 * @param cvEntryRecord the entry record to remove
	 */
	public void removeEntry(CVEntryRecord cvEntryRecord) {
		entries.remove(cvEntryRecord);
	}
	/**
	 * @return the descriptions
	 */
	public List<CVDescriptionRecord> getDescriptions() {
		return descriptions;
	}
	/**
	 * @param cvDescriptionRecord add a description record
	 */
	public void addDescription(CVDescriptionRecord cvDescriptionRecord) {
		descriptions.add(cvDescriptionRecord);
	}
}
