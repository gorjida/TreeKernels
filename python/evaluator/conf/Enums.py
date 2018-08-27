from enum import Enum

class Parser_Category(Enum):
    STANFORD = "STANFORD"
    UD = "UD"
    CONSTITUENCY = "CONST"
    BASE = "BASE"

class Feature_Family_Experiments(Enum):
    all = "All_Features"
    base = "Base_Only"
    stanford = "Stanford_Only"
    UD = "UD_Only"
    Const = "Constituency_Only"
    kernels = "All_Kernels_Only"
    base_const = "Base_With_Const"
    base_stanford = "Base_With_Stanford"
    base_ud = "Base_with_UD"