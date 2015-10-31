package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by business on 28/10/2015.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class OneScoreWidgetProvider extends AppWidgetProvider {

    private final static String LOG_TAG = OneScoreWidgetProvider.class.getSimpleName();

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(LOG_TAG, "onUpdate start");
        context.startService(new Intent(context, OneScoreWidgetService.class));
        Log.v(LOG_TAG, "onUpdate - intent to start ScoresWidgetService sent");
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        Log.v(LOG_TAG, "onReceive called - action: " + intent.getAction());
//        if (SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
//            context.startService(new Intent(context, ScoresWidgetService.class));
//        }
    }
}