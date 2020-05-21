/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cml.beans;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author benne
 */
public class Script {
    private File file;
    private String classname;
    private Map<String, String> data = new HashMap();

    public Script(File file, String classname) {
        this.file = file;
        this.classname = classname;
    }
    
    public Script() {
        
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
    
}
