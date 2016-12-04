/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

/**
 * The face class creates an object that has the name, location and weights for an image
 * @author Anton
 */
public class Face implements java.io.Serializable {
    private String name;
    private String location;
    private double[] weights;
    
    public Face() {
        
    }
    
    public Face(String name, String location, double[] weights) {
        this.name = name;
        this.location = location;
        this.weights = weights;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the weights
     */
    public double[] getWeights() {
        return weights;
    }

    /**
     * @param weights the weights to set
     */
    public void setWeights(double[] weights) {
        this.weights = weights;
    }
}
