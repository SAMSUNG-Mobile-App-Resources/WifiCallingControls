package com.ajhodges.wificallingcontrols.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.ajhodges.wificallingcontrols.Constants;
import com.ajhodges.wificallingcontrols.R;
import com.ajhodges.wificallingcontrols.bundle.PluginBundleManager;
import com.ajhodges.wificallingcontrols.ipphone.WifiCallingManager;

/**
 * Created by Adam on 3/13/14.
 */
public class ToggleWidgetProvider extends AppWidgetProvider {
    public final static String EXTRA_WIDGET_IDS = "ToggleWidgetProviderID";
    public final static String EXTRA_WIDGET_TOGGLE = "ToggleWidgetProviderToggle";

    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.hasExtra(EXTRA_WIDGET_IDS)){
            int[] ids = intent.getIntArrayExtra(EXTRA_WIDGET_IDS);
            Boolean ipphoneEnabled = null;
            Boolean widgetUpdating = context.getSharedPreferences("ToggleWidgetProvider", Context.MODE_PRIVATE).getBoolean("widgetUpdating", false);
            if(widgetUpdating) {
                return;
            }
            else if(intent.hasExtra(EXTRA_WIDGET_TOGGLE)){
                //set the widget background to reflect the new Wifi Calling State
                ipphoneEnabled = !intent.getBooleanExtra(EXTRA_WIDGET_TOGGLE, false);

                //This intent is coming from a widget click event! Broadcast toggle pendingintent
                Intent fireIntent = new Intent();
                fireIntent.setAction(com.twofortyfouram.locale.Intent.ACTION_FIRE_SETTING);

                final Bundle resultBundle = PluginBundleManager.generateBundle(context, ipphoneEnabled ? 1 : 0);
                fireIntent.putExtra(com.twofortyfouram.locale.Intent.EXTRA_BUNDLE, resultBundle);

                context.getSharedPreferences("ToggleWidgetProvider", Context.MODE_PRIVATE).edit().putBoolean("widgetUpdating", true);
                Log.v(Constants.LOG_TAG, "Button clicked, toggling Wifi Calling state.");
                context.sendBroadcast(fireIntent);
            }
            //Noticed a change in our settings... update widgets!
            this.update(context, AppWidgetManager.getInstance(context), ids, ipphoneEnabled);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        super.onUpdate(context,appWidgetManager,appWidgetIds);
        update(context,appWidgetManager,appWidgetIds, null);
    }

    private void update(Context context, AppWidgetManager manager, int[] ids, Boolean ipphoneEnabled){
        //Called once per widget to initialize the state
        Log.v(Constants.LOG_TAG, "onUpdate: updating widget state");

        //get the current Wifi Calling state
        if(ipphoneEnabled == null)
            ipphoneEnabled = WifiCallingManager.getInstance(context).getIPPhoneEnabled(context);

        for(int i : ids){
            //set onClick to update the widget and toggle the Wifi Calling state
            Intent updateWidgets = new Intent();
            updateWidgets.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateWidgets.putExtra(EXTRA_WIDGET_IDS, ids);
            updateWidgets.putExtra(EXTRA_WIDGET_TOGGLE, ipphoneEnabled);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, updateWidgets, PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.toggle_button, pendingIntent);

            //set the widget background to reflect the current Wifi Calling State
            views.setImageViewResource(R.id.toggle_button, (ipphoneEnabled ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off));

            manager.updateAppWidget(i, views);
        }
    }
}
