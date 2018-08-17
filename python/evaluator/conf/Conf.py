

class Conf:
    def __init__(self,base_path,feature_file_name,header):
        self._base_path = base_path
        self._feature_file_name = feature_file_name
        self._header_file_name = header

        self._list_of_feature_names = ['STANFORD_IDENTITY','STANFORD_SOFT','STANFORD_HYBRID','UD_IDENTITY','UD_SOFT','UD_HYBRID','CONST_IDENTITY','STANFORD_EMBED','STANFORD_HYBRID_EMBED'
            ,'UD_EMBED','UD_HYBRID_EMBED','BASE_VECTOR_STANFORD','BASE_JACC_WORD','BASE_JACC_POSTAG','BASE_JACC_RELATION_STANFORD','BASE_VECTOR_UD','BASE_JACC_RELATION_UD']

        self._list_of_feature_names_base = ['BASE_VECTOR_STANFORD', 'BASE_JACC_WORD', 'BASE_JACC_POSTAG',
                                       'BASE_JACC_RELATION_STANFORD', 'BASE_VECTOR_UD', 'BASE_JACC_RELATION_UD']

        self._list_of_feature_names_kernel = ['STANFORD_IDENTITY','UD_IDENTITY','CONST_IDENTITY','BASE_VECTOR_STANFORD','BASE_JACC_WORD','BASE_JACC_POSTAG','BASE_JACC_RELATION_STANFORD','BASE_VECTOR_UD','BASE_JACC_RELATION_UD']

        self._invalid_column_names = []
