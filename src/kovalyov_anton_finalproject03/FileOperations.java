/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import java.io.File;
import java.util.List;

/**
 * The class for going through a directory recursively, and incrementing the name
 * of a file to be saved in case it already exists
 * @author Anton
 */
public class FileOperations {
    
    /**
     * The static recursive method to get all the files with a given type from a directory
     * @param directoryPath
     * @param type
     * @param results
     */
    public static void getFilesRecursively(String directoryPath, String type, List results) {
        File directory = new File(directoryPath);
        File[] fList = directory.listFiles();
        for (File file : fList){
            // If not a directory and has the right extension add to files list
            if (file.isFile() && getFileExtension(file).compareTo(type) == 0) {
                results.add(file);
            }
            // If it is a directory check it's files recursively
            else if (file.isDirectory()){
                getFilesRecursively(file.getPath(), type, results);
            }
        }
    }
    
    /**
     * The static method to get the extension/type of a file
     * @param file
     * @return a string
     */    
    public static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        }   
        catch (Exception e) {
            return "";
        }
    }
    
    /**
     * The static method to get the new name of a file if it already exists in the
     * given directory
     * @param name
     * @param type
     * @return the new name.type of the file
     */
    public static String getFileName(String name, String type) {
        File file = new File(name + "." + type);
        int fileNo = 1;
        String fileName = name;
        if (file.exists() && !file.isDirectory()) {
            while(file.exists()) {
                file = new File(name + "(" + fileNo + ")" + "." + type);
                fileName = name + "(" + fileNo + ")";
                fileNo++;
            }
        } 
        return fileName += "." + type;
    }
}
