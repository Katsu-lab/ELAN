package mpi.eudico.server.corpora.clom;

import java.util.Set;

/**
 * RefLink is the common interface for the {@code GROUP_REF_LINK} and 
 * {@code CROSS_REF_LINK} elements in the EAF file.
 * <p>
 * These elements link together Annotations, either one-on-one or in groups.
 * <p>
 * Implementation note:
 * don't override hash() or equals(), since we want RefLinks to be in
 * HashSets based on their reference value only.
 * 
 * @author olasei
 */

public interface RefLink {

	/**
	 * @return the ID of the reference link itself.
	 */
	public String getId();
	
	/**
	 * @return a name of the reference link
	 */
	public String getRefName();

	/**
	 * @return any External Reference that there may be
	 */
	public ExternalReference getExtRef();

	/**
	 * @return the language the Controlled Vocabulary Entry is in
	 */
	public String getLangRef();

	/**
	 * @return a Controlled Vocabulary Entry
	 */
	public String getCveRef();

	/**
	 * @return what sort of reference is this, the type of reference
	 */
	public String getRefType();

	/**
	 * @return the text inside the element (may be removed from the schema)
	 */
	public String getContent();
	
	/**
	 * @param ids a collection of id's to check
	 * @return {@code true} if this RefLink refers in some way to any of the given ids.
	 */
	public boolean references(Set<String> ids);
	
	/**
	 * @return converts the RefLink to some readable representation.
	 */
	@Override
	public String toString();
}
