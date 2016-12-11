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
public class PhoneVerificationActivityTest {

    @Rule
    public ActivityTestRule<PhoneVerificationActivity> mPhoneVerificationActivityRule =
            new ActivityTestRule<>(PhoneVerificationActivity.class);

    @Test
    public void checkLoginMessageDisplay() {
        onView(withId(R.id.textView2)).check(matches(withText(R.string.sms_charge_msg)));
    }

    @Test
    public void failCheckLoginMessageDisplay() {
        onView(withId(R.id.textView2)).check(matches(withText(R.string.app_name)));
    }

    @Test
    public void checkVerifyButton() {
        onView(withId(R.id.btn_verify_phone)).check(matches(withText(R.string.verify_text)));
    }

    @Test
    public void failCheckVerifyButton() {
        onView(withId(R.id.btn_verify_phone)).check(matches(withText("failed check")));
    }

}
