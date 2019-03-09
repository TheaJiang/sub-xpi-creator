/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sap.capillary.xpi.entity;

/**
 *
 * @author I312865
 */
public class TreeNodeBean {
    
    private String text;
    private String hint;

    
    public TreeNodeBean() {
    }
    
    @Override
    public String toString(){
        return getText();
    }
    
    /**
     * @return the hint
     */
    public String getHint() {
        return hint;
    }

    /**
     * @param hint the hint to set
     */
    public void setHint(String hint) {
        this.hint = hint;
    }
    
        /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }
}
