package mpi.eudico.server.corpora.clomimpl.type;

import mpi.eudico.server.corpora.lexicon.LexiconQueryBundle2;

/**
 * A class to store several properties that tiers of that type have in common.
 * In user interfaces etc. now referred to as "Tier Type". 
 */
public class LinguisticType {
	/** an enumeration of property keys */
	public enum PropKey {
		ID,
		NAME,
		CONSTRAINT,
		CV_NAME,
		DC,
		LEX_BUNDLE,
		LEX_LINK,
		LEX_FIELD
	};
    /** the name of the type, also the id */
    String typeName;

    /** the constraint instance, possibly composite */
    Constraint constraints; // can be composite

    /** the time alignable flag, true by default */
    boolean timeAlignable = true;

    /* HS: added jun 04 support for controlled vocabularies */
    /** the identifier of the Controlled Vocabulary in use by this tier type */
    String controlledVocabularyName;
    
    /*HS: added april 08 support for reference to a ISO Data Category. Initially a string,
     * might need an Object later */
    /** a reference to a (ISO) Data Category. Can be a simple id (when it is a category from the ISO DCR)
     * or a combination of DCR identifier + id, e.g. ISO12620#32 */
    String dataCategory;
    
	/**
	 * Holds a lexicon connection
	 */
	LexiconQueryBundle2 lexiconQueryBundle = null;

    /**
     * Creates a new LinguisticType instance
     *
     * @param theName the name of the type
     */
    public LinguisticType(String theName) {
        typeName = theName;
    }
    
    /**
     * Duplicates a linguistic type instance using a new name.
     * Note that this does not clone the CV if that's desired.
     * 
     * @param theName the (new) name of the type
     * @param orig the type to copy
     */
    public LinguisticType(String theName, LinguisticType orig) {
    	this(theName);
    	if (orig.hasConstraints()) {
    		try {
    			addConstraint(orig.getConstraints().clone());
    		}
    		catch(CloneNotSupportedException ex) {
    			ex.printStackTrace(); // can't happen
    		}
    	}
    	setTimeAlignable(orig.isTimeAlignable());
    	// The caller should clone CV but for now, use the same name
    	setControlledVocabularyName(orig.getControlledVocabularyName());
    	setDataCategory(orig.getDataCategory());
    	// add a copy of the lexicon query bundle, if any
    	if (orig.getLexiconQueryBundle() != null) {
	    	LexiconQueryBundle2 copyBundle = new LexiconQueryBundle2(orig.getLexiconQueryBundle());
	    	// the copy constructor does not copy the service client, set it separately
	    	if (copyBundle.getLink() != null) {
	    		copyBundle.getLink().setSrvcClient(orig.getLexiconQueryBundle().getLink().getSrvcClient());
	    	}
	    	setLexiconQueryBundle(copyBundle);
    	}
    }

    /**
     *
     * @return a description string
     */
    @Override
	public String toString() {
        String stereotype = "";

        if (constraints != null) {
            stereotype = Constraint.stereoTypes[constraints.getStereoType()];
        }

        return typeName + ", " + timeAlignable + ", " + stereotype;
    }

    /**
     * @return the name of the type
     */
    public String getLinguisticTypeName() {
        return typeName;
    }

    /**
     * @param theName the new name of the type
     */
    public void setLinguisticTypeName(String theName) {
        typeName = theName;
    }

    /**
     * If a type returns false, no constraints, it is or should be used by a
     * root tier.
     *
     * @return true if the type has a {@code Constraint} object set, false 
     * otherwise
     */
    public boolean hasConstraints() {
        if (constraints != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the {@code Constraint} object or {@code null}
     */
    public Constraint getConstraints() {
        return constraints;
    }

    /**
     * @param theConstraint the constraint to set or to add to the current constraint
     */
    public void addConstraint(Constraint theConstraint) {
        if (constraints == null) {
            constraints = theConstraint;
        } else {
            constraints.addConstraint(theConstraint);
        }
    }

    /**
     * Removes all constraints, sets it to {@code null}
     */
    public void removeConstraints() {
        constraints = null;
    }

    /**
     *
     * @return true if this type is time alignable, false otherwise, 
     * should be consistent with the constraints
     */
    public boolean isTimeAlignable() {
        return timeAlignable;
    }

    /**
     * Sets the time alignable flag. This flag should in fact be derived
     * from the constraint object but doesn't currently.
     * 
     * @param isTimeAlignable the new time alignable flag
     */
    public void setTimeAlignable(boolean isTimeAlignable) {
        timeAlignable = isTimeAlignable;
    }

    /**
     * Returns whether or not annotation values on tiers using this LinguisticType 
     * are restricted by a ControlledVocabulary.<br>
     * Current implementation is very loose; the value returned only depends 
     * on the presence of a non-null reference string. Could be more strict 
     * by keeping a flag independent from a reference string or object reference.
     * 
     * @return true if there is a reference to a ControlledVocabulary
     */
    public boolean isUsingControlledVocabulary() {
    	return (controlledVocabularyName == null || 
    		controlledVocabularyName.length() == 0) ? false : true;
    }
    
    /**
     * Returns the name (identifier) of the ControlledVocabulary in use by 
     * this type.<br>
     * The actual CV objects are stored in and managed by the Transcription.
     * (Candidate for change: might store a reference to the CV object itself 
     * instead of using a reference to it.)
     * 
     * @return the name/identifier of the cv
     */
    public String getControlledVocabularyName() {
    	return controlledVocabularyName;
    }
    
    /**
     * Sets the name of the ControlledVocabulary to be used by this type.
     * 
     * @see #getControlledVocabularyName()
     * @param name the name/identifier of the cv
     */
    public void setControlledVocabularyName(String name) {
    	controlledVocabularyName = name;
    }
    
    /**
     * Returns the Data Category reference (the identifier).
     * 
     * @return the Data Category reference
     */
	public String getDataCategory() {
		return dataCategory;
	}
	
	/**
	 * Sets the Data Category reference.
	 * 
	 * @param dataCategory the identifier of the data category
	 */
	public void setDataCategory(String dataCategory) {
		this.dataCategory = dataCategory;
	}
	
	/**
	 * @return true if there is a reference to lexicon service 
	 * @author Micha Hulsbosch
	 */
	public boolean isUsingLexiconQueryBundle() {
		return (lexiconQueryBundle == null) ? false : true;
	}
	
	/**
	 * @return the lexiconQueryBundle
	 * @author Micha Hulsbosch
	 */
	public LexiconQueryBundle2 getLexiconQueryBundle() {
		return lexiconQueryBundle;
	}

	/**
	 * @param lexiconQueryBundle the lexiconQueryBundle to set
	 * @author Micha Hulsbosch
	 */
	public void setLexiconQueryBundle(LexiconQueryBundle2 lexiconQueryBundle) {
		this.lexiconQueryBundle = lexiconQueryBundle;
	}
    
	/**
	 * Overrides <code>Object</code>'s equals method by checking all  fields of
	 * the other object to be equal to all fields in this  object.
	 *
	 * @param obj the reference object with which to compare
	 *
	 * @return true if this object is the same as the obj argument; false
	 *         otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			// null is never equal
			return false;
		}

		if (obj == this) {
			// same object reference 
			return true;
		}

		if (!(obj instanceof LinguisticType)) {
			// it should be a LinguisticType object
			return false;
		}
		
		LinguisticType other = (LinguisticType) obj;
		
		if (typeName != null && !typeName.equals(other.getLinguisticTypeName())) {
			return false;
		} else if (other.getLinguisticTypeName() != null && 
			!other.getLinguisticTypeName().equals(typeName)) {
			return false;
		}
		
		if (isTimeAlignable() != other.isTimeAlignable()) {
			return false;
		}
		
		if (hasConstraints() != other.hasConstraints()) {
			return false;
		} else {
			if (hasConstraints() && !getConstraints().equals(other.getConstraints())) {
				return false;
			}
		}
		
		if (isUsingControlledVocabulary()) {
			if (!other.isUsingControlledVocabulary()) {
				return false;
			} else {
				// can compare CV only by their name
				if ( !controlledVocabularyName.equals(other.getControlledVocabularyName()) ) {
					return false;
				}
			}				
		} else {
			if (other.isUsingControlledVocabulary()) {
				return false;
			}
		}
		
		if (dataCategory == null) {
			if (other.getDataCategory() != null) {
				return false;
			}
		} else if (!dataCategory.equals(other.getDataCategory())) {
			return false;
		}
		
		if (lexiconQueryBundle == null) {
			if (other.getLexiconQueryBundle() != null) {
				return false;
			} else {
				// both null, return true
			}
		} else {
			if (other.getLexiconQueryBundle() == null) {
				return false;
			} else {
				// compare the lexiconQueryBundle (by name)?
				if (!lexiconQueryBundle.equals(other.getLexiconQueryBundle())) {
					return false;
				}
			}
		}
		
		return true;
	}

}
