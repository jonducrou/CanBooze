package com.ducrou.jon.canbooze;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.JobIntentService;

import com.ducrou.jon.canbooze.data.OAuthHelper;

import java.util.Calendar;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CanBoozeIntentService extends JobIntentService {
    private static final String ACTION_UPDATE_WEIGHT = "com.example.jon.appwidget.action.UPDATE_WEIGHT";
    private static final String ACTION_START_POLLIMG = "com.example.jon.appwidget.action.START_POLLING";
    private static final String FITBIT_LATEST_WEIGHT = "fitbit_latest_weight";

    static final int JOB_ID = 1001;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CanBoozeIntentService.class, JOB_ID, work);
    }

    public CanBoozeIntentService() {
        super();
    }

    public static void startActionUpdateWeight(Context context) {
        Intent intent = new Intent(context, CanBoozeIntentService.class);
        intent.setAction(ACTION_UPDATE_WEIGHT);
//        context.startService(intent);
        enqueueWork(context,intent);
    }

    public static void startActionStartPolling(Context context) {
        Intent intent = new Intent(context, CanBoozeIntentService.class);
        intent.setAction(ACTION_START_POLLIMG);
//        context.startService(intent);
        enqueueWork(context,intent);
        System.out.println("POLLING INTENT FIRED");
    }


    @Override
    protected void onHandleWork(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            System.out.println("INTENT!!!!: " + action);
            if (ACTION_UPDATE_WEIGHT.equals(action)) {
                handleActionUpdateWeight();
            } else if (ACTION_START_POLLIMG.equals(action)) {
                handleActionStartPolling();
            }
        }
    }

    private void handleActionUpdateWeight() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Float w = OAuthHelper.getWeight(sp);
        if (w == null) {
            System.out.println("NULL WEIGHT!?");
            return;
        }
        setWeight(w);
    }

    private void setWeight(Float w) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int targetWeight = Integer.parseInt(sp.getString("weight", "-1"));
        SharedPreferences.Editor editor = sp.edit();
        if (w == null) {
            editor.remove(FITBIT_LATEST_WEIGHT);
            editor.remove(FITBIT_LATEST_WEIGHT + "_S");
        } else {
            editor.putFloat(FITBIT_LATEST_WEIGHT, w);
            editor.putString(FITBIT_LATEST_WEIGHT + "_S", "" + w);
        }
        editor.commit();
        Intent intent = new Intent();
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra("Error", w == null);
        if (w != null) {
            intent.putExtra("CanBooze", w < targetWeight);
        }
        getApplicationContext().sendBroadcast(intent);
    }


    private void handleActionStartPolling() {

        Intent ii = new Intent(getApplicationContext(), CanBoozeIntentService.class);
        ii.setAction(CanBoozeIntentService.ACTION_UPDATE_WEIGHT);
        PendingIntent pii = PendingIntent.getService(getApplicationContext(), 2222, ii,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                AlarmManager.INTERVAL_HALF_HOUR, pii);
    }

}
