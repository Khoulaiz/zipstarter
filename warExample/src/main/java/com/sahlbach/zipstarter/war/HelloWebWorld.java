package com.sahlbach.zipstarter.war;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class HelloWebWorld {

    public static void main (String[] args) {

        GenericApplicationContext ctx = new GenericApplicationContext();
        ctx.setClassLoader(HelloWebWorld.class.getClassLoader());
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
        xmlReader.loadBeanDefinitions(new ClassPathResource("beans/beans-cmdline.xml", HelloWebWorld.class.getClassLoader()));
        ctx.refresh();

        HelloWorldBean helloWorldBean = (HelloWorldBean) ctx.getBean("helloWorldBean");
        System.out.println(helloWorldBean.getOutputText());
    }
}
