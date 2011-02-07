
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.net.URL;

class SimpleHamr extends Object {

    public static final String OAI_BASE = "http://www.datadryad.org/oai/request?verb=GetRecord&identifier=oai:datadryad.org:";
    public static final String OAI_APPEND = "&metadataPrefix=oai_dc";
    private static final String CROSSREF_URL = "http://api.labs.crossref.org/";


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
       Takes a single command line argument, which is the ID of a Dryad item to retrieve.

       For example, java SimpleHamr "10255/dryad.20"
     **/
    public static void main(String[] args) throws Exception {
	// get a Dryad ID
	String itemID = args[0];
	System.out.println("processing " + itemID);

	// retrieve the object from Dryad (in OAI_DC format)
	String accessURL = OAI_BASE + itemID + OAI_APPEND;
	System.out.println("retrieving " + accessURL);
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder db = dbf.newDocumentBuilder();
	Document doc = db.parse(new URL(accessURL).openStream());

	NodeList nl = doc.getElementsByTagName("dc:title");
	System.out.println("retrieved " + nl.item(0).getTextContent());
	
	// extract its DOI
	nl = doc.getElementsByTagName("dc:relation");
	String targetDOI = null;
	for(int i=0; i < nl.getLength(); i++){
	    String rel = nl.item(i).getTextContent();
	    System.out.println("relation " + rel);
	    if (rel.startsWith("doi")) {
		targetDOI = rel.substring(4); // skip "doi:"
	    }
	}
	System.out.println("doi is " + targetDOI);

	// get the corresponding record from crossref
	String authURL = CROSSREF_URL + targetDOI + ".xml";
	System.out.println("retrieving " + authURL);
	DocumentBuilderFactory authdbf = DocumentBuilderFactory.newInstance();
	DocumentBuilder authdb = authdbf.newDocumentBuilder();
	Document authdoc = authdb.parse(new URL(authURL).openStream());

	nl = authdoc.getElementsByTagName("title");
	System.out.println("retrieved " + nl.item(0).getTextContent());
	
	
	// match fields, and create the output XML format

	// render the XML into pretty HTML

	// display
	
    }
}
