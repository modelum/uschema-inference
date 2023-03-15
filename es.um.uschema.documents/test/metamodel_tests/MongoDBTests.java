/**
 * 
 */
package metamodel_tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.documents.main.Orchestrator;
import es.um.uschema.documents.main.util.InferenceMode;
import es.um.uschema.documents.main.util.constants.ConfigConstants;

/**
 * @author dsevilla
 *
 */
class MongoDBTests 
{
	private static Orchestrator inferenceTest;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUp() throws Exception
	{
	    inferenceTest = new Orchestrator(DbType.MONGODB);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDown() throws Exception
	{
	}

	// BuildUSchema created: models/user_profile_huge_RESULT.xmi in 350040 ms (mapReduce time: 349624 ms)
	// BuildUSchema created: models/user_profile_huge_RESULT.xmi in 348935 ms (mapReduce time: 348588 ms)
	// BuildUSchema created: models/user_profile_huge_RESULT.xmi in 347408 ms (mapReduce time: 346979 ms)
	@Test
	void testHuge()
	{
		inferenceTest.runJsonExample(InferenceMode.INFER_ONLY, ConfigConstants.JSON_FOLDER, "user_profile_huge");
	}

	// BuildUSchema created: models/user_profile_small_RESULT.xmi in 35548 ms (mapReduce time: 35186 ms)
	// BuildUSchema created: models/user_profile_small_RESULT.xmi in 34895 ms (mapReduce time: 34522 ms)
	// BuildUSchema created: models/user_profile_small_RESULT.xmi in 34716 ms (mapReduce time: 34366 ms)
	@Test
	void testSmall()
	{
		inferenceTest.runJsonExample(InferenceMode.INFER_ONLY, ConfigConstants.JSON_FOLDER, "user_profile_small");
	}
	
	// BuildUSchema created: models/user_profile_medium_RESULT.xmi in 41206 ms (mapReduce time: 40862 ms)
	// BuildUSchema created: models/user_profile_medium_RESULT.xmi in 40906 ms (mapReduce time: 40566 ms)
	// BuildUSchema created: models/user_profile_medium_RESULT.xmi in 40786 ms (mapReduce time: 40448 ms)
	@Test
	void testMedium()
	{
		inferenceTest.runJsonExample(InferenceMode.INFER_ONLY, ConfigConstants.JSON_FOLDER, "user_profile_medium");
	}
	
	// BuildUSchema created: models/user_profile_large_RESULT.xmi in 114644 ms (mapReduce time: 114264 ms)
	// BuildUSchema created: models/user_profile_large_RESULT.xmi in 115415 ms (mapReduce time: 115009 ms)
	// BuildUSchema created: models/user_profile_large_RESULT.xmi in 117318 ms (mapReduce time: 116888 ms)
	@Test
	void testLarge()
	{
		inferenceTest.runJsonExample(InferenceMode.INFER_ONLY, ConfigConstants.JSON_FOLDER, "user_profile_large");
	}
	
}
