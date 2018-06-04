import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import csv
from enum import Enum


score_path = "depthWidthJune1st_SICK_DATA.txt"
score_path_ud = "depthWidthJune1st_SICK_DATA_UDDep.txt"
score_path_constituency = "depthWidthJune1st_SICK_DATA_constituency.txt"

class stats_type(Enum):
    DEPTH = "Depth"
    WIDTH = "Width"

def map_records(x):
    z = []
    [z.append(int(xx)) for xx in x.split("|")]
    return (z)

def generate_bins_freqs(data,type):
    # Filter nans
    if type==stats_type.DEPTH:
        filtered = data.dropna(subset=['DepthList'])
        num_with_no_common = len(data) - len(filtered)
        all = reduce(lambda x, y: x + y, map(map_records, filtered["DepthList"]))
    else:
        filtered = data.dropna(subset=['WidthList'])
        num_with_no_common = len(data) - len(filtered)
        all = reduce(lambda x, y: x + y, map(map_records, filtered["WidthList"]))

    bins = np.arange(.5, 21.5, 1)
    freqs, bins = np.histogram(all, bins=bins)
    ff = list(freqs)
    ff.append(0)
    freqs = np.array(ff)
    return (bins,freqs,num_with_no_common)

def generate_bins_freqs_for_scores(data):
    bins = np.arange(0, 1.01, .05)
    freqs, bins = np.histogram(data, bins=bins)
    ff = list(freqs)
    ff.append(0)
    freqs = np.array(ff)
    return (bins, freqs)

if __name__=="__main__":

    type = stats_type.WIDTH
    #Load data
    data = pd.read_csv(score_path, delimiter="\t")
    data["sim_score"] = data.apply(lambda x: x['InterSectNum'] / (np.sqrt(x['Sent1SelfNum'] * x['Sent2SelfNum'])),
                                   axis=1)
    depth_vals = []
    width_vals = []

    data_ud = pd.read_csv(score_path_ud, delimiter="\t")
    data_ud["sim_score"] = data_ud.apply(lambda x: x['InterSectNum'] / (np.sqrt(x['Sent1SelfNum'] * x['Sent2SelfNum'])),
                                   axis=1)
    depth_vals_ud = []
    width_vals_ud = []

    data_const = pd.read_csv(score_path_constituency, delimiter="\t")
    data_const["sim_score"] = data_const.apply(lambda x: x['InterSectNum'] / (np.sqrt(x['Sent1SelfNum'] * x['Sent2SelfNum'])),
                                         axis=1)
    depth_vals_const = []
    width_vals_const = []

    bins,freqs,num_with_no_common = generate_bins_freqs(data,type)
    bins_ud,freqs_ud,num_with_no_common_ud = generate_bins_freqs(data_ud,type)
    bins_const, freqs_const, num_with_no_common_const = generate_bins_freqs(data_const, type)

    #plot histogram

    bar_width = 0.35
    plt1 = plt.bar(bins,freqs,bar_width,color="b")
    plt2 = plt.bar(bins_ud+bar_width, freqs_ud,bar_width,color="r")
    plt3 = plt.bar(bins_const + 2*bar_width, freqs_const, bar_width, color="m")
    if type==stats_type.WIDTH:
        plt.xlabel("Subtree Width",size=20)
    else:
        plt.xlabel("Subtree Depth", size=20)
    plt.ylabel("Frequency",size=20)
    plt.grid(True)
    plt.legend([plt1,plt2,plt3],["Stanford Standard Parser","Universal Dependency Parser","Constituency Parser"])
    plt.show()


    #plot scores
    ud_score = list(data_ud['sim_score'])
    stanford_score = list(data['sim_score'])
    consituency_score = list(data_const['sim_score'])

    bins,freqs = generate_bins_freqs_for_scores(stanford_score)
    bins_ud,freqs_ud = generate_bins_freqs_for_scores(ud_score)
    bins_const,freqs_const = generate_bins_freqs_for_scores(consituency_score)
    bar_width = 0.05/3
    plt1 = plt.bar(bins, freqs,bar_width, color="b")
    plt2 = plt.bar(bins_ud + bar_width, freqs_ud, bar_width, color="r")
    plt2 = plt.bar(bins_ud + 2*bar_width, freqs_const, bar_width, color="m")
    plt.xlabel("Similarity Scores",size=20)
    plt.ylabel("Frequency", size=20)
    plt.xticks(bins)
    plt.grid(True)
    plt.legend([plt1, plt2,plt3], ["Stanford Standard Parser", "Universal Dependency Parser","Constituency Parser"])
    plt.show()






    #Find histogram
    #news = pd.DataFrame(reduce(lambda x, y: x + y, map(lambda r: data['DepthList'], data)))