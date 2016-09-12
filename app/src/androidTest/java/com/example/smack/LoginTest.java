package com.example.smack;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.filters.LargeTest;

import android.content.Context;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import java.util.Collection;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.action.ViewActions.typeText;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest {

    private String email;
    private String password;
    private String BtnConnected;
    private String BtnDisConnected;
    private String contact;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<LoginActivity>(LoginActivity.class);

    @Before
    public void initValidString(){
        email = "daithi@jwchat.org";
        password = "College@13";
        BtnConnected = "Disconnect";
        BtnDisConnected = "Connect";
        contact = "testingsmack@jwchat.org";
    }

    @Test
    public void testLogin(){
       // onView(withId(R.id.button)).perform(click()).check(matches(isDisplayed()));

        Activity login = getActivityInstance();
        onView(withId(R.id.ed_jid)).perform(typeText(email));
        onView(withId(R.id.ed_password)).perform(typeText(password));
        onView(withId(R.id.button)).perform(click());
        Activity chat = getActivityInstance();
        boolean b = (chat instanceof ChatActivity);
        assertTrue(b);
/*
        onView(withId(R.id.ed_to)).perform(typeText(contact));
        onView(withId(R.id.inputEditText)).perform(typeText("Message"));
        onView(withId(R.id.sendButton)).perform(click());
*/

    }

    public Activity getActivityInstance(){
        final Activity[] activities = new Activity[1];
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Activity currentActivity = null;
                Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                if (resumedActivities.iterator().hasNext()){
                    currentActivity = (Activity)resumedActivities.iterator().next();
                    activities[0] = currentActivity;
                }
            }
        });

        return activities[0];
    }
}
