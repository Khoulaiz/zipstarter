package com.sahlbach.zipstarter.war;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private HelloWorldBean helloWorldBean;

    @RequestMapping(method = RequestMethod.GET)
    public String printHello (ModelMap model) {
        model.addAttribute("message", helloWorldBean.getOutputText());
        return "helloPage";
    }

}