
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
       getLevenshteinDistance algorithm by Chas Emerick
       (see http://www.merriampark.com/ldjava.htm)
       
    **/
    //TODO: replace this with a call to the Apache Commons library (which should be the same implementation)
    public static int getLevenshteinDistance (String s, String t) {
	if (s == null || t == null) {
	    throw new IllegalArgumentException("Strings must not be null");
	}
	
	/*
	  The difference between this impl. and the previous is that, rather 
	  than creating and retaining a matrix of size s.length()+1 by t.length()+1, 
	  we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
	  is the 'current working' distance array that maintains the newest distance cost
	  counts as we iterate through the characters of String s.  Each time we increment
	  the index of String t we are comparing, d is copied to p, the second int[].  Doing so
	  allows us to retain the previous cost counts as required by the algorithm (taking 
	  the minimum of the cost count to the left, up one, and diagonally up and to the left
	  of the current cost count being calculated).  (Note that the arrays aren't really 
	  copied anymore, just switched...this is clearly much better than cloning an array 
	  or doing a System.arraycopy() each time  through the outer loop.)
	  
	  Effectively, the difference between the two implementations is this one does not 
	  cause an out of memory condition when calculating the LD over two very large strings.  		
	*/		
	
	int n = s.length(); // length of s
	int m = t.length(); // length of t
	
	if (n == 0) {
	    return m;
	} else if (m == 0) {
	    return n;
	}
	
	int p[] = new int[n+1]; //'previous' cost array, horizontally
	int d[] = new int[n+1]; // cost array, horizontally
	int _d[]; //placeholder to assist in swapping p and d
	
	// indexes into strings s and t
	int i; // iterates through s
	int j; // iterates through t
	
	char t_j; // jth character of t
	
	int cost; // cost
	
	for (i = 0; i<=n; i++) {
	    p[i] = i;
	}
	
	for (j = 1; j<=m; j++) {
	    t_j = t.charAt(j-1);
	    d[0] = j;
	    
	    for (i=1; i<=n; i++) {
		cost = s.charAt(i-1)==t_j ? 0 : 1;
		// minimum of cell to the left+1, to the top+1, diagonally left and up +cost				
		d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+cost);  
	    }
	    
	    // copy current distance counts to 'previous row' distance counts
	    _d = p;
	    p = d;
	    d = _d;
	} 
	
	// our last action in the above loop was to switch d and p, so p now 
	// actually has the most recent cost counts
	return p[n];
    }


    
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
	String doctitle = nl.item(0).getTextContent();
	System.out.println("doctitle " + doctitle);
	
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
	String authtitle = nl.item(0).getTextContent();
	System.out.println("authtitle " + authtitle);
	
	// match fields, and create the output XML format
	int distance = getLevenshteinDistance(doctitle, authtitle);
	System.out.println("distance = " + distance);
	double match = 1.0 - ((double)distance / (double)(Math.max(doctitle.length(),authtitle.length())));
	
	System.out.println("match of doctitle and authtitle is " + match);
	
	// render the XML into pretty HTML

	// display
	
    }
}
