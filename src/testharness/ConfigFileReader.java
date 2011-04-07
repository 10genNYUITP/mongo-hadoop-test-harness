
import java.io.IOException;
import java.io.Reader;
import javax.xml.parsers.*;
import org.apache.hadoop.util.Tool;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import java.util.*;
/**
    //config looks like:

{@literal <config>
     <testparams>
 * <property name="mongo.splits.use-chunks">  <val value="true"/><val value="false"></property>
 * <property name="mongo.splits.use-shards">  <val value="true"/><val value="false"></property>
 * <property name="mongo.splits.slaveok"><when property="use-shards" val="true">  <val value="true"/><val value="false"></property>
 * </testparams>
 * <test class="WordCount" args=""></test>
 * <test class="WordCount" args="--use-query"></test>
 * }
 */
class ConfigFileReader extends org.xml.sax.ext.DefaultHandler2 {

	PropertyCycle propertyCycle;
	private XMLReader parser;
	//List<TestCase> testcases = new List<TestCase>();
	Map<String, TestCase> testcases = new HashMap<String, TestCase>();
	String dbname;
	int dbport;
	String binpath;
	String testName; 
	List<String> args = new ArrayList<String>();

	class PropertyHandler extends org.xml.sax.ext.DefaultHandler2{
		ContentHandler parent ;
		PropertyCycle pc = null;
		int depth = 0;
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			depth++;
			if(depth == 1){
				pc = new PropertyCycle( attributes.getValue("name"));
			} else if ("val".equals(qName)){
				pc.addValue(attributes.getValue("value"));
			} else if ("when".equals(qName)){
				PropertyHandler ph = new PropertyHandler();
				ph.parent = this;
				parser.setContentHandler(ph);
				ph.startElement(uri, localName, qName, attributes);
				pc.setWhen(ph.pc);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (--depth == 0)
				parser.setContentHandler(parent); //return control to parent
		}
	}

	ConfigFileReader() {
		
	}

	static ConfigFileReader parse(Reader r) throws SAXException, ParserConfigurationException, IOException{
		XMLReader xr = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
		xr.setFeature("http://xml.org/sax/features/validation", false);
		xr.setFeature("http://apache.org/xml/features/validation/schema/augment-psvi", false);
		ConfigFileReader cfr = new ConfigFileReader();	// Why is cfr.parser needed at all?
		cfr.parser = xr;
                xr.setContentHandler(cfr);
		xr.parse(new InputSource(r));
        return cfr;
	}


	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}


	@Override	// is this invoked automatically? Where and when are the parameters for startElement set? -> checks if propert is there in the xml file
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if ("property".equals(qName)){
			PropertyHandler ph = new PropertyHandler();
			ph.parent = this;
			parser.setContentHandler(ph);
			ph.startElement(uri, localName, qName, atts);
			if (propertyCycle == null)
				propertyCycle = ph.pc;
			else
				propertyCycle.add(ph.pc);
		}
		else if ("test".equals(qName)) {
				String argument = atts.getValue("args");
				StringTokenizer stz = new StringTokenizer(argument);
				while(stz.hasMoreTokens()) {
					args.add(stz.nextToken());
				}
				testName = atts.getValue("class");
            try {
				TestCase tc = new TestCase((Tool) Class.forName(testName).newInstance(), (String[]) args.toArray(new String[args.size()]));
				testcases.put(testName, tc);
            } catch (Exception ex) {
                System.err.println("Could not instantiate '"+testName+"', caught "+ex.getClass().getName()+ex.getMessage());
            }
		} 
		else if ("dbprops".equals(qName)) {
			dbname = atts.getValue("dbname");
			dbport = Integer.parseInt(atts.getValue("port"));
			binpath = atts.getValue("path");
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}
}
