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

import java.text.SimpleDateFormat;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

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
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.MATCH_DAY
    };
    private static final int HOME_IDX = 0;
    private static final int HOME_GOALS_IDX = 1;
    private static final int AWAY_IDX = 2;
    private static final int AWAY_GOALS_IDX = 3;
    private static final int TIME_IDX = 4;
    private static final int DATE_IDX = 5;
    private static final int MATCH_DAY_IDX = 6;

    private final static String LOG_TAG = OneScoreWidgetService.class.getSimpleName();

    public OneScoreWidgetService() {
        super("OneScoreWidgetService");
        Log.v(LOG_TAG, "constructor end");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOG_TAG, "onHandleIntent start - action: " + intent.getAction());

        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currDate = dayFormat.format(System.currentTimeMillis());
        boolean isRightToLeft = getResources().getBoolean(R.bool.is_right_to_left);
        Log.v(LOG_TAG, "onHandleIntent - currDate/isRightToLeft: " + currDate + "/" + isRightToLeft);
        boolean noResultsFound = false;
        Cursor cursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                SCORE_COLUMNS, null, new String[] {currDate}, DatabaseContract.scores_table.HOME_COL + " DESC");

        String homeName = "No results found";
        int homeScore = -1;
        String awayName = "No results found";
        int awayScore = -1;
        String timeStr = "";

        String scores;
        if (cursor == null) {
            Log.e(LOG_TAG, "onHandleIntent - cursor is NULL");
            noResultsFound = true;
        } else if (cursor.getCount() == 0) {
//        if (!cursor.moveToFirst()) {
            Log.e(LOG_TAG, "onHandleIntent - cursor there is no data");
            noResultsFound = true;
            cursor.close();
        } else {
            cursor.moveToFirst();
            homeName = cursor.getString(HOME_IDX);
            homeScore = cursor.getInt(HOME_GOALS_IDX);
            awayName = cursor.getString(AWAY_IDX);
            awayScore = cursor.getInt(AWAY_GOALS_IDX);
            timeStr = cursor.getString(TIME_IDX);
            cursor.close();
        }

        scores = Utilies.getScores(homeScore, awayScore, isRightToLeft);
        Log.e(LOG_TAG, "onHandleIntent - homeScore/awayScore/scores: " + homeScore + "/" + awayScore + "/" + scores);

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                OneScoreWidgetProvider.class));

        Log.e(LOG_TAG, "onHandleIntent - processing widgets - count/noResultsFound: " + appWidgetIds.length + "/" + noResultsFound);
        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.one_score_widget);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.oneScoreWidget, pendingIntent);
            Log.e(LOG_TAG, "onHandleIntent - onClick set");

            views.setTextViewText(R.id.home_name, homeName);

            views.setTextViewText(R.id.time, timeStr + ": " + testCnt++);

            views.setTextViewText(R.id.scores, scores);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                views.setContentDescription(R.id.scores, scores);
            }

            views.setTextViewText(R.id.away_name, awayName);

            Log.v(LOG_TAG, "scores: " + Utilies.getScores(homeScore, awayScore, isRightToLeft));

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.v(LOG_TAG, "onHandleIntent called - updateAppWidget called - appWidgetId: " + appWidgetId);
        }
        Log.v(LOG_TAG, "onHandleIntent end - action: " + intent.getAction());
    }
}
