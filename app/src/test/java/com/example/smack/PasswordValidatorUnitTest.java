package com.example.smack;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PasswordValidatorUnitTest {

    private PasswordValidator mPasswordValidator;

    @Before
    public void setUp(){
        mPasswordValidator = new PasswordValidator();
    }

    @Test
    public void password_Valid_ReturnsTrue(){
        assertEquals(true,mPasswordValidator.validate("College@13"));
    }

    @Test
    public void password_Invalid_ReturnsFalse(){
        assertEquals(false,mPasswordValidator.validate("dog1234"));
    }

    @Test
    public void password_Empty_ReturnsFalse(){
        assertEquals(false,mPasswordValidator.validate(""));
    }

    @After
    public void tearDown(){
        mPasswordValidator = null;
    }
}