package nl.asymmetrics.droidshows.thetvdb.utils;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler{

    private List<String> XMLData = new ArrayList<String>();
    private boolean merge = false;

    public List<String> getParsedData() {
        return this.XMLData;
    }

    // ===========================================================
    // Methods to work with the parser
    // ===========================================================
    @Override
    public void startDocument() throws SAXException {
        this.XMLData = new ArrayList<String>();
    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    /** Gets be called on opening tags like:
     * <tag>
     * Can provide attribute(s), when xml was like:
     * <tag attribute="attributeValue">*/
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        this.merge = false;
        this.XMLData.add("<" + localName + ">");
    }

    /** Gets be called on closing tags like:
     * </tag> */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
    	if (this.merge)
    		this.XMLData.set(this.XMLData.size()-1, this.XMLData.get(this.XMLData.size()-1).trim());
        this.merge = false;
    	this.XMLData.add("</" + localName + ">");
    }

    /** Gets be called on the following structure:
     * <tag>characters</tag> */
    @Override
    public void characters(char ch[], int start, int length) {
        String tmp = new String(ch, start, length);
    	if (this.merge) {
    		this.XMLData.set(this.XMLData.size()-1, this.XMLData.get(this.XMLData.size()-1) + tmp);
    	} else {
    		this.XMLData.add(tmp);
    	}
    	this.merge = true;
    }
}
