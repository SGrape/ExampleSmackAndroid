package com.example.smack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dmolloy on 9/1/16.
 */
@SuppressWarnings("DefaultFileTemplate")
class PasswordValidator {

    private final Pattern pattern;
    private Matcher matcher;


    private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{7,20})";

    public PasswordValidator(){
        pattern = Pattern.compile(PASSWORD_PATTERN);
    }

    /**
     * Validate password with regular expression
     * @param password password for validation
     * @return true valid password, false invalid password
     */

    public boolean validate(final String password){
        matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
