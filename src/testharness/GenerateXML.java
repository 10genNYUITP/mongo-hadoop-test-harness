import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

public class GenerateXML {
	
	// either do it via addresults or read from database..

	private List<GenerateXML> objs = new ArrayList<GenerateXML>();
	private String name;
	private String [] args; 
	private Double runtime;
	private String md5;
	private long count;
	private Map<String, String> params;
	private Date start;
	private Boolean baseline;
	
	// set these conditions by dissecting setParams below
	private String shardsCondition;
	private String chunksCondition;
	private String slaveOK;
	

	public void setName(String name) {
		this.name = name;
	}

	public void setArgs(String[] args) {
		this.args = args;		
	}

	public void setRuntime(double runtime) {
		this.runtime = runtime;		
	}

	public void setMD5(String md5sum) {
		this.md5 = md5sum;		
	}

	public void setCount(long count) {
		this.count = count;
	}

	public void setParams(Map<String, String> setParams) {
		this.params = setParams;
		// dissect this and get the individual values to fill in for above declared variables...
	}

	public void setStart(Date date) {
		this.start = date;		
	}

	public void setBaseline(Boolean bl) {
		this.baseline = bl;		
	}

	void addResults() {
		objs.add(this);
	}

	public void generate() {		

		int i = 0;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			
			Element root = doc.createElement("TestHarness");
			doc.appendChild(root);
			for(GenerateXML nodeVals : objs) {
				Element node = doc.createElement("Test");
				
				root.appendChild(node);
					
					Element id = doc.createElement("ID");
					id.appendChild(doc.createTextNode(Integer.toString(++i)));
					node.appendChild(id);
				
					Element name = doc.createElement("Name");
					name.appendChild(doc.createTextNode(nodeVals.name));
					node.appendChild(name);
				
					Element args = doc.createElement("Arguments");
					StringBuilder sb = new StringBuilder();
					for(String arg : nodeVals.args) {
						sb.append(arg + "; ");
					} args.appendChild(doc.createTextNode(sb.toString()));
					node.appendChild(args);
					
					Element performace = doc.createElement("Runtime");
					performace.appendChild(doc.createTextNode(nodeVals.runtime.toString()));
					node.appendChild(performace);
					
					Element md5 = doc.createElement("MD5");
					md5.appendChild(doc.createTextNode(nodeVals.md5));
					node.appendChild(md5);
					
					Element count = doc.createElement("Count");
					md5.appendChild(doc.createTextNode(Long.toString(nodeVals.count)));
					node.appendChild(count);
					
					/*Element start = doc.createElement("Start Time");
					md5.appendChild(doc.createTextNode(nodeVals.start));		// convert date to string and store!
					node.appendChild(start);*/
					
					Element baseline = doc.createElement("Baseline");
					md5.appendChild(doc.createTextNode(nodeVals.baseline.toString()));
					node.appendChild(baseline);
					
					Element uS = doc.createElement("Use-Shards");
					uS.appendChild(doc.createTextNode(nodeVals.shardsCondition));
					node.appendChild(uS);
					
					Element uC = doc.createElement("Use-Chunks");
					uC.appendChild(doc.createTextNode(nodeVals.chunksCondition));
					node.appendChild(uC);
					
					Element sO = doc.createElement("Slave-OK");
					sO.appendChild(doc.createTextNode(nodeVals.slaveOK));
					node.appendChild(sO);
			}
			
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			DOMSource src = new DOMSource(doc);
			StreamResult result =  new StreamResult(new File("/home/rushin/mongoresults.xml"));
			t.transform(src, result);
			
		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch(FactoryConfigurationError fce) {
			fce.printStackTrace();
		} catch (TransformerConfigurationException tce) {
			tce.printStackTrace();
		} catch (TransformerException te) {
			te.printStackTrace();
		}
	}
}
