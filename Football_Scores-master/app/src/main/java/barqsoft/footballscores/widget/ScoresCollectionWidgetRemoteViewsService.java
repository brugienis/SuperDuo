package barqsoft.footballscores.widget;


import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ScoresCollectionWidgetRemoteViewsService extends RemoteViewsService {

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.TIME_COL
    };
    private static final int ID_IDX = 0;
    private static final int HOME_IDX = 1;
    private static final int HOME_GOALS_IDX = 2;
    private static final int AWAY_IDX = 3;
    private static final int AWAY_GOALS_IDX = 4;
    private static final int TIME_IDX = 5;

    private final static String LOG_TAG = ScoresCollectionWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mCursor = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (mCursor != null) {
                    mCursor.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
                String currDate = dayFormat.format(System.currentTimeMillis());
                mCursor = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                        SCORE_COLUMNS, null, new String[]
                                {currDate}, DatabaseContract.scores_table.TIME_COL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
            }

            @Override
            public int getCount() {
                return mCursor == null ? 0 : mCursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        mCursor == null || !mCursor.moveToPosition(position)) {
                    return null;
                }
                String homeName = "No results found";
                int homeScore = -1;
                String awayName = "No results found";
                int awayScore = -1;
                String timeStr = "";

                String scores;

                homeName = mCursor.getString(HOME_IDX);
                homeScore = mCursor.getInt(HOME_GOALS_IDX);
                awayName = mCursor.getString(AWAY_IDX);
                awayScore = mCursor.getInt(AWAY_GOALS_IDX);
                timeStr = mCursor.getString(TIME_IDX);

                scores = Utilies.getScores(homeScore, awayScore);
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_scores_collection_list_item);

                views.setTextViewText(R.id.home_name, homeName);

                views.setTextViewText(R.id.time, timeStr);

                views.setTextViewText(R.id.scores, scores);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    views.setContentDescription(R.id.scores, scores);
                }

                views.setTextViewText(R.id.away_name, awayName);

                final Intent fillInIntent = new Intent();

                fillInIntent.putExtra(MainActivity.WIDGET_SELECTED_ROW_ID, mCursor.getInt(ID_IDX));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

//            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
//            private void setRemoteContentDescription(RemoteViews views, String description) {
//                views.setContentDescription(R.id.widget_icon, description);
//            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_scores_collection_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mCursor.moveToPosition(position))
                    return mCursor.getLong(ID_IDX);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}

