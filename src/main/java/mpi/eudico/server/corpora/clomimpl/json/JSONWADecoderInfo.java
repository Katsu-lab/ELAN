package mpi.eudico.server.corpora.clomimpl.json;

import mpi.eudico.server.corpora.clom.DecoderInfo;

/**
 * Decoder object containing information for the interpretation of a 
 * (WebAnnotation) JSON file.
 * 
 * @author Han Sloetjes
 *
 */
public class JSONWADecoderInfo implements DecoderInfo {
	private String sourceFilePath;
	/* the encoding of the file */
	private String charsetName;
	
	/**
	 * Constructor.
	 */
	public JSONWADecoderInfo() {
	}
	
	/**
	 * Constructor with the source file as parameter.
	 * 
	 * @param sourceFilePath the path of the file as a string
	 */
	public JSONWADecoderInfo(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}

	@Override
	public String getSourceFilePath() {
		return sourceFilePath;
	}
	
	public void setSourceFilePath(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}
	
	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}



}
