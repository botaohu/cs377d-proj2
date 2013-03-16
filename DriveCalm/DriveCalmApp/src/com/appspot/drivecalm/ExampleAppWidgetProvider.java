package com.appspot.drivecalm;

import java.util.Random;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class ExampleAppWidgetProvider extends AppWidgetProvider {

  private static final String ACTION_CLICK = "ACTION_CLICK";

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {

	  final int N = appWidgetIds.length;

      // Perform this loop procedure for each App Widget that belongs to this provider
      for (int i=0; i<N; i++) {
          int appWidgetId = appWidgetIds[i];

          // Create an Intent to launch ExampleActnActivityivity
          Intent intent = new Intent(context, MainService.class);
         // PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
          PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
          // Get the layout for the App Widget and attach an on-click listener
          // to the button
          RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.activity_main);
          views.setOnClickPendingIntent(R.id.button1, pendingIntent);
          // Tell the AppWidgetManager to perform an update on the current app widget
          appWidgetManager.updateAppWidget(appWidgetId, views);
      }
    
  }
} 