
import java.io.*;

public class TestHarness {

	String [] testcases = null;

	public static void main(String[] args) throws Exception{
<<<<<<< HEAD
		String fileName =  "/home/rushin/mongo-test-harness-eclipse/src/testharness/Config.xml";
=======

		String fileName;
>>>>>>> 4b771e631fd5709d1d71b0a319460398e0db6016
		
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

		System.out.println("# OF THE TESCASES MAP: " + cfrNew.testcases.size());
		for(TestCase tstc : cfrNew.testcases) {
			System.out.println("test harness: RUNNING TEST CASE: " + tstc);
			tstc.runTest(cfrNew.propertyCycle);
		}
        //(new ResultStorage()).storeDB();
	}
}
