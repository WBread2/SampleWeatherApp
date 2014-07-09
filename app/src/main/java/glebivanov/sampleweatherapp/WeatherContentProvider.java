package glebivanov.sampleweatherapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Calendar;

public class WeatherContentProvider extends ContentProvider {

    // // Константы для БД
    // БД
    static String DB_NAME = "weatherDB";
    static final int DB_VERSION = 1;

    // Таблица
    static String DB_TABLE_NAME = "city_list_table";

    // Поля
    static public final String WEATHER_ID = "_id";
    static public final String WEATHER_city_id = "city_id";
    static public final String WEATHER_city_name = "city_name";
    static public final String WEATHER_country_name = "country_name";
    static public final String WEATHER_lastupdate = "lastupdate";
    static public final String WEATHER_condition = "condition";
    static public final String WEATHER_description = "description";
    static public final String WEATHER_temperature = "temperature";
    static public final String WEATHER_wind_deg = "wind_deg";
    static public final String WEATHER_wind_speed = "wind_speed";
    static public final String WEATHER_humidity = "humidity";
    static public final String WEATHER_pressure = "pressure";

    // Скрипт создания таблицы
    static final String DB_CREATE = "create table " + DB_TABLE_NAME + "("
            + WEATHER_ID + " integer primary key autoincrement, "
            + WEATHER_city_id + " integer, "
            + WEATHER_city_name + " text, "
            + WEATHER_country_name + " text, "
            + WEATHER_lastupdate + " text, "
            + WEATHER_condition + " text, "
            + WEATHER_description + " text, "
            + WEATHER_temperature + " float, "
            + WEATHER_wind_deg + " float, "
            + WEATHER_wind_speed + " float, "
            + WEATHER_humidity + " float, "
            + WEATHER_pressure + " float"
            + ");";

    // // Uri
    // authority
    static final String AUTHORITY = "glebivanov.sampleweatherapp.weather";

    // path
    static final String CITIES_PATH = "cities";

    // Общий Uri
    public static final Uri WEATHER_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + CITIES_PATH);

    // Типы данных
    // набор строк
    static final String WEATHER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + CITIES_PATH;

    // одна строка
    static final String WEATHER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + CITIES_PATH;

    //// UriMatcher
    // общий Uri
    static final int URI_CITIES = 1;

    // Uri с указанным ID
    static final int URI_CITIES_ID = 2;

    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, CITIES_PATH, URI_CITIES);
        uriMatcher.addURI(AUTHORITY, CITIES_PATH + "/#", URI_CITIES_ID);
    }

    DBHelper dbHelper;
    SQLiteDatabase db;

    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    // чтение
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // проверяем Uri
        switch (uriMatcher.match(uri)) {
            case URI_CITIES: // общий Uri
                // если сортировка не указана, ставим свою - по имени
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = WEATHER_ID + " ASC";
                }
                break;
            case URI_CITIES_ID: // Uri с ID
                String id = uri.getLastPathSegment();
                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = WEATHER_ID + " = " + id;
                } else {
                    selection = selection + " AND " + WEATHER_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DB_TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        // просим ContentResolver уведомлять этот курсор
        // об изменениях данных в WEATHER_CONTENT_URI
        cursor.setNotificationUri(getContext().getContentResolver(),
                WEATHER_CONTENT_URI);
        return cursor;
    }

    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != URI_CITIES)
            throw new IllegalArgumentException("Wrong URI: " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(DB_TABLE_NAME, null, values);
        Uri resultUri = ContentUris.withAppendedId(WEATHER_CONTENT_URI, rowID);
        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_CITIES:
                //
                break;
            case URI_CITIES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = WEATHER_ID + " = " + id;
                } else {
                    selection = selection + " AND " + WEATHER_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(DB_TABLE_NAME, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_CITIES:
                //
                break;
            case URI_CITIES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = WEATHER_ID + " = " + id;
                } else {
                    selection = selection + " AND " + WEATHER_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(DB_TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_CITIES:
                return WEATHER_CONTENT_TYPE;
            case URI_CITIES_ID:
                return WEATHER_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);
            ContentValues cv = new ContentValues();

            cv.put(WEATHER_city_name, "Saint Petersburg");
            cv.put(WEATHER_country_name, "RU");
            cv.put(WEATHER_city_id, 498817);
            cv.put(WEATHER_temperature, 273.15);
            cv.put(WEATHER_condition, "");
            cv.put(WEATHER_description, "");
            cv.put(WEATHER_wind_speed, "");
            cv.put(WEATHER_wind_deg, 0);
            cv.put(WEATHER_humidity, 0);
            cv.put(WEATHER_pressure, 0);

            Calendar cal = Calendar.getInstance();
            String ins_time = cal.getTime().toString();
            cv.put(WEATHER_lastupdate, ins_time);
            db.insert(DB_TABLE_NAME, null, cv);

            cv.put(WEATHER_city_name, "Moscow");
            cv.put(WEATHER_country_name, "RU");
            cv.put(WEATHER_city_id, 524901);
            cv.put(WEATHER_temperature, 273.15);
            cv.put(WEATHER_condition, "");
            cv.put(WEATHER_description, "");
            cv.put(WEATHER_wind_speed, "");
            cv.put(WEATHER_wind_deg, 0);
            cv.put(WEATHER_humidity, 0);
            cv.put(WEATHER_pressure, 0);

            ins_time = cal.getTime().toString();
            cv.put(WEATHER_lastupdate, ins_time);
            db.insert(DB_TABLE_NAME, null, cv);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}