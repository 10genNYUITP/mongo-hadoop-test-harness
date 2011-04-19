
import java.io.*;

public class TestHarness {

	String [] testcases = null;

	public static void main(String[] args) throws Exception{
		String fileName = null;
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
			System.out.println("RUNNING TEST CASE: " + tstc);
			tstc.runTest(cfrNew.propertyCycle);
		}
        (new ResultStorage()).storeDB();
	}
}
