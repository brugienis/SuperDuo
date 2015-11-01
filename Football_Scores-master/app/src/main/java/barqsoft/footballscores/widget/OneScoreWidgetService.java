package barqsoft.footballscores.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by business on 28/10/2015.
 */
public class OneScoreWidgetService  extends IntentService {

    private static int testCnt;

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.DATE_COL
    };
    private static final int HOME_INDEX = 0;
    private static final int HOME_GOALS_INDEX = 1;
    private static final int AWAY_INDEX = 2;
    private static final int AWAY_GOALS_INDEX = 3;
    private static final int DATE_INDEX = 4;

    private final static String LOG_TAG = OneScoreWidgetService.class.getSimpleName();

    public OneScoreWidgetService() {
        super("OneScoreWidgetService");
        Log.v(LOG_TAG, "constructor end");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "onHandleIntent start - action: " + intent.getAction());

        Cursor cursor = getContentResolver().query(DatabaseContract.BASE_CONTENT_URI,
                SCORE_COLUMNS, null, null, DatabaseContract.scores_table.HOME_COL + " ASC");

        if (cursor == null) {
            Log.e(LOG_TAG, "cursor is NULL");
            return;
        }
        if (cursor.getCount() == 0) {
//        if (!cursor.moveToFirst()) {
            Log.e(LOG_TAG, "cursor there is no data");
            cursor.close();
            return;
        }
        cursor.moveToLast();
        String homeName = cursor.getString(HOME_INDEX);
        Integer homeScore = cursor.getInt(HOME_GOALS_INDEX);
        String awayName = cursor.getString(AWAY_INDEX);
        Integer awayScore = cursor.getInt(AWAY_GOALS_INDEX);
        int date = cursor.getInt(DATE_INDEX);
        cursor.close();

        Log.v(LOG_TAG, "home: " + homeName + "; score: " + homeScore + "; away: " + awayName +
                "; score: " + awayScore + "; date" + date);

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                OneScoreWidgetProvider.class));

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.one_score_widget);
//            views.setTextViewText(R.id.home_name, "Everton FC");
//            views.setTextViewText(R.id.score, "6 - 2");
//            views.setTextViewText(R.id.date, "01:00 " + testCnt++);
//            views.setTextViewText(R.id.away_name, "Sunderland AFC");

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.oneScoreWidget, pendingIntent);


            views.setTextViewText(R.id.home_name, homeName);
            if (homeScore > -1) {
//                views.setTextViewText(R.id.homeScoreTextId, homeScore.toString());
//                views.setContentDescription(R.id.homeScoreTextId, homeScore.toString());
                views.setTextViewText(R.id.home_score, homeScore.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.home_score, homeScore.toString());
                }
            } else {
                views.setTextViewText(R.id.home_score, "?");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.home_score, "?");
                }
            }

            views.setTextViewText(R.id.date, date + ": " + testCnt++);

            views.setTextViewText(R.id.away_name, awayName);
            if (awayScore > -1) {
                views.setTextViewText(R.id.away_score, awayScore.toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.away_score, awayScore.toString());
                }
            } else {
                views.setTextViewText(R.id.away_score, "?");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.away_score, "?");
                }
            }


            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.v(LOG_TAG, "onHandleIntent called - updateAppWidget called - appWidgetId: " + appWidgetId);
        }
        Log.v(LOG_TAG, "onHandleIntent end - action: " + intent.getAction());
    }
}
