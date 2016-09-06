package com.rustic.wechat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HelloWorldController {
	
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
    public ModelAndView showMessage(@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
        System.out.println("in controller");
  
        ModelAndView mv = new ModelAndView("hello");
        mv.addObject("message", "Controller");
        mv.addObject("name", name);
        return mv;
    }

}
