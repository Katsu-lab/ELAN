package mpi.eudico.client.annotator.lexicon.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import mpi.eudico.client.annotator.lexicon.api.Param;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class to parse the lexiconServiceClient.cmdi.
 * 
 * @author Micha Hulsbosch
 *
 */
public class LexSrvcClntParser extends DefaultHandler {

	private InputStream inputStream;
	public ArrayList<Param> paramList;
	public String type;
	public boolean curOsSupported;
	public String name;
	public String implementor;
	public String description;

	public LexSrvcClntParser(InputStream inputStream) {
		super();
		this.inputStream = inputStream;
		paramList = new ArrayList<Param>(10);
	}
	
	public void parse()  throws SAXException {
		if (inputStream != null) {
			try {
				SAXParserFactory parserFactory = SAXParserFactory.newInstance();
				parserFactory.setNamespaceAware(true);
				parserFactory.setValidating(false);
				parserFactory.newSAXParser().parse(inputStream, this);
			} catch (IOException ioe) {
				throw new SAXException(ioe);
			} catch (ParserConfigurationException pce) {
				throw new SAXException(pce);
			} finally {
				try {
					inputStream.close();
				} catch (Throwable thr) {}
			}
		} else {
			throw new SAXException("No input stream specified");
		}
	}
	
	//############## ContentHandler methods ######################################
	String curContent = "";
	Param curParam;

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		curContent += new String(ch, start, length);
	}

	@Override
	public void startElement(String nameSpaceURI, String name,
            String rawName, Attributes attributes) throws SAXException {
		if (name.equals("lexiconserviceclient")) {
			//type = attributes.getValue("type");
			description = attributes.getValue("info");
			implementor = attributes.getValue("spiClass");
			curOsSupported = true;
			/*
			String os = System.getProperty("os.name").toLowerCase();
			if (os.indexOf("win") > -1) {
				implementor = attributes.getValue("runWin");
				curOsSupported = (implementor != null);
//				if (implementor == null) {
//					curOsSupported = false;
//					//hier... throw exception to stop parsing?
//				}
			} else if (os.indexOf("mac") > -1) {
				implementor = attributes.getValue("runMac");
				curOsSupported = (implementor != null);
//				if (implementor == null) {
//					curOsSupported = false;
//				}
			} else if (os.indexOf("linux") > -1) {
				implementor = attributes.getValue("runLinux");
				curOsSupported = (implementor != null);
//				if (implementor == null) {
//					curOsSupported = false;
//				}
			}
			*/
		} else if (name.equals("param")) {
			curParam = new Param();
			curParam.setType(attributes.getValue("type"));
		}
	}

	@Override
	public void endElement(String nameSpaceURI, String name, String rawName)
	throws SAXException {
		if (curContent != null && curContent.length() > 0) {
			if (name.equals("lexiconserviceclient")) {
				this.name = curContent.trim();
			} else if (name.equals("param")) {
				curParam.setContent(curContent.trim());
				paramList.add(curParam);
			}
		}
		
		curContent = "";
	}

}
