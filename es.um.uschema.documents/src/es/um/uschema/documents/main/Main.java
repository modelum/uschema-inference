package es.um.uschema.documents.main;

import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.documents.main.util.InferenceMode;
import es.um.uschema.documents.main.util.constants.ConfigConstants;

public class Main
{
    public static void main(String[] args)
    {
        Orchestrator inferenceTest = new Orchestrator(DbType.MONGODB);

        // inferenceTest.runCompaniesExample(InferenceMode.FILL_AND_INFER, ConfigConstants.COMPANIES_FILE); //POJO
        // inferenceTest.runRedditExample(InferenceMode.FILL_ONLY, ConfigConstants.REDDIT_FOLDER);
        // inferenceTest.runEveryPoliticianExample(InferenceMode.FILL_ONLY, ConfigConstants.EVERYPOLITICIAN_FILE);
        // inferenceTest.runFacebookExample(InferenceMode.FILL_AND_INFER, ConfigConstants.FACEBOOK_FOLDER); //POJO
        // inferenceTest.runHarvardExample(InferenceMode.FILL_AND_INFER, ConfigConstants.HARVARD_FILE); //POJO
        // inferenceTest.runJsonExample(InferenceMode.INFER_ONLY, ConfigConstants.JSON_FOLDER, "user_profile_huge");
        // inferenceTest.runLinksExample(InferenceMode.FILL_AND_INFER, ConfigConstants.LINKS_FOLDER); //POJO
        //////// inferenceTest.runModelExample(InferenceMode.FILL_AND_INFER, ConfigConstants.MODEL_FILE);
        // inferenceTest.runOpenSanctionsExample(InferenceMode.FILL_AND_INFER, ConfigConstants.OPENSANCTIONS_FILE);
        // inferenceTest.runPleiadesExample(InferenceMode.INFER_ONLY, ConfigConstants.PLEIADES_FILE);
        // inferenceTest.runProteinsExample(InferenceMode.FILL_ONLY, ConfigConstants.PROTEINS_FOLDER); //POJO
        // inferenceTest.runPublicationsExample(InferenceMode.FILL_AND_INFER, ConfigConstants.PUBLICATIONS_FILE); //POJO
        inferenceTest.runStackOverflowExample(InferenceMode.FILL_ONLY, ConfigConstants.STACKOVERFLOW_FOLDER);
        // inferenceTest.runUrbanDictionaryExample(InferenceMode.FILL_AND_INFER, ConfigConstants.URBANDICTIONARY_FILE); //POJO
        // inferenceTest.runWebclickExample(InferenceMode.FILL_AND_INFER, ConfigConstants.WEBCLICKS_FOLDER); //POJO
        // inferenceTest.runHearthstoneExample(InferenceMode.INFER_ONLY, ConfigConstants.HEARTHSTONE_FOLDER);
        // inferenceTest.runSolarSystemExample(InferenceMode.FILL_AND_INFER, ConfigConstants.SOLARSYSTEM_FOLDER);

        // TODO: Before checking more datasets, we need to make sure "ObjectMapper oMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);"
        // Is in each interface. Thing is, this is only working por POJO objects and not readTree interfaces.
        // So tldr; datasets loaded without POJO objects are inserting NULL and empty values.
        // Problem with the COMPANY dataset is that it contains A LOT of aggregated objects and null values.
        // Aggregated objects tend to make mongodb run out of memory during the reduce process.
        // Null values tend to abort the inference process. Until the inference process is fixed (TODO(tm)),
        // we will make use of POJO objects and ignore problematic fields. Thing is, then we have a lot of options...
    }
}
