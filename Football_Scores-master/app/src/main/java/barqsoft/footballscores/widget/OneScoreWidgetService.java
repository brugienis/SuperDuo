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
        Log.v(LOG_TAG, "onHandleIntent - currDate: " + currDate);
        Cursor cursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                SCORE_COLUMNS, null, new String[] {currDate}, DatabaseContract.scores_table.HOME_COL + " ASC");

        if (cursor == null) {
            Log.e(LOG_TAG, "cursor is NULL");
            return;
        }

        if (cursor.getCount() == 0) {
//        if (!cursor.moveToFirst()) {
            Log.e(LOG_TAG, "cursor there is no data");
            cursor.close();
            return;
        } else {
            cursor.moveToFirst();
            String homeName;
            Integer homeScore;
            String awayName;
            Integer awayScore;
            int time;
            String timeStr;
            int matchDay;
            String date;

            String scores;
//            cursor.moveToLast();
            while (true) {
                homeName = cursor.getString(HOME_IDX);
                homeScore = cursor.getInt(HOME_GOALS_IDX);
                awayName = cursor.getString(AWAY_IDX);
                awayScore = cursor.getInt(AWAY_GOALS_IDX);
                time = cursor.getInt(TIME_IDX);
                timeStr = cursor.getString(TIME_IDX);
                date = cursor.getString(DATE_IDX);
                matchDay = cursor.getInt(MATCH_DAY_IDX);

                scores = Utilies.getScores(homeScore, awayScore);

                Log.v(LOG_TAG, "home: " + homeName + "; score: " + homeScore + "; away: " + awayName +
                        "; score: " + awayScore + "; time: " + time + "; timeStr: " + timeStr + "; date: " + date + "; matchDay: " + matchDay);

                if (cursor.isLast()) {
                    cursor.close();
                    break;
                } else {
                    cursor.moveToNext();
                }
            }

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

//                views.setTextViewText(R.id.date, date + ": " + testCnt++);
                views.setTextViewText(R.id.date, timeStr + ": " + testCnt++);

                views.setTextViewText(R.id.scores, scores);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.scores, scores);
                }

                views.setTextViewText(R.id.away_name, awayName);

                Log.v(LOG_TAG, "scores: " + Utilies.getScores(homeScore, awayScore));

                // Tell the AppWidgetManager to perform an update on the current app widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
                Log.v(LOG_TAG, "onHandleIntent called - updateAppWidget called - appWidgetId: " + appWidgetId);
            }
            Log.v(LOG_TAG, "onHandleIntent end - action: " + intent.getAction());
        }
    }
}
