
import java.io.*;

public class TestHarness {

	String [] testcases = null;

	public static void main(String[] args) throws Exception{
		String fileName  = "/home/rushin/mongo-test-harness-eclipse/src/testharness/Config.xml";
		//String fileName="";
		for(int i = 0; i < args.length ; i++){
			String argi = args[i];
			if ("--config".equals(argi))
				fileName = args[++i];
			else{
				System.err.println("unknown argument "+argi);
				System.exit(1);
			}
		}
		if (fileName == null){
			System.err.println("must set config file");
			System.exit(1);
		}

		ConfigFileReader cfrNew = ConfigFileReader.parse(new FileReader(fileName));

		for(TestCase tstc : cfrNew.testcases) {
			tstc.runTest(cfrNew.propertyCycle);
		}
        (new ResultStorage()).storeDB();
        (new GenerateXML()).generate();
	}
}
