
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

class ConfigFileReader extends org.xml.sax.ext.DefaultHandler2 {

	PropertyCycle propertyCycle;
	private XMLReader parser;
	//List<TestCase> testcases = new List<TestCase>();
	Map<String, TestCase> testcases = new HashMap<String, TestCase>();
	TestCase current_testcase;
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
		ConfigFileReader cfr = new ConfigFileReader();
		cfr.parser = xr;
        xr.setContentHandler(cfr);
		xr.parse(new InputSource(r));
        return cfr;
	}


	@Override
	public void startDocument() throws SAXException {
		System.out.println("Strart document");
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("End document");
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if ("property".equals(qName)){
			if (current_testcase == null){
				PropertyHandler ph = new PropertyHandler();
				ph.parent = this;
				parser.setContentHandler(ph);
				ph.startElement(uri, localName, qName, atts);
				if (propertyCycle == null)
					propertyCycle = ph.pc;
				else
					propertyCycle.add(ph.pc);
			} else {
				System.out.println(atts.getValue("name") + atts.getValue("val"));
				current_testcase.setProperty(atts.getValue("name"), atts.getValue("val"));
			}
		}
		else if ("test".equals(qName)) {
				String argument = atts.getValue("args");
				StringTokenizer stz = new StringTokenizer(argument);
				while(stz.hasMoreTokens()) {
					args.add(stz.nextToken());
				}
				testName = atts.getValue("class");
            try {
            	Object o = Class.forName(testName).newInstance();
            	System.out.println("Class "+o.getClass().getName()+" is a Tool: "+ (o instanceof Tool ));
		TestCase tc = new TestCase((Tool) Class.forName(testName).newInstance(), (String[]) args.toArray(new String[args.size()]));
		testcases.put(testName, tc);
		current_testcase = tc;
            } catch (Exception ex) {
            	ex.printStackTrace();
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

