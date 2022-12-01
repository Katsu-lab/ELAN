package mpi.eudico.server.corpora.clomimpl.json;

/**
 * Defines constants for parsing and creating WebAnnotation JSON files.
 */
public class WAConstants {
	public static final String WA_CONTEXT        = "http://www.w3.org/ns/anno.jsonld";
	public static final String LDP_CONTEXT       = "http://www.w3.org/ns/ldp.jsonld";
	public static final String MEDIA_SELECTOR    = "http://www.w3.org/TR/media-frags/";
	public static final String TEXT_FORMAT       = "text/plain";
	public static final String HTML_FORMAT       = "text/html";
	public static final String CONTEXT           = "@context";
	public static final String ID                = "id";
	public static final String TYPE              = "type";
	public static final String LABEL             = "label";
	public static final String CREATOR           = "creator";
	public static final String NICKNAME          = "nickname";// ?? no place in ELAN data model
	public static final String EMAIL_SHA         = "email_sha1";// ?? no place
	public static final String TOTAL             = "total";
	public static final String FIRST             = "first";
	public static final String LAST              = "last";
	public static final String START_INDEX       = "startIndex";
	public static final String ITEMS             = "items";
	public static final String CREATED           = "created"; // date
	public static final String GENERATED         = "generated"; // date
	public static final String MOTIVATION        = "motivation";
	public static final String RIGHTS            = "rights";
	public static final String GENERATOR         = "generator";
	public static final String NAME              = "name";
	public static final String HOMEPAGE          = "homepage";
	public static final String BODY              = "body";
	public static final String VALUE             = "value";
	public static final String LANGUAGE          = "language";
	public static final String PURPOSE           = "purpose";
	public static final String FORMAT            = "format";
	public static final String TARGET            = "target";
	public static final String SELECTOR          = "selector";
	public static final String CONFORMS_TO       = "conformsTo";
	public static final String REFINED_BY        = "refinedBy";
	public static final String FOREGROUND_COLOR  = "foregroundColor";
	public static final String BACKGROUND_COLOR  = "backgroundColor";
	public static final String KEY               = "key";
	public static final String TRANSCRIBING		 = "transcribing";
	public static final String BASIC_CONTAINER	 = "BasicContainer";
	
	// standard values
	public static final String ANNOTATION        = "Annotation";
	public static final String ANN_COLLECTION    = "AnnotationCollection";
	public static final String ANN_PAGE          = "AnnotationPage";
	public static final String TEXTUAL_BODY      = "TextualBody";
	public static final String FRAG_SELECTOR     = "FragmentSelector";
	public static final String PART_OF           = "partOf";
	public static final String NEXT              = "next";
	public static final String BODY_VALUE        = "bodyValue";
	public static final String CHOICE            = "Choice";
	public static final String SOURCE            = "source";
	public static final String CONTAINER         = "Container";
	public static final String CONTAINS          = "contains";
	public static final String AUDIO             = "Audio";
	public static final String VIDEO             = "Video";
	//public static final String SOUND             = "Sound";
	
	// some alternative values
	public static final String AT_ID             = "@id";
	public static final String AT_TYPE           = "@type";
	public static final String TEXT              = "Text";
	public static final String UNNAMED           = "Unnamed";
	
	private WAConstants() {
	}

}
