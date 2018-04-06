package conf;

import java.util.Map;

/**
 * Created by u6042446 on 2016-11-16.
 */
public class Configuration {

    //

    //public String basePath = System.getenv().get("BasePath");
    public String basePath = "/Users/u6042446/IdeaProjects/TaxAccountingQA/";
    public String stropWords = basePath+"/src/resources/stopwords_en.txt";

    public String wordSimilarityPath = basePath + "/src/resources/similarity.txt";
    public String indexToWordMapPath = basePath + "/src/resources/indexToWord.txt";
    //public String ftcRawFeaturePath = basePath+"/src/resources/TaxFeature_FTC_raw_features_sampled.txt";
    //public String ftcRawFeaturePath = "/Users/u6042446/Desktop/forTaimur/TaxAccountingQA/src/resources/TaxFeature_FTC_raw_features_combiend.txt";
    //public String ftcDependencyRawFeaturePath = basePath+"/src/resources/TaxFeature_FTC_dependency_raw_features_sampled.txt";

    public String wordEmbeddingPath = basePath+"/src/resources/word_embeddings.txt";

    //public String ftcRawFeaturePath = basePath+"/goldenData/TaxFeature_FTC_raw_features_golden.txt";
    public String ftcRawFeaturePath = basePath+"/goldenData/TaxFeature_FTC_raw_features_golden.txt";
    //public String ftcRawFeaturePath = basePath + "/src/resources/sentences_full_corpus.txt";

    public String featureFilePath = basePath+ "/featuresTest/TaxFeature";
    //public String featureFilePath = basePath+ "/src/resources/sentences_full_corpus.txt";
    //public String generatedRawFeatureFilePath = "/Users/u6042446/Desktop/features/TaxFeatures_graded_raw_features.txt";
    //public String generatedRawFeatureFilePath = "/Users/u6042446/Desktop/features/rawFeatures/hobbyLoss_generated_features.txt";
    public String generatedRawFeatureFilePath = "/Users/u6042446/Desktop/features/rawFeatures/sample.txt";

    //general configurations for FeatureGeneration
    public String resourcePath = basePath+"/src/resources";
    public String CRFModelPath = basePath+"/src/resources/CRFModels/english.muc.7class.distsim.crf.ser.gz";
    public String rulePath = basePath+"/src/resources/rules";
    public String posTagDict = basePath+"/src/resources/pos_tag_dict";

    public String SMESetOfQuestionAnswers = basePath+"/data/TRTA_TAM_250_QA_Pairs.Sets1-5.4Editors.xlsx";
    //public String CarterSetOfQuestionAnswers = "/Users/u6042446/Downloads/es_top_100_tasks_test_50.xlsx";
    //public String CarterSetOfQuestionAnswers = basePath+"/data/query_task_map_300q_1000es_for_featurization_with_contextual.csv";
    //public String CarterSetOfQuestionAnswers = basePath + "/data/query_task_map_sample.csv";
    public String CarterSetOfQuestionAnswers = basePath + "/goldenData/query_task_map_for_featurization.csv";
    public String queryToIdMap = basePath + "/data/query_question_text_map_461q.txt";
    //public String CarterSetOfQuestionAnswers =  "/Users/u6042446/Desktop/ali_files/packages/iir/FTC_resources/hobby-loss/sample_document.xlsx";

    //FTC configs
    //public String FTCRawFilePath = basePath+"/src/resources/ftc_20161022_trta_1.0.json";
    public String FTCRawFilePath = basePath + "/src/resources/sentences_full_corpus.txt";
    public String FTCSourceField = "_source";
    public String FTCTextField = "text";
    public String FTCTitleField = "title";


    public String chunkTagBegin = "B";
    public String chunkTagInside = "I";
    public String chunkTagOut = "O";
    public String nounPhrase = "NP";
    public String verbPhrase = "VP";

    public String highLevelDelimitter = "\\@";
    public String intraCategoryDelimitter = "\\$\\$\\$";
    public String interCategoryDelimitter = "\\|\\|\\|";
    public String interFeatureDelimitter = "\t";

    public String intraCategoryDelimitterWriter = "$$$";
    public String interCategoryDelimitterWriter = "|||";
    public String interFeatureDelimitterWriter = "\t";

    public int minNGram = 1;
    public int maxNgram = 3;

    //feature types
    public String queryid = "queryid";
    public String answerid = "answerid";
    public String sentenceid = "sentenceid";
    public String tokens = "tokens";
    public String ngrams = "ngrams";
    public String ngramsLemma = "ngramsLemma";
    public String chunks = "chunks";

    public String corpusDelimitter = "|";

    //feature JSON fields
    public String jsonDocId = "documentId";
    public String jsonSenId = "sentenceId";
    public String jsonSenText = "sentenceText";
    public String jsonConstTree = "constitutencyTree";
    public String jsonDepTree = "dependencyTree";
    public String jsonRawFeatures = "rawFeatures";
    public String jsonFeatToken = "tokens";
    public String jsonFeatChunks = "chunks";
    public String jsonFeatNgrams = "ngrams";
    public String jsonFeatNgramLemma = "ngramsLemma";
    public String jsonConstTreeKernel = "constituencyKernel";
    public String jsonEmbedding = "embeddingWordVec";
    public String jsonTopic = "topicUnsupervised";

    public float dependencyTreeHyperParameter = .9f;

    public int internalThreads = 3;

    public static Configuration confInstance = new Configuration();


    private Configuration() {
        //Private constructor for the configuration object
        //Try to get parameters from environmental variables first
        Map<String, String> env = System.getenv();

        //Check for important environmental variables
        if (!env.containsKey("BasePath")) {
            System.out.print("Please define an environmenal variable as BasePath and load your root-path there");
            //System.exit(1);
        }
    }

    public static Configuration getInstance() {
        return (confInstance);
    }

}
