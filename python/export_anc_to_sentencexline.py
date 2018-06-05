import os, fnmatch
import xml.etree.ElementTree as ET
import re


def normalize(text):
    try:
        text = text.replace("\n"," ").replace("\t"," ")
        text = re.sub(' +', ' ',text)
        return text
    except:
        return ""

def get_sentences(infile):
    sentences = []
    ss = open(infile).read().replace('<?xml version="1.0" encoding="UTF-8"?>',"")
    ss = "<doc>"+ss+"</doc>"
    doc = ET.fromstring(ss)
    for sent in doc:
        sent2 = (normalize(sent.text))
        if sent2 != "":
            sentences.append(sent2)
    return sentences


inDIR = '/Users/u6067443/data/OANC/export'
pattern = '*xml'
fileList = []

# Walk through directory
for dName, sdName, fList in os.walk(inDIR):
    for fileName in fList:
        if fnmatch.fnmatch(fileName, pattern): # Match search string
            fileList.append(os.path.join(dName, fileName))

for file in fileList:
    sentences = get_sentences(file)
    for sent in sentences:
        print(sent)



