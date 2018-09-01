package com.ducrou.jon.canbooze;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;

import com.ducrou.jon.canbooze.data.OAuthHelper;

public class OAuthSigninActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_signin);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(OAuthHelper.getAuthURL()));

    }
}
