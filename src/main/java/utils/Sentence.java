package utils;

import java.util.*;
/**
 * Created by u6042446 on 2018-06-07.
 */
public class Sentence {

    private String tokens;
    private int sentLength;

    public Sentence(String tokens,int sentLength)
    {
        this.tokens = tokens;
        this.sentLength = sentLength;
    }


    public String getTokens() {return (tokens);}
    public int getSentLength() {return (sentLength);}
}
