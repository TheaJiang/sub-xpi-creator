package com.sap.capillary.xpi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import com.sap.capillary.xpi.controller.AuthController;
import com.sap.capillary.xpi.controller.BusinessLogic;
import com.sap.capillary.xpi.controller.HttpOp;
import com.sap.capillary.xpi.entity.TreeNodeBean;

@SpringBootApplication
public class XpiApplication {
  
    public static JSONArray tree = null;
	public static void main(String[] args) throws Exception{
	    ConfigurableApplicationContext  context= SpringApplication.run(XpiApplication.class, args);
	    
		String username = context.getEnvironment().getProperty("jira.author");
	    String password = context.getEnvironment().getProperty("jira.pwd");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(new TimerTask() {
            public void run() {
            	try {
					getProjects(username, password);
				} catch (Exception e) {
					System.out.println(e.toString());
				}
            }
        }, 0, 12, TimeUnit.HOURS);
	}
	
	private static void getProjects(String username, String password) throws Exception {
		BusinessLogic bl = new BusinessLogic();
		bl.setHttpOp(username, password);
		List<TreeNodeBean> tnbList = bl.getAllProject();
		JSONArray jsonArrProj = new JSONArray();
		for(int i=0; i<tnbList.size();i++) {
		    String project = tnbList.get(i).getText();
		    JSONObject jsonObjProj = new JSONObject();
		    jsonObjProj.put("text", project);
		    jsonObjProj.put("projectName", tnbList.get(i).getHint());
		    Map<String, String> compMap = bl.getAllComponents(project);
		    if(compMap!=null && compMap.size()>0) {
		        JSONArray jsonArrComp = new JSONArray();
		        for(String key: compMap.keySet()) {
		            JSONObject jsonObjComp = new JSONObject();
		            jsonObjComp.put("componentId", key);
		            String value = compMap.get(key);
		            jsonObjComp.put("text", value);
		            jsonArrComp.put(jsonObjComp);
		        }
		        jsonObjProj.put("nodes", jsonArrComp);
		    }
		    jsonArrProj.put(jsonObjProj);
		}
		System.out.println(jsonArrProj.toString());
		tree = jsonArrProj;
    }
}
