package com.example.smack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by dmolloy on 9/5/16.
 */
public class SingletonUnitTest {

    private Singleton mSingleton;

    @Before
    public void setUp(){
        mSingleton = Singleton.getInstance();
    }

    @Test
    public void variableTest() {
        assertSame("", mSingleton.getmUser());
        assertSame("", mSingleton.getmPass());

        mSingleton.setmPass("NotAVeryGoodPassword");
        mSingleton.setmUser("USER");

        assertSame("NotAVeryGoodPassword",mSingleton.getmPass());
        assertSame("USER",mSingleton.getmUser());
    }

    @After
    public void tearDown(){
        mSingleton = null;
    }
}
