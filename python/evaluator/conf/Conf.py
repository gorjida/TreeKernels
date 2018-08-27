import sys
sys.path.append("/Users/u6042446/IdeaProjects/TreeKernels/python/evaluator/conf/")
from Enums import Parser_Category
from Enums import Feature_Family_Experiments

class Conf:
    def __init__(self,base_path,feature_file_name,header):
        self._base_path = base_path
        self._feature_file_name = feature_file_name
        self._header_file_name = header

        self._list_of_all_feature_names = ['STANFORD_IDENTITY','STANFORD_SOFT','STANFORD_HYBRID','UD_IDENTITY','UD_SOFT','UD_HYBRID','CONST_IDENTITY','STANFORD_EMBED','STANFORD_HYBRID_EMBED'
            ,'UD_EMBED','UD_HYBRID_EMBED','BASE_VECTOR_STANFORD', 'BASE_JACC_WORD', 'BASE_JACC_POSTAG','BASE_JACC_RELATION_STANFORD', 'BASE_VECTOR_UD', 'BASE_JACC_RELATION_UD']

        self._list_of_base_feature_names = ['BASE_VECTOR_STANFORD', 'BASE_JACC_WORD', 'BASE_JACC_POSTAG',
                                       'BASE_JACC_RELATION_STANFORD', 'BASE_VECTOR_UD', 'BASE_JACC_RELATION_UD']

        self._list_of_kernel_feature_names = ['STANFORD_IDENTITY','STANFORD_SOFT','STANFORD_HYBRID','UD_IDENTITY','UD_SOFT','UD_HYBRID','CONST_IDENTITY','STANFORD_EMBED','STANFORD_HYBRID_EMBED'
            ,'UD_EMBED','UD_HYBRID_EMBED']


        self._invalid_column_names = []

    def extract_feature_parser_categories(self):
        feature_families = {}

        base = []
        const = []
        stanford = []
        ud = []
        for x in self._list_of_all_feature_names:
            prefix = x.split("_")[0]
            if prefix==Parser_Category.BASE.value:
                base.append(x)
            elif prefix==Parser_Category.CONSTITUENCY.value:
                const.append(x)
            elif prefix==Parser_Category.STANFORD.value:
                stanford.append(x)
            else:
                ud.append(x)

        #Now create experiments
        feature_families[Feature_Family_Experiments.all.value] = self._list_of_all_feature_names
        feature_families[Feature_Family_Experiments.base.value] = base
        feature_families[Feature_Family_Experiments.stanford.value] = stanford
        feature_families[Feature_Family_Experiments.UD.value] = ud
        feature_families[Feature_Family_Experiments.kernels.value] = stanford+ud+const
        feature_families[Feature_Family_Experiments.Const.value] = const
        feature_families[Feature_Family_Experiments.base_const.value] = const+base
        feature_families[Feature_Family_Experiments.base_stanford.value] = stanford+base
        feature_families[Feature_Family_Experiments.base_ud.value] = ud+base

        return (feature_families)


