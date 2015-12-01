/*
 * Copyright (C) 2013 The Android Open Source Project
 */

package barqsoft.footballscores.util;

import barqsoft.footballscores.R;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies {

    private static final int SERIE_A = 357;
    private static final int PREMIER_LEGAUE = 354;
    private static final int CHAMPIONS_LEAGUE = 362;
    private static final int PRIMERA_DIVISION = 358;
    private static final int BUNDESLIGA = 351;

    private static final String SERIA_A_TEXT = "Seria A";
    private static final String PREMIER_LEAGUE_TEXT = "Premier League";
    private static final String UEFA_CHAMPIONS_LEAGUE_LEXT = "UEFA Champions League";
    private static final String PRIMERA_DIVISION_TEXT = "Primera Division";
    private static final String BUNDESLIGA_TEXT = "Bundesliga";
    private static final String UNKNOWN_LEAGUE_TEXT = "Not known League Please report";

    public static String getLeague(int league_num) {
        switch (league_num) {
            case SERIE_A:
                return SERIA_A_TEXT;
            case PREMIER_LEGAUE:
                return PREMIER_LEAGUE_TEXT;
            case CHAMPIONS_LEAGUE:
                return UEFA_CHAMPIONS_LEAGUE_LEXT;
            case PRIMERA_DIVISION:
                return PRIMERA_DIVISION_TEXT;
            case BUNDESLIGA:
                return BUNDESLIGA_TEXT;
            default:
                return UNKNOWN_LEAGUE_TEXT;
        }
    }

    private static final String GROUP_STAGES = "Group Stages, Matchday : 6";
    private static final String FIRST_KNOCKOUT = "First Knockout round";
    private static final String QUARTER_FINAL = "QuarterFinal";
    private static final String SEMI_FINAL = "SemiFinal";
    private static final String FINAL = "Final";
    private static final String MATCH_DAY = "Match day : ";

    public static String getMatchDay(int matchDay, int leagueNum) {
        if (leagueNum == CHAMPIONS_LEAGUE) {
            if (matchDay <= 6) {
                return GROUP_STAGES;
            } else if (matchDay == 7 || matchDay == 8) {
                return FIRST_KNOCKOUT;
            } else if (matchDay == 9 || matchDay == 10) {
                return QUARTER_FINAL;
            } else if (matchDay == 11 || matchDay == 12) {
                return SEMI_FINAL;
            } else {
                return FINAL;
            }
        } else {
            return MATCH_DAY + String.valueOf(matchDay);
        }
    }

    private static final String HYPHEN = " - ";

    public static String getScores(int homeGoals, int awayGoals) {
        if (homeGoals < 0 || awayGoals < 0) {
            return HYPHEN;
        } else
            return String.valueOf(homeGoals) + HYPHEN + String.valueOf(awayGoals);
    }

    private static final String ARSENAL_LONDON_FC = "Arsenal London FC";
    private static final String MANCHESTER_UNITED_FC = "Manchester United FC";
    private static final String SWANSEA_CITY = "Swansea City";
    private static final String LEICESTER_CITY = "Leicester City";
    private static final String EVERTON_FC = "Everton FC";
    private static final String WEST_HAM_UNITED_FC = "West Ham United FC";
    private static final String TOTTENHAM_HOTSPUR_FC = "Tottenham Hotspur FC";
    private static final String WEST_BROMWICH_ALBION = "West Bromwich Albion";
    private static final String SUNDERLAND_AFC = "Sunderland AFC";
    private static final String STOKE_CITY_FC = "Stoke City FC";

    public static int getTeamCrestByTeamName(String teamName) {
        if (teamName == null) {
            return R.drawable.no_icon;
        }
        switch (teamName) { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case ARSENAL_LONDON_FC:
                return R.drawable.arsenal;
            case MANCHESTER_UNITED_FC:
                return R.drawable.manchester_united;
            case SWANSEA_CITY:
                return R.drawable.swansea_city_afc;
            case LEICESTER_CITY:
                return R.drawable.leicester_city_fc_hd_logo;
            case EVERTON_FC:
                return R.drawable.everton_fc_logo1;
            case WEST_HAM_UNITED_FC:
                return R.drawable.west_ham;
            case TOTTENHAM_HOTSPUR_FC:
                return R.drawable.tottenham_hotspur;
            case WEST_BROMWICH_ALBION:
                return R.drawable.west_bromwich_albion_hd_logo;
            case SUNDERLAND_AFC:
                return R.drawable.sunderland;
            case STOKE_CITY_FC:
                return R.drawable.stoke_city;
            default:
                return R.drawable.no_icon;
        }
    }

}
