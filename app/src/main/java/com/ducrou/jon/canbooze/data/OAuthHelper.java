package com.ducrou.jon.canbooze.data;

import android.content.SharedPreferences;

import com.github.scribejava.apis.FitbitApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class OAuthHelper {

    private static String clientid = "22CXW7";
    private static String client_secret = "fa5ff232e5d56ee5058da626ea29be91";
    private static String code = "56ee0fa7f3d54443144e2367ff6850beecbb899b";

    private static OAuth20Service service = new ServiceBuilder(clientid)
            .apiSecret(client_secret)
            .scope("weight")
            .callback("ducrou://app/fitbit")
            .debug()
            .build(FitbitApi20.instance());


    public static String getAuthURL() {
        return service.getAuthorizationUrl();
    }

    public static OAuth2AccessToken getAccessToken(String code) {
        try {
            return service.getAccessToken(code);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response signAndRequest(OAuth2AccessToken accessToken, OAuthRequest request) {
        service.signRequest(accessToken, request);
        try {
            return service.execute(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static OAuth2AccessToken createToken(String accessToken,
                                                String tokenType,
                                                int expiresIn,
                                                String refreshToken,
                                                String scope,
                                                String rawResponse) {
        return new OAuth2AccessToken(accessToken, tokenType, expiresIn, refreshToken, scope, rawResponse);
    }


    public static void refresh(SharedPreferences sp) {
        if (!sp.contains("fitbit_refresh_token")) {
            return;
        }
        String refreshToken = sp.getString("fitbit_refresh_token", "");

        try {
            saveAccessToken(sp, service.refreshAccessToken(refreshToken));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void saveAccessToken(SharedPreferences sp, OAuth2AccessToken accessToken) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("fitbit_access_token", accessToken.getAccessToken());
        editor.putString("fitbit_refresh_token", accessToken.getRefreshToken());
        editor.putString("fitbit_scope", accessToken.getScope());
        editor.putString("fitbit_token_type", accessToken.getTokenType());
        editor.putInt("fitbit_expires_in", accessToken.getExpiresIn());
        editor.putString("fitbit_raw", accessToken.getRawResponse());
        editor.commit();
    }

    public static void clearAccessToken(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("fitbit_access_token");
        editor.remove("fitbit_refresh_token");
        editor.remove("fitbit_scope");
        editor.remove("fitbit_token_type");
        editor.remove("fitbit_expires_in");
        editor.remove("fitbit_raw");
        editor.commit();
    }

    public static Float getWeight(SharedPreferences sp) {
        OAuth2AccessToken accessToken = createToken(
                sp.getString("fitbit_access_token", ""),
                sp.getString("fitbit_token_type", ""),
                sp.getInt("fitbit_expires_im", 0),
                sp.getString("fitbit_refresh_token", ""),
                sp.getString("fitbit_scope", ""),
                sp.getString("fitbit_raw", ""));
        OAuth2AccessToken authToken = accessToken;
        final String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.fitbit.com/1/user/-/body/log/weight/date/" + date + ".json");
        final Response response = signAndRequest(authToken, request);

        try {
            String body = response.getBody();
            if (response.getCode() == 200) {
                try {
                    //all things are gooooooood
                    JSONObject obj = new JSONObject(body);
                    return new Float(((JSONObject) obj.getJSONArray("weight").get(0)).getDouble("weight"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //something bad happened

            if (response.getCode() == 401 && body.contains("expired_token")) {
                refresh(sp);
                return null;
            }

            clearAccessToken(sp);

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }
}
