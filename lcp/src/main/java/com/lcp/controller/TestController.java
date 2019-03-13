package com.lcp.controller;

import com.lcp.bean.Student;
import com.lcp.mvcannotation.Autowired;
import com.lcp.mvcannotation.Controller;
import com.lcp.mvcannotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestController {
    @Autowired("student")
    Student student;
    
    @RequestMapping("/index")
    public void test() {
    	
    }
}
