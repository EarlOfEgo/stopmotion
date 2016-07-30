package com.sthagios.stopmotion;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import android.os.SystemClock;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Stopmotion
 *
 * @author stephan
 * @since 30.07.16
 */
@RunWith(JUnit4.class)
public class Screenshots {

    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testTakeScreenshot() {
        //Sleep to load all images
        SystemClock.sleep(2000);
        Screengrab.screenshot("main_screen");
        onView(withId(R.id.fab)).perform(click());

        //Doesn't work, fastane issue
//        onView(withId(R.id.container_amount)).perform(click());
//        SystemClock.sleep(2000);
//        Screengrab.screenshot("burst_amount");
//        pressBack();

        onView(withId(R.id.button_capture)).perform(click());

        SystemClock.sleep(4000);
        Screengrab.screenshot("images_overview");
        pressBack();
        pressBack();

        onView(withId(R.id.recyclerViewImageList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        SystemClock.sleep(1000);
        onView(withId(R.id.expand_button)).perform(click());
        SystemClock.sleep(1000);
        Screengrab.screenshot("view_image");
    }
}
