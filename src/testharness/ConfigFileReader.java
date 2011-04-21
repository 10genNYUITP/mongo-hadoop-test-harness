
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
	List<TestCase> testcases = new ArrayList<TestCase>();
	private TestCase current_testcase = null;
	private static String dbname;
	private static String dbout;
	private static String collection;
	private static int dbport;
	private static String binpath;
	static String testName;
	List<String> args = new ArrayList<String>();
	
	public String getBinpath() {
		return binpath;
	}
	
	public String getDBName() {
		return dbname;
	}
	
	public String getDBOut() {
		return dbout;
	}
	
	public int getDBPort() {
		return dbport;
	}
	
	public String getCollection() {
		return collection;
	}
	
	public static String getTestName() {
		return testName;
	}

	class PropertyHandler extends org.xml.sax.ext.DefaultHandler2{
		ContentHandler parent ;
		PropertyCycle pc = null;
		int depth = 0;
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			depth++;
			if(depth == 1){
				pc = new PropertyCycle(attributes.getValue("name"));
			} else if ("val".equals(qName)){
				final String value = attributes.getValue("value");
				System.out.println("value is: " + value);
                pc.addValue(value);
				String t = attributes.getValue("baseline");
                if ("true".equals(t))
                    pc.setBaseline(value);
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
		System.out.println("Start document");
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("End document, pc is: "+propertyCycle);
	}


	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if ("property".equals(qName)){
			System.out.println("PARSING PROPERTY TAG1");
			System.out.println("INSIDE PROPERTY");
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
				System.out.println("PARSING THE VALUES OF PROPERTY TAG1");
				System.out.println(atts.getValue("name") + atts.getValue("val"));
				current_testcase.setProperty(atts.getValue("name"), atts.getValue("val"));
			}
		}
		else if ("test".equals(qName)) {
			System.out.println("PARSING TEST TAG");
			System.out.println("INSIDE TEST");
			String argument = atts.getValue("args");
			StringTokenizer stz = new StringTokenizer(argument);
			while(stz.hasMoreTokens()) {
				args.add(stz.nextToken());
			}
			testName = atts.getValue("class");
			try {
				Object o = Class.forName(testName).newInstance();
				System.out.println("Class "+o.getClass().getName()+" is a Tool: "+ (o instanceof Tool ));
				TestCase tc = new TestCase((Tool) o, (String[]) args.toArray(new String[args.size()]));
				testcases.add(tc);
				current_testcase = tc;
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("Could not instantiate '"+testName+"', caught "+ex.getClass().getName()+ex.getMessage());
			}
		} 
		else if ("dbprops".equals(qName)) {
			System.out.println("PARSING RESULTDB TAG1");
			dbname = atts.getValue("dbname");
			dbout = atts.getValue("dbout");
			collection = atts.getValue("collection");
			dbport = Integer.parseInt(atts.getValue("port"));
			binpath = atts.getValue("path");
			System.out.println(binpath + " " + dbport);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}
}
