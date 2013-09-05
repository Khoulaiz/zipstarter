package com.sahlbach.zipstarter.zip;

import java.util.ResourceBundle;

public class HelloWorld {

    public static void main (String[] args) {
        ResourceBundle hwResource = ResourceBundle.getBundle("com/sahlbach/zipstarter/helloWorld");
        System.out.println("Hello "+hwResource.getString("hello"));
    }

}
