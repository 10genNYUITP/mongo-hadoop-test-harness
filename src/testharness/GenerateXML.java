package testharness;

import java.io.*;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

public class GenerateXML {

	private String name;
	private String [] args; 
	private Double runtime;
	private String md5;
	private long count;
	private Date start;
	private Boolean baseline;
	private String shardsCondition;
	private String chunksCondition;
	private String slaveOK;

	public void gen() {
		
	}

	public void setName(String n) {
		name = n;
	}

	public void setArgs(String[] arguments) {
		args = arguments;		
	}

	public void setRuntime(double rt) {
		runtime = rt;		
	}

	public void setMD5(String md5sum) {
		md5 = md5sum;		
	}

	public void setCount(long c) {
		count = c;
	}

	public void setUS(String US) {
		shardsCondition = US;
	}

	public void setUC(String UC) {
		chunksCondition = UC;
	}

	public void setSO(String SO) {
		slaveOK = SO;
	}

	public void setStart(Date date) {
		start = date;		
	}

	public void setBaseline(Boolean bl) {
		baseline = bl;		
	}

	public String getShards() {
		return shardsCondition;
	}

	public void generate(List<GenerateXML> nodeVals) {

		int i = 0;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			Element root = doc.createElement("TestHarness");
			doc.appendChild(root);

			for(GenerateXML objs : nodeVals) {

				Element node = doc.createElement("Test");

				root.appendChild(node);
				Element id = doc.createElement("ID");
				id.appendChild(doc.createTextNode(Integer.toString(++i)));
				node.appendChild(id);

				Element n = doc.createElement("Name");
				n.appendChild(doc.createTextNode(objs.name));
				System.out.println(objs.name);
				node.appendChild(n);

				Element arguments = doc.createElement("Arguments");
				StringBuilder sb = new StringBuilder();
				for(String arg : objs.args) {
					sb.append(arg + "; ");
				} arguments.appendChild(doc.createTextNode(sb.toString()));
				node.appendChild(arguments);

				Element performace = doc.createElement("Runtime");
				performace.appendChild(doc.createTextNode(objs.runtime.toString()));
				node.appendChild(performace);

				Element md5 = doc.createElement("MD5");
				md5.appendChild(doc.createTextNode(objs.md5.toString()));
				node.appendChild(md5);

				Element counts = doc.createElement("Count");
				md5.appendChild(doc.createTextNode(Long.toString(objs.count)));
				node.appendChild(counts);

				Element start = doc.createElement("Start-Time");
				md5.appendChild(doc.createTextNode(objs.start.toString()));
				node.appendChild(start);

				Element baseLine = doc.createElement("Baseline");
				md5.appendChild(doc.createTextNode(objs.baseline.toString()));
				node.appendChild(baseLine);

				Element uS = doc.createElement("Use-Shards");
				uS.appendChild(doc.createTextNode(objs.shardsCondition));
				node.appendChild(uS);

				Element uC = doc.createElement("Use-Chunks");
				uC.appendChild(doc.createTextNode(objs.chunksCondition));
				node.appendChild(uC);

				Element sO = doc.createElement("Slave-OK");
				sO.appendChild(doc.createTextNode(objs.slaveOK));
				node.appendChild(sO);
			}

			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
			DOMSource src = new DOMSource(doc);
			StreamResult result =  new StreamResult(new File("/home/r_omio/mongoresults.xml"));
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
