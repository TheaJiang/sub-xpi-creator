package com.sap.capillary.xpi.controller;

import java.util.Base64;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@EnableAutoConfiguration
public class AuthController {

    private HttpOp httpOp = null;
    public AuthController() {
        httpOp = new HttpOp();
    }
    
    @RequestMapping("/")
    public String index() {
        return "login";
    }
    
    @RequestMapping("/login")
    public String login(@RequestParam Map<String,Object> map, HttpServletRequest request, Model model) {
        if(map==null || map.get("action")==null || !map.get("action").toString().equalsIgnoreCase("login"))
            return "login";
        String username = map.get("txtUserCd").toString();
        String pwd = map.get("txtUserPwd").toString();
        return login(request, username, pwd, model);
    }

    public String login(HttpServletRequest request, String username, String pwd, Model model) {
        String basic_auth = "Basic " + Base64.getEncoder().encodeToString(
                (username + ":" + pwd).getBytes());
        httpOp.basic_auth = basic_auth;
        String postBody = String.format(
        		"{\"username\": \"%s\",\"password\": \"%s\"}", username, pwd);
        try {
            httpOp.postHttpRequest("https://jira.successfactors.com/rest/auth/1/session", postBody);
        } catch (Exception ex) {
        	model.addAttribute("loginError", true);
            return "login";
        }
        HttpSession hs = request.getSession();
        HttpOp httpOp = new HttpOp(basic_auth);
        hs.setAttribute("http_operation", httpOp);
        model.addAttribute("logout", true);
        return "main_menu";
    }
    
    @RequestMapping("/logout")
    public String loginError(HttpServletRequest request) {
    	HttpSession hs = request.getSession();
        Object op = hs.getAttribute("http_operation");
        if(op!=null)
        	hs.removeAttribute("http_operation");
        return "login";
    }

}
