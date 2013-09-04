package com.sahlbach.zipstarter.zip;

import java.util.ResourceBundle;

public class HelloWorld {

    public static void main (String[] args) {
        ResourceBundle hwResource = ResourceBundle.getBundle("MyResources");
        System.out.println("Hello "+hwResource.getString("hello"));
    }

}
