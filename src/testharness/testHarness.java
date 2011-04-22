
import java.io.*;
import java.util.*;

public class testHarness {

	String [] testcases = null;

	public static void main(String[] args) throws Exception{
		String fileName  = "/home/rushin/mongo-test-harness/src/testharness/Config.xml";
		//String fileName = null;
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

		//java.util.Iterator<Map.Entry<String, TestCase>> i1 = cfrNew.testcases.entrySet().iterator();
		System.out.println("SIZE OF THE TESCASES MAP: " + cfrNew.testcases.size());
		for(TestCase tstc : cfrNew.testcases) {
			// while (i1.hasNext()) {	 
			//Map.Entry<String, TestCase> pairs = (Map.Entry) i1.next();
			//TestCase tcase = (TestCase) pairs.getValue();
			//String tname = (String) pairs.getKey();
			System.out.println("RUNNING TEST VASE: " + tstc);

			tstc.runTest(cfrNew.propertyCycle);

		}
	}//main()
}
