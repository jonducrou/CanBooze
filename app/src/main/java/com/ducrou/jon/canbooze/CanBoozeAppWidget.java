package com.ducrou.jon.canbooze;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import static android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CanBoozeSettingsActivity CanBoozeAppWidgetConfigureActivity}
 */
public class CanBoozeAppWidget extends AppWidgetProvider {
    public static final String ACTION_FORCE_UPDATE = "com.example.jon.appwidget.action.FORCE_UPDATE";

    enum STATE {
        UNKNOWN, WINE_YES, NO_BOOZE
    }
    private static STATE current = STATE.UNKNOWN;
    private static long lastUpdate = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        System.out.println("WIDGET UPDATE");
        // There may be multiple widgets active, so update all of them
        ComponentName thisWidget = new ComponentName(context, CanBoozeAppWidget.class);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int appWidgetId : allWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.can_booze_app_widget);
            views.setTextViewText(R.id.lower, "\uD83C\uDF77");

            switch (current) {
                case WINE_YES:
                    views.setTextViewText(R.id.upper, " ");
                    break;
                case NO_BOOZE:
                    views.setTextViewText(R.id.upper, "❌");
                    break;
                default:
                    views.setTextViewText(R.id.upper, "❔");
            }
            // Register an onClickListener
            Intent intent = new Intent(context, CanBoozeAppWidget.class);

            intent.setAction(ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            intent.putExtra("ForceRefresh", true);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            views.setOnClickPendingIntent(R.id.upper, pendingIntent);
            views.setOnClickPendingIntent(R.id.lower, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            CanBoozeIntentService.startActionUpdateWeight(context);
        }
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("CanBooze")) {
            System.out.println("CanBooze!");
            boolean canBooze = intent.getBooleanExtra("CanBooze", false);
            boolean error = intent.getBooleanExtra("Error", true);
            current = error ? STATE.UNKNOWN : (canBooze ? STATE.WINE_YES : STATE.NO_BOOZE);
            System.out.println("UPDATED TO " + current);
            if (System.currentTimeMillis() - lastUpdate > 10000)
            onUpdate(context, AppWidgetManager.getInstance(context), AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, CanBoozeAppWidget.class)));
        }

        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("ForceRefresh")) {
            onUpdate(context, AppWidgetManager.getInstance(context), AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, CanBoozeAppWidget.class)));
        }
    }
}

