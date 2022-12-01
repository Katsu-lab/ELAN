package mpi.eudico.server.corpora.lexicon;

/**
 * Exception thrown by LexiconServiceClient methods
 * @author Micha Hulsbosch
 *
 */
@SuppressWarnings("serial")
public class LexiconServiceClientException extends Exception {
	public static final String NO_USERNAME_OR_PASSWORD = "No username or password";
	public static final String MALFORMED_URL = "Malformed Url";
	public static final String CLIENT_MALFUNCTION = "Client malfunction";
	public static final String INCORRECT_USERNAME_OR_PASSWORD = "Incorrect username or password";
	public static final String CONNECTION_MALFUNCTION = "CONNECTION_MALFUNCTION";

	public LexiconServiceClientException() {
	}

	public LexiconServiceClientException(String message) {
		super(message);
	}

	public LexiconServiceClientException(Throwable cause) {
		super(cause);
	}

	public LexiconServiceClientException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * @return the key to use for looking up a localized message in a 
	 * resource bundle, or null 
	 */
	public String getMessageLocaleKey() {
		if(getMessage().equals(MALFORMED_URL)) {
			return "LexiconServiceClientException.MalformedUrl";
		} else if(getMessage().equals(CLIENT_MALFUNCTION)) {
			return "LexiconServiceClientException.ClientMalfunction";
		} else if(getMessage().equals(CONNECTION_MALFUNCTION)) {
			return "LexiconServiceClientException.ConnectionMalfunction";
		} else if(getMessage().equals(LexiconServiceClientException.INCORRECT_USERNAME_OR_PASSWORD)) {
			return "LexiconServiceClientException.IncorrectUsernameOrPassword";
		}	
		return null;
	}
}
