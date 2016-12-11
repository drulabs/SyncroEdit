package org.drulabs.syncroedit;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Authored by KaushalD on 10/5/2016.
 */

@RunWith(AndroidJUnit4.class)
public class SignInActivityTest {

    @Rule
    public ActivityTestRule<SignInActivity> mSignInActivityRule =
            new ActivityTestRule<>(SignInActivity.class);

    @Test
    public void checkLoginMessageDisplay() {
        onView(withId(R.id.textView)).check(matches(withText(R.string.sign_in_text)));
    }

}
