package com.sahlbach.zipstarter.war;

/**
 * Copyright by Volkswagen AG
 *
 * @author Andreas Sahlbach
 *         Date: 9/5/13
 *         Time: 6:28 PM
 */
public class HelloWorldBean {
    private String outputText;

    public String getOutputText () {
        return outputText;
    }

    public void setOutputText (final String outputText) {
        this.outputText = outputText;
    }
}
