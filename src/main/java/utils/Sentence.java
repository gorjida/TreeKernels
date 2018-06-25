package utils;

import java.util.*;
/**
 * Created by u6042446 on 2018-06-07.
 */
public class Sentence {

    private String sentenceText;
    private int sentLength;
    private int sentenceId;

    public Sentence(String sentenceText,int sentLength,int sentenceId)
    {
        this.sentenceText = sentenceText;
        this.sentLength = sentLength;
        this.sentenceId = sentenceId;
    }


    public String getSentenceText() {return (this.sentenceText);}
    public int getSentLength() {return (sentLength);}
    public int getSentenceId() {return (sentenceId);}
}
