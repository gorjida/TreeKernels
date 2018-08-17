import sys
sys.path.append("/Users/u6042446/IdeaProjects/TreeKernels/python/evaluator/")
import pandas as pd
import numpy as np
from sklearn.preprocessing import scale
import csv
from conf.Conf import Conf
from sklearn.preprocessing import MinMaxScaler
from sklearn import linear_model
from processor.Data import Data
from sklearn import metrics



if __name__=="__main__":
    conf = Conf("/Users/u6042446/IdeaProjects/TreeKernels/data/preprocessed/", "temp.txt", "header.txt")
    EXPERIMENTS = [conf._list_of_feature_names,conf._list_of_feature_names_base,conf._list_of_feature_names_kernel]
    data = Data(conf)
    folds = data.n_fold_validation(data.full_data_frame, 10)

    for fold_index,fold in enumerate(folds):
        train_set_df, test_set_df = data.generate_train_validation_form_fold(fold_index, folds)

        #CROSS VALIDATION

        best_regularizer = .01
        #logreg = linear_model.LogisticRegression(C=1.0 / best_regularizer, class_weight={1: 20})


        for experiment in EXPERIMENTS:
            logreg = linear_model.LogisticRegression(C=1.0 / best_regularizer)
            training_features = np.array(train_set_df[experiment])
            labels = np.array(train_set_df["label_numerical"])
            test_features = np.array(test_set_df[experiment])
            test_labels = np.array(test_set_df["label_numerical"])
            logreg.fit(training_features, labels)
            probs = logreg.predict_proba(test_features)[:,1]
            fpr, tpr, thresholds = metrics.roc_curve(test_labels, probs)
            print(metrics.auc(fpr, tpr))




