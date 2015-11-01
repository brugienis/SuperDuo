package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by business on 14/10/2015.
 */
public class ScoresWidgetService extends IntentService {

    private static int testCnt;

    private final static String LOG_TAG = ScoresWidgetService.class.getSimpleName();

    public ScoresWidgetService() {
        super("ScoresWidgetService");
        Log.v(LOG_TAG, "constructor end");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "onHandleIntent start - action: " + intent.getAction());
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                ScoresWidgetProvider.class));

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_detail_test);
            views.setTextViewText(R.id.widgetText, "test: " + testCnt++);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widgetTest, pendingIntent);


            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.v(LOG_TAG, "onHandleIntent called - updateAppWidget called - appWidgetId: " + appWidgetId);
        }
        Log.v(LOG_TAG, "onHandleIntent end - action: " + intent.getAction());
    }
}
