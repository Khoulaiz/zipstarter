package com.sahlbach.zipstarter.war;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Copyright by Volkswagen AG
 *
 * @author Andreas Sahlbach
 *         Date: 9/5/13
 *         Time: 2:48 PM
 */
public class HelloWebWorld {
    public static void main (String[] args) {

        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.setClassLoader(HelloWebWorld.class.getClassLoader());
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new ClassPathResource("beans-cmdline.xml", HelloWebWorld.class.getClassLoader()));
        ctx.refresh();

        HelloWorldBean helloWorldBean = (HelloWorldBean) ctx.getBean("helloWorldBean");
        System.out.println(helloWorldBean.getOutputText());
    }
}
