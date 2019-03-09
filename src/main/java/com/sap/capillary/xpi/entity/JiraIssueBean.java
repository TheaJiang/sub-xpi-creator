/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.capillary.xpi.entity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author I312865
 */
public class JiraIssueBean extends TreeNodeBean {

    private String issueKey;
    private String projectKey;
    private String componentId;
    private String componentName;
    private String description;
    private String fixVersionId;
    private String fixVersionName;
    private String customfield_10002Value;
    private String issuetype;
    private String summary;
    private String keyword;
    private String startDate;
    private String dueDate;
    private Boolean createFlag = false;
    private Boolean mainFlag = false;
    
    public JiraIssueBean(){
    }
    
    private String jsonBuilder(String formatString, String name, String value){
        if(value==null||value.trim().isEmpty())
            return "";
        else
            return String.format(formatString, name, value);
    }
    
    public String toJson(){
        StringBuilder sb = new StringBuilder();
        sb.append(jsonBuilder("\"%s\": { \"key\": \"%s\" },", "project", getProjectKey()));
        sb.append(jsonBuilder("\"%s\": [ { \"id\": \"%s\" } ],", "components", getComponentId()));
        sb.append(jsonBuilder("\"%s\": [ { \"id\": \"%s\" } ],", "fixVersions", getFixVersionId()));
        sb.append(jsonBuilder("\"%s\": { \"value\": \"%s\" },", "customfield_10002", getCustomfield_10002Value()));
        sb.append(jsonBuilder("\"%s\": { \"id\": \"%s\" }, ", "issuetype", getIssuetype()));
        sb.append(jsonBuilder("\"%s\": \"%s\",", "summary", getSummary()));
        sb.append(jsonBuilder("\"%s\": \"%s\"", "description", getDescription()));
        return String.format("{ \"fields\": { %s }}", sb.toString());
    }
    
    public String toJsonNo10002(){
        StringBuilder sb = new StringBuilder();
        sb.append(jsonBuilder("\"%s\": { \"key\": \"%s\" },", "project", getProjectKey()));
        sb.append(jsonBuilder("\"%s\": [ { \"id\": \"%s\" } ],", "components", getComponentId()));
        sb.append(jsonBuilder("\"%s\": [ { \"id\": \"%s\" } ],", "fixVersions", getFixVersionId()));
        //sb.append(jsonBuilder("\"%s\": { \"value\": \"%s\" },", "customfield_10002", getCustomfield_10002Value()));
        sb.append(jsonBuilder("\"%s\": { \"id\": \"%s\" }, ", "issuetype", getIssuetype()));
        sb.append(jsonBuilder("\"%s\": \"%s\",", "summary", getSummary()));
        sb.append(jsonBuilder("\"%s\": \"%s\"", "description", getDescription()));
        return String.format("{ \"fields\": { %s }}", sb.toString());
    }
    
    public static String[] head2Array(){
        return new String[]{"project","component name","component id", "fix version","ticket priority","issuetype","summary","description"};
    }
    public String toCsv(){
        return String.format("%s,%s,%s,%s,%s,%s,%s",
                projectKey,
                componentName, // none json
                componentId,
                fixVersionId,
                customfield_10002Value,
                issuetype,
                summary,
                description);
    }
    public List<String> toArray(){
        List<String> a = new ArrayList();
        a.add(getProjectKey());
        a.add(getComponentName());
        a.add(getComponentId());
        a.add(getFixVersionId());
        a.add(getCustomfield_10002Value());
        a.add(getIssuetype());
        a.add(getSummary());
        a.add(getDescription());
        return a;
    }
    
    public static void toCsv(List<JiraIssueBean> jibList, String filePath) throws Exception{
        String newline = System.getProperty("line.separator");
        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(newline);
        try{
            fileWriter= new FileWriter(filePath);
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
            csvFilePrinter.printRecord(head2Array());
            for(JiraIssueBean jib: jibList){
                csvFilePrinter.printRecord(jib.toArray());
            }
        } catch(Exception ex) {
            throw ex;
        } finally{
            try{
                if(fileWriter!=null){
                    fileWriter.flush();
                    fileWriter.close();
                }
                if(csvFilePrinter!=null)
                    csvFilePrinter.close();
            } catch(Exception ex){
            }
        }
    }
    
    public static List<JiraIssueBean> fromCsv(InputStream file){
        List<JiraIssueBean> jibList = new ArrayList<JiraIssueBean>();
        try {
            CSVParser parser = CSVParser.parse(file, Charset.forName("UTF-8"), CSVFormat.DEFAULT);
            boolean firstline = true;
            for (CSVRecord csvRecord : parser) {
                if(firstline){
                    firstline = false;
                    continue;
                }
                JiraIssueBean jib = new JiraIssueBean();
                jib.setProjectKey(csvRecord.get(0));
                //jib.setComponentName(csvRecord.get(1));
                jib.setComponentId(csvRecord.get(2));
                jib.setFixVersionId(csvRecord.get(3));
                jib.setCustomfield_10002Value(csvRecord.get(4));
                //jib.setIssuetype(csvRecord.get(5));
                jib.setSummary(csvRecord.get(6));
                jib.setDescription(csvRecord.get(7));
                jibList.add(jib);
            }
        } catch (IOException ex) {
            
        }
        return jibList;
    }

    /**
     * @return the project_key
     */
    public String getProjectKey() {
        return projectKey;
    }

    /**
     * @param project_key the project_key to set
     */
    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    /**
     * @return the components_id
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * @param components_id the components_id to set
     */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the customfield_10002_value
     */
    public String getCustomfield_10002Value() {
        return customfield_10002Value;
    }

    /**
     * @param customfield_10002_value the customfield_10002_value to set
     */
    public void setCustomfield_10002Value(String customfield_10002Value) {
        this.customfield_10002Value = customfield_10002Value;
    }

    /**
     * @return the issuetype
     */
    public String getIssuetype() {
        issuetype = "167";
        return issuetype;
    }

    /**
     * @param issuetype the issuetype to set
     */
    /* always 167
    public void setIssuetype(String issuetype) {
        this.issuetype = issuetype;
    }
    */

    /**
     * @return the summary
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return the keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * @param keyword the keyword to set
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * @return the createFlag
     */
    public Boolean getCreateFlag() {
        return createFlag;
    }

    /**
     * @param createFlag the createFlag to set
     */
    public void setCreateFlag(Boolean createFlag) {
        this.createFlag = createFlag;
    }

    /**
     * @return the mainFlag
     */
    public Boolean getMainFlag() {
        return mainFlag;
    }

    /**
     * @param mainFlag the mainFlag to set
     */
    public void setMainFlag(Boolean mainFlag) {
        this.mainFlag = mainFlag;
    }

    /**
     * @return the issueKey
     */
    public String getIssueKey() {
        return issueKey;
    }

    /**
     * @param issueKey the issueKey to set
     */
    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    } 
    
    /**
     * @return the startDate
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the dueDate
     */
    public String getDueDate() {
        return dueDate;
    }

    /**
     * @param dueDate the dueDate to set
     */
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * @return the fixVersions
     */
    public String getFixVersionId() {
        return fixVersionId;
    }

    /**
     * @param fixVersionId the fixVersions to set
     */
    public void setFixVersionId(String fixVersionId) {
        this.fixVersionId = fixVersionId;
    }
    
        /**
     * @return the componentName
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * @param componentName the componentName to set
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     * @return the fixVersionName
     */
    public String getFixVersionName() {
        return fixVersionName;
    }

    /**
     * @param fixVersionName the fixVersionName to set
     */
    public void setFixVersionName(String fixVersionName) {
        this.fixVersionName = fixVersionName;
    }
}
