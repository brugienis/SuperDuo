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

//    private static int testCnt;

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL
    };
    private static final int HOME_IDX = 0;
    private static final int HOME_GOALS_IDX = 1;
    private static final int AWAY_IDX = 2;
    private static final int AWAY_GOALS_IDX = 3;
    private static final int TIME_IDX = 4;

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
//        boolean isRightToLeft = getResources().getBoolean(R.bool.is_right_to_left);
        // get the most recent results for today with scores
        Cursor cursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                SCORE_COLUMNS, null, new String[]
                        {currDate}, DatabaseContract.scores_table.TIME_COL + " DESC");

        // FIXME: 9/11/2015 move to strings and show 'retrieving' at the start and 'not found' if not found
        String homeName = getResources().getString(R.string.retrieving_scores);
        String awayName  = getResources().getString(R.string.retrieving_scores);
        int homeScore = -1;
        int awayScore = -1;
        String timeStr = "";

        String scores;
        if (cursor == null || cursor.getCount() == 0) {
            Log.e(LOG_TAG, "onHandleIntent - cursor is NULL");
            homeName = awayName = getResources().getString(R.string.empty_scores_list);
        } else {
            cursor.moveToFirst();
            while (!cursor.isBeforeFirst()) {
                homeName = cursor.getString(HOME_IDX);
                homeScore = cursor.getInt(HOME_GOALS_IDX);
                awayName = cursor.getString(AWAY_IDX);
                awayScore = cursor.getInt(AWAY_GOALS_IDX);
                timeStr = cursor.getString(TIME_IDX);
                if (homeScore > -1) {
                    break;
                }
            }
        }

        cursor.close();

        scores = Utilies.getScores(homeScore, awayScore);

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                OneScoreWidgetProvider.class));

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.one_score_widget);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.oneScoreWidget, pendingIntent);
            Log.e(LOG_TAG, "onHandleIntent - onClick set");

            views.setTextViewText(R.id.home_name, homeName);

            views.setTextViewText(R.id.time, timeStr);

            views.setTextViewText(R.id.scores, scores);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                views.setContentDescription(R.id.scores, scores);
            }

            views.setTextViewText(R.id.away_name, awayName);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.v(LOG_TAG, "onHandleIntent called - updateAppWidget called - appWidgetId: " + appWidgetId);
        }
        Log.v(LOG_TAG, "onHandleIntent end - action: " + intent.getAction());
    }
}
