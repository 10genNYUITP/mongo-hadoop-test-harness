
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
	private String binpath;
	private String testName;
	private String resultsdb;
	List<String> args = new ArrayList<String>();
	private String dumppath;

	public com.mongodb.MongoURI getResultURI(){
		com.mongodb.MongoURI resultsuri = new com.mongodb.MongoURI(resultsdb);
		return resultsuri;
	}

	public String getBinpath() {
		return binpath;
	}

	public String getDumpPath() {
		return dumppath;
	}

	class PropertyHandler extends org.xml.sax.ext.DefaultHandler2{
		ContentHandler parent ;
		PropertyCycle pc = null;
		int depth = 0;
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			depth++;
			if(depth == 1){
				pc = new PropertyCycle(ConfigFileReader.this, attributes.getValue("name"));
			} 
			else if ("val".equals(qName)){
				final String value = attributes.getValue("value");
				pc.addValue(value);
				String t = attributes.getValue("baseline");
				if ("true".equals(t))
					pc.setBaseline(value);
			}
			else if ("when".equals(qName)){
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
				parser.setContentHandler(parent);
		}
	}

	static ConfigFileReader parse(Reader r) throws SAXException, ParserConfigurationException, IOException{
		XMLReader xr = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
		xr.setFeature("http://xml.org/sax/features/validation", false);
		xr.setFeature("http://apache.org/xml/features/validation/schema/augment-psvi", false);
		ConfigFileReader cfr = new ConfigFileReader();
		cfr.parser = xr;
		xr.setContentHandler(cfr);
		System.out.println("Reader :" + r);
		xr.parse(new InputSource(r));
		return cfr;
	}


	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
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
				//System.out.println("Class "+o.getClass().getName()+" is a Tool: "+ (o instanceof Tool ));
				TestCase tc = new TestCase((Tool) o, (String[]) args.toArray(new String[args.size()]));
				testcases.add(tc);
				current_testcase = tc;
			} catch (Throwable ex) {
				ex.printStackTrace();
				System.err.println("Could not instantiate '"+testName+"', caught "+ex.getClass().getName()+" "+ex.getMessage());
                                System.err.println("my classpath is: "+System.getProperty("java.class.path"));
                                System.exit(1);
			}
		} 
		else if ("resultsdb".equals(qName)) {
			resultsdb = atts.getValue("uri");
		}
		else if("path".equals(qName)) {
			binpath = atts.getValue("binpath");
			dumppath = atts.getValue("dumppath");
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
	}
}
