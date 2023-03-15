package es.um.uschema.documents.main;

import java.io.File;

import es.um.uschema.documents.injectors.interfaces.Companies2Db;
import es.um.uschema.documents.injectors.interfaces.EveryPolitician2Db;
import es.um.uschema.documents.injectors.interfaces.Facebook2Db;
import es.um.uschema.documents.injectors.interfaces.Harvard2Db;
import es.um.uschema.documents.injectors.interfaces.Hearthstone2Db;
import es.um.uschema.documents.injectors.interfaces.Json2Db;
import es.um.uschema.documents.injectors.interfaces.Links2Db;
import es.um.uschema.documents.injectors.interfaces.Model2Db;
import es.um.uschema.documents.injectors.interfaces.OpenSanctions2Db;
import es.um.uschema.documents.injectors.interfaces.Pleiades2Db;
import es.um.uschema.documents.injectors.interfaces.Proteins2Db;
import es.um.uschema.documents.injectors.interfaces.Publications2Db;
import es.um.uschema.documents.injectors.interfaces.Reddit2Db;
import es.um.uschema.documents.injectors.interfaces.SolarSystem2Db;
import es.um.uschema.documents.injectors.interfaces.Source2Db;
import es.um.uschema.documents.injectors.interfaces.StackOverflow2Db;
import es.um.uschema.documents.injectors.interfaces.UrbanDictionary2Db;
import es.um.uschema.documents.injectors.interfaces.Webclick2Db;
import es.um.uschema.documents.injectors.util.DbType;
import es.um.uschema.documents.main.process.FillInferDb;
import es.um.uschema.documents.main.util.InferenceMode;
import es.um.uschema.documents.main.util.constants.ConfigConstants;

public class Orchestrator
{
    private DbType dbType;

    private FillInferDb dbFillInfer;

    public Orchestrator(DbType dbType)
    {
        this.dbType = dbType;
        this.dbFillInfer = new FillInferDb(this.dbType);
    }

    public void runCompaniesExample(InferenceMode option, String sourceFile)
    {
        String dbName = "companies";
        File input = new File(sourceFile);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Companies2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runEveryPoliticianExample(InferenceMode option, String sourceFile)
    {
        String dbName = "everypolitician";
        File input = new File(sourceFile);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new EveryPolitician2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runFacebookExample(InferenceMode option, String sourceFolder)
    {
        String dbName = "facebook";
        File input = new File(sourceFolder);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Facebook2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runHarvardExample(InferenceMode option, String sourceFile)
    {
        String dbName = "harvard";
        File input = new File(sourceFile);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Harvard2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runHearthstoneExample(InferenceMode option, String sourceFile)
    {
        String dbName = "hearthstone";
        File input = new File(sourceFile);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Hearthstone2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runJsonExample(InferenceMode option, String sourceFolder, String dbName)
    {
        // String dbName = new File(sourceFolder).getName();
        File input = new File(sourceFolder);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + "_RESULT.xmi");

        runExample(option, new Json2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runLinksExample(InferenceMode option, String sourceFolder)
    {
        String dbName = "links";
        File input = new File(sourceFolder);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Links2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runModelExample(InferenceMode option, String sourceFile)
    {
        String fileName = new File(sourceFile).getName();
        String dbName = fileName.substring(0, fileName.lastIndexOf("."));
        File input = new File(sourceFile);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + "_RESULT.xmi");

        runExample(option, new Model2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runOpenSanctionsExample(InferenceMode option, String sourceFile)
    {
        String dbName = "opensanctions";
        File input = new File(sourceFile);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new OpenSanctions2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runPleiadesExample(InferenceMode option, String sourceFile)
    {
        String dbName = "pleiades";
        File input = new File(sourceFile);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Pleiades2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runProteinsExample(InferenceMode option, String sourceFolder)
    {
        String dbName = "proteins";
        File input = new File(sourceFolder);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Proteins2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runPublicationsExample(InferenceMode option, String sourceFile)
    {
        String dbName = "publications";
        File input = new File(sourceFile);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Publications2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runStackOverflowExample(InferenceMode option, String sourceFolder)
    {
        String dbName = "stackoverflow";
        File input = new File(sourceFolder);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        // Users.xml: 6438660 files => 38 minutes
        // Votes.xml: 116720227 files => 10 hours
        // Comments.xml: 53566720 files => 5 hours
        // Posts.xml: 33566854 files => ???
        // Tags.xml: 48375 files
        // PostLinks.xml: 3993518 files
        // Badges.xml: 21882069 files
        runExample(option, new StackOverflow2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runUrbanDictionaryExample(InferenceMode option, String sourceFile)
    {
        String dbName = "urban";
        File input = new File(sourceFile);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new UrbanDictionary2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runWebclickExample(InferenceMode option, String sourceFolder)
    {
        String dbName = "webclicks";
        File input = new File(sourceFolder);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Webclick2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runRedditExample(InferenceMode option, String sourceFolder)
    {
        String dbName = "reddit";
        File input = new File(sourceFolder);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new Reddit2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    public void runSolarSystemExample(InferenceMode option, String sourceFolder)
    {
        String dbName = "solar_system";
        File input = new File(sourceFolder);
        File outputModel = new File(ConfigConstants.OUTPUT_FOLDER + dbName + ".xmi");

        runExample(option, new SolarSystem2Db(dbType, ConfigConstants.DATABASE_IP), dbName, input, outputModel);
    }

    private void runExample(InferenceMode option, Source2Db source2Db, String dbName, File source, File outputModel)
    {
        if (option != InferenceMode.INFER_ONLY)
        {
            if (source.isDirectory())
                dbFillInfer.fillDbFromFolder(source2Db, dbName, source);
            else
                dbFillInfer.fillDbFromFile(source2Db, dbName, source);
        }

        if (option != InferenceMode.FILL_ONLY)
            dbFillInfer.inferFromDb(dbName, outputModel);
    }
}
