import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import org.w3c.dom.*;

public class GenerateXML {

	public void generate() {
		
		int i = 0;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			
			Element root = doc.createElement("TestHarness");
			doc.appendChild(root);
			for(ResultStorage nodeVals : ResultStorage.objs) {
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
					
					Element performace = doc.createElement("Performace");
					performace.appendChild(doc.createTextNode(nodeVals.performance.toString()));
					node.appendChild(performace);
					
					Element md5 = doc.createElement("MD5");
					md5.appendChild(doc.createTextNode(nodeVals.md5));
					node.appendChild(md5);
					
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
