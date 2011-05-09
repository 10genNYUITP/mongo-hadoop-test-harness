package testharness;

import java.io.*;

public class TestHarness  extends com.mongodb.hadoop.util.MongoTool{

    String [] testcases = null;
    public static void main(String[] args) throws Exception{
        new TestHarness().run(args);
    }
    public int run(String[] args) throws Exception{
        //String fileName  = "/home/r_omio/mongo-hadoop-test-harness/src/testharness/Config.xml";
        String fileName="";
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
        //(new GenerateXML()).generate((new PropertyCycle().getGens()));
        //(new GenerateXML()).generateXML(cfrNew);
        return 0;
    }
}
