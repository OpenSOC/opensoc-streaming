package com.opensoc.dataservices.common;



public class UserObject {

    public String name;

    public UserObject(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
