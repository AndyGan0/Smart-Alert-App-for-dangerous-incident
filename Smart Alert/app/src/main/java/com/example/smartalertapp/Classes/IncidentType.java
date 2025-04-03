package com.example.smartalertapp.Classes;

import java.io.Serializable;

public class IncidentType implements Serializable {

    //  Static Variables
    public static final IncidentType FIRE = new IncidentType(      "FIRE",      1,12, 0.1);
    public static final IncidentType EARTHQUAKE = new IncidentType("EARTHQUAKE",1,12,0.1);
    public static final IncidentType FLOOD = new IncidentType(     "FLOOD",     1,12,0.1);
    public static final IncidentType TORNADO = new IncidentType(   "TORNADO",   1,12,0.1);
    public static final IncidentType TSUNAMI = new IncidentType(   "TSUNAMI",   1,12,0.1);
    public static final IncidentType AVALANCHE = new IncidentType( "AVALANCHE", 1,12,0.1);


    public static IncidentType getIncidentTypeWithID(String ID){
        if ( ID.equals(IncidentType.FIRE.getTypeID()) ){
            return IncidentType.FIRE;
        }
        else if ( ID.equals(IncidentType.EARTHQUAKE.getTypeID()) ){
            return IncidentType.EARTHQUAKE;
        }
        else if ( ID.equals(IncidentType.FLOOD.getTypeID()) ){
            return IncidentType.FLOOD;
        }
        else if ( ID.equals(IncidentType.TORNADO.getTypeID()) ){
            return IncidentType.TORNADO;
        }
        else if ( ID.equals(IncidentType.TSUNAMI.getTypeID()) ){
            return IncidentType.TSUNAMI;
        }
        else if ( ID.equals(IncidentType.AVALANCHE.getTypeID()) ){
            return IncidentType.AVALANCHE;
        }
        return null;
    }





    //  Object variables and methods
    //  They can only be created from within the class
    private IncidentType(String id, int bias, int hours_to_die, double extra_radius){
        this.IncidentTypeID = id;
        this.bias = bias;
        this.elapsed_hours_to_die = hours_to_die;
        this.extra_radius = extra_radius;
    }



    //  Object variables
    private final String IncidentTypeID;
    private final int bias;
    private final int elapsed_hours_to_die;
    private final double extra_radius;





    //  Getters
    public String getTypeID(){
        return IncidentTypeID;
    }
    public int getBias(){
        return bias;
    }
    public int getHoursNeededToDie(){
        return elapsed_hours_to_die;
    }

    public double get_Extra_radius() {return extra_radius;}




}
