
import sys
sys.path.append("/Users/u6042446/IdeaProjects/TreeKernels/python/evaluator/")
import pandas as pd
import numpy as np
from sklearn.preprocessing import scale
import csv
from conf.Conf import Conf
from sklearn.preprocessing import MinMaxScaler
import random

from conf import Conf


class Data:
    def __init__(self,conf):
        self._conf = conf
        self.load_feature_file()

    def load_feature_file(self):
        scaler = MinMaxScaler()
        full_data_frame = pd.read_csv(self._conf._base_path + "/" + self._conf._feature_file_name , sep="\t",quoting=csv.QUOTE_ALL)
        self.full_data_frame = full_data_frame.dropna() #drop all the NANs
        self.full_data_frame[self._conf._list_of_all_feature_names] = scaler.fit_transform(self.full_data_frame[self._conf._list_of_all_feature_names])
        self.full_data_frame["label_numerical"] = self.full_data_frame['Label'].map(lambda rec: rec)
        #feature_df = data[conf._list_of_feature_names]

    def shuffle_data_frame(self):
        self.full_data_frame = self.full_data_frame.reindex(np.random.permutation(self.full_data_frame.index))

    def n_fold_validation(self,train_df,n_fold):
        """
        :param train_df:
        :param n_fold:
        :return:
        """
        #self.shuffle_data_frame() #shuffle wholde dataframe
        all_indexes = list(train_df.index)
        random.shuffle(all_indexes)
        fold_indexes = np.array_split(all_indexes,n_fold)#generate different folds
        fold_train_df = []
        for x in fold_indexes:
            this_fold_df = train_df.loc[x]
            fold_train_df.append(this_fold_df)
        return (fold_train_df)

    def generate_train_validation_form_fold(self,fold_index,all_folds):
        """
        generates list of all training and validation records
        :param fold_index: index of the fold for validation
        :param fold_train_df: full folded training data
        :return:
        """
        all_train_instances = all_folds[:fold_index]+all_folds[(fold_index+1):]
        empty_df = pd.DataFrame()
        for x in all_train_instances:
            empty_df = empty_df.append(x)
        #train_records = []
        #for train_x in all_train_instances: train_records += list(train_x)
        validation_instances = all_folds[fold_index]
        return (empty_df,validation_instances)


if __name__=="__main__":
    conf = Conf.Conf("/Users/u6042446/IdeaProjects/TreeKernels/data/preprocessed/", "temp.txt", "header.txt")
    data = Data(conf)
    folds = data.n_fold_validation(data.full_data_frame,10)






