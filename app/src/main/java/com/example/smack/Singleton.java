package com.example.smack;

/**
 * Created by dmolloy on 9/1/16.
 */
@SuppressWarnings("DefaultFileTemplate")
public class Singleton {

    private static Singleton mInstance = null;

    private String mUser;
    private String mPass;

    private Singleton(){
        mUser = "";
        mPass = "";
    }

    public static Singleton getInstance(){
        if(mInstance == null)
        {
            mInstance = new Singleton();
        }
        return mInstance;
    }

    public String getmUser(){
        return this.mUser;
    }

    public void setmUser(String value){
        mUser = value;
    }

    public String getmPass(){
        return this.mPass;
    }

    public void setmPass(String value){
        mPass = value;
    }
}
