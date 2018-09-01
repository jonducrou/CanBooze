package com.ducrou.jon.canbooze;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.ducrou.jon.canbooze.data.OAuthHelper;
import com.github.scribejava.core.model.OAuth2AccessToken;

public class OAuthLandingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_landing);

        final String code = getIntent().getData().getQueryParameter("code");
        final Context c = getApplicationContext();
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);

        new TokenOperation().execute(code);
    }

    private class TokenOperation extends AsyncTask<String, Void, OAuth2AccessToken> {

        @Override
        protected OAuth2AccessToken doInBackground(String... strings) {
            final OAuth2AccessToken accessToken = OAuthHelper.getAccessToken(strings[0]);
            return accessToken;
        }

        @Override
        protected void onPostExecute(OAuth2AccessToken accessToken) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            OAuthHelper.saveAccessToken(sp, accessToken);
            Intent intent = new Intent();
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra("ForceRefresh", true);

            getApplicationContext().sendBroadcast(intent);
        }
    }
}
