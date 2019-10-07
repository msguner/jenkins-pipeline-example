package myTests;

import com.intuit.karate.KarateOptions;
import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;


// mvn test -Dkarate.options="--tags ~@ignore classpath:demo/cats/cats.feature" -Dtest=DemoTestParallel
// mvn test -Dkarate.options="--tags ~@ignore" -Dtest=AnimalsTest
// https://github.com/intuit/karate#command-line

//@RunWith(Karate.class)

//@KarateOptions(features = "classpath:tests/test2.feature", tags = "~@ignore")
//@KarateOptions(features = "classpath:myTests/test2.feature", tags = {"@scenario1"})
//@KarateOptions(features = "classpath:myTests/test1.feature")
public class TestRunner {

//    @AfterClass
//    public static void after() {
//        generateReport("target/surefire-reports");
//    }

    public static void generateReport(String karateOutputPath) {
        Collection<File> jsonFiles = FileUtils.listFiles(new File(karateOutputPath), new String[]{"json"}, true);
        List<String> jsonPaths = new ArrayList(jsonFiles.size());
        jsonFiles.forEach(file -> jsonPaths.add(file.getAbsolutePath()));
        Configuration config = new Configuration(new File("target"), "jenkins_pipeline_example");
        ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
        reportBuilder.generateReports();
    }

    @Test
    public void denememeTest() {
//        String myParams_p = System.getProperty("myParams");
//        JsonObject myParams_jsonObject = new JsonParser().parse(myParams_p).getAsJsonObject();
//
//        String features_p = myParams_jsonObject.get("features").getAsString();
//        String tags_p = myParams_jsonObject.get("tags").getAsString();
//
//        List<String> features = Arrays.asList(features_p.split("\\s*,\\s*"));
//        List<String> tags = Arrays.asList(tags_p.split("\\s*,\\s*"));
//
//        System.out.println("features list: " + features);
//        System.out.println("tags list: " + tags);

        //ESKİ TYPE
//        List<String> tags = Arrays.asList("@getJenkinsParamsAndPrint, @printBestDev, @printWorstDev");
//        List<String> features = Arrays.asList("classpath:tests/test2.feature");
//        String karateOutputPath = "target/surefire-reports";
//        Results results = Runner.parallel(tags, features, 5, karateOutputPath);
//        generateReport(karateOutputPath);
//        assertTrue(results.getErrorMessages(), results.getFailCount() == 0);

        String karateOutputPath = "target/surefire-reports";
//        String karateOutputPath = "karateOutput";
        Results stats = Runner.parallel(getClass(), 5, karateOutputPath);
        generateReport(karateOutputPath);
        assertTrue("there are scenario failures", stats.getFailCount() == 0);
    }
}

