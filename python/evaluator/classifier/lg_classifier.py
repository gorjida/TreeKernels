import sys
sys.path.append("/Users/u6042446/IdeaProjects/TreeKernels/python/evaluator/")
sys.path.append("/Users/u6042446/IdeaProjects/TreeKernels/python/conf/")
import pandas as pd
import numpy as np
from sklearn.preprocessing import scale
import csv
from conf.Conf import Conf
from sklearn.preprocessing import MinMaxScaler
from sklearn import linear_model
from processor.Data import Data
from sklearn import metrics
import operator


def calculate_auc(test_labels,probs):
    thresholds = np.arange(0,1.01,.05)

    tps = []
    fps = []
    auc = 0
    for th_index,th in enumerate(thresholds):
        temp_tps = []
        temp_fps = []
        for index, x in enumerate(test_labels):
            if probs[index] > th:
                if x == 1:
                    temp_tps.append(1)
                    temp_fps.append(0)
                else:
                    temp_tps.append(1)
                    temp_fps.append(0)
            else:
                temp_fps.append(0)
                temp_tps.append(0)

        tps.append(sum(temp_tps)/(1.0*len(temp_tps)))
        fps.append(sum(temp_fps)/(1.0*len(temp_fps)))
        if th_index>0:
            delta_tps = tps[th_index-1]-tps[th_index]
            delta_fps = fps[th_index-1]-fps[th_index]
            print(delta_tps)
            auc+= delta_fps*delta_tps
    return (auc)



if __name__=="__main__":

    #Full path of the file that stores all the metrics
    results_file_name = "/Users/u6042446/IdeaProjects/TreeKernels/python/evaluator/Results/mt-metrics-paraphrase-corpus_kernel_family_experiment.txt"
    writer = open(results_file_name,"a")
    writer.write("Experiment\tMean_AUC\tStandard_Deviation_AUC\n")

    #base path where all the features are stored
    base_path = "/Users/u6042446/IdeaProjects/TreeKernels/data/input_data_original/paraphrase_identification_task/dataset/mt-metrics-paraphrase-corpus/"
    #Name of the feature file name
    feature_file_name = "temp.txt"
    #Name of header file
    header_file_name = "header.txt"
    conf = Conf(base_path, feature_file_name, header_file_name)
    feature_families_experiment = conf.extract_feature_parser_categories()
    data = Data(conf)

    experiment_auc = {}
    for experiment_name in feature_families_experiment:
        experiment = feature_families_experiment[experiment_name]
        print("Running for experiment="+str(experiment_name))

        mcmc_auc = []
        for mcmc in range(0,10):
            folds = data.n_fold_validation(data.full_data_frame, 10)
            #Train/test validation folds
            for fold_index,fold in enumerate(folds):
                test_auc = []
                train_set_df, test_set_df = data.generate_train_validation_form_fold(fold_index, folds)
                #Cross Validation using train-set-df
                auc = {}
                regs = [1E-3,1E-2,1E-1,1,10,100,100]
                for r in regs:
                    logreg = linear_model.LogisticRegression(C=1.0 / r)
                    valid_folds = data.n_fold_validation(train_set_df,10)
                    temp_auc = []
                    for valid_fold_index,valid_fold in enumerate(valid_folds):
                        valid_train_set_df,valid_test_set_df = data.generate_train_validation_form_fold(valid_fold_index,valid_folds)
                        #Train on the validation set
                        valid_training_features = np.array(valid_train_set_df[experiment])
                        valid_labels = np.array(valid_train_set_df["label_numerical"])
                        valid_test_features = np.array(valid_test_set_df[experiment])
                        valid_test_labels = np.array(valid_test_set_df["label_numerical"])
                        logreg.fit(valid_training_features, valid_labels)
                        probs = logreg.predict_proba(valid_test_features)[:, 1]
                        fpr, tpr, thresholds = metrics.roc_curve(valid_test_labels, probs)
                        temp_auc.append(metrics.auc(fpr, tpr))
                    auc[r] = np.mean(temp_auc)


                #Choose reg with the best performance
                best_regularizer = sorted(auc.items(),key=operator.itemgetter(1),reverse=True)[0][0]
                logreg = linear_model.LogisticRegression(C=1.0 / best_regularizer)
                training_features = np.array(train_set_df[experiment])
                labels = np.array(train_set_df["label_numerical"])
                test_features = np.array(test_set_df[experiment])
                test_labels = np.array(test_set_df["label_numerical"])
                logreg.fit(training_features, labels)
                probs = logreg.predict_proba(test_features)[:,1]
                fpr, tpr, thresholds = metrics.roc_curve(test_labels, probs)
                test_auc.append(metrics.auc(fpr, tpr))

            print("Finished round="+str(mcmc))
            mcmc_auc.append(np.mean(test_auc))

        mean = np.mean(mcmc_auc)
        std = np.std(mcmc_auc)
        writer.write(experiment_name+"\t"+str(mean)+"\t"+str(std)+"\n")
        writer.flush()
        #experiment_auc[experiment] = np.mean(mcmc_auc)

    writer.close()