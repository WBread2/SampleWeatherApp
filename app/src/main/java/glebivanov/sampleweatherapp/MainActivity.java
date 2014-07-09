package glebivanov.sampleweatherapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.Calendar;


public class MainActivity extends Activity {

    public final static String FULL_CITY_ID      = "full_city_id";

    RelativeLayout rl;

    public static final Uri WEATHER_URI = Uri
            .parse("content://glebivanov.sampleweatherapp.weather/cities");


    int rbCount = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rl = (RelativeLayout)findViewById(R.id.rlScrollCity);
        ConnectionActions();
        PreloadWeatherDB();

        Button btAddCity = (Button) findViewById(R.id.btAddCity);
        btAddCity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchCityActivity.class);
                startActivity(intent);
            }
        });

        BroadcastReceiver br = new BroadcastReceiver()
        {
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    if(ConnectionActions()){
                        PreloadWeatherDB();
                    }
                }
                else{
                    String ncn  = intent.getStringExtra(SearchCityActivity.NEW_CITY_NAME);
                    String ncc  = intent.getStringExtra(SearchCityActivity.NEW_CITY_COUNTRY);
                    long   ncid = intent.getLongExtra(SearchCityActivity.NEW_CITY_ID, 0);
                    if (ncid > 0) {
                        AddNewCity (ncn, ncc, ncid);
                        ShowAndLaunchCity(ncn, ncc, ncid, 0);
                    }
                }
          }
        };
        IntentFilter intFilt = new IntentFilter(SearchCityActivity.BROADCAST_ACTION);
        intFilt.addAction(android.net.ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(br, intFilt);

    }

    boolean ConnectionActions(){
        boolean isConnected = isNetworkConnected();
        TextView tvOfflineMode = (TextView)findViewById(R.id.tvOfflineMode);
        int vis = tvOfflineMode.getVisibility();
        if (!isConnected){
            if(vis!=0){
                tvOfflineMode.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Please check your network connection!", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        else{
            if(vis==0){
                tvOfflineMode.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Network is connected!", Toast.LENGTH_LONG).show();
            }
            return true;
        }
    }

    boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        }
        else{
            return ni.isConnectedOrConnecting();
        }
    }

    void PreloadWeatherDB(){
        rl.removeAllViews();

        Cursor c = getContentResolver().query(WEATHER_URI, null, null, null, null);

        if (c.moveToFirst()) {
            int WEATHER_city_nameIndex    = c.getColumnIndex(WeatherContentProvider.WEATHER_city_name);
            int WEATHER_country_nameIndex = c.getColumnIndex(WeatherContentProvider.WEATHER_country_name);
            int WEATHER_city_idIndex      = c.getColumnIndex(WeatherContentProvider.WEATHER_city_id);
            int WEATHER_temperatureIndex  = c.getColumnIndex(WeatherContentProvider.WEATHER_temperature);

            do {
                String CityName     = c.getString(WEATHER_city_nameIndex);
                String CountryName  = c.getString(WEATHER_country_nameIndex);
                long   city_id      = c.getLong(WEATHER_city_idIndex);
                float  CityTemp     = c.getFloat(WEATHER_temperatureIndex);

                if(!CityName.equals(""))
                    ShowAndLaunchCity(CityName, CountryName, city_id, CityTemp);

            } while (c.moveToNext());
        }
    }

    void AddNewCity(String CityName, String CountryName, long ncid){
        ContentValues cv = new ContentValues();
        cv.put(WeatherContentProvider.WEATHER_city_name, CityName);
        cv.put(WeatherContentProvider.WEATHER_country_name, CountryName);
        cv.put(WeatherContentProvider.WEATHER_city_id, ncid);
        cv.put(WeatherContentProvider.WEATHER_temperature, 0);
        cv.put(WeatherContentProvider.WEATHER_condition, "");
        cv.put(WeatherContentProvider.WEATHER_description, "");
        cv.put(WeatherContentProvider.WEATHER_wind_speed, "");
        cv.put(WeatherContentProvider.WEATHER_wind_deg, 0);
        cv.put(WeatherContentProvider.WEATHER_humidity, 0);
        cv.put(WeatherContentProvider.WEATHER_pressure, 0);

        Calendar cal = Calendar.getInstance();
        String ins_time = cal.getTime().toString();
        cv.put(WeatherContentProvider.WEATHER_lastupdate, ins_time);

        Uri newUri = getContentResolver().insert(WEATHER_URI, cv);
    }


    void ShowAndLaunchCity(String CityName, String CountryName, final long CityID, float CityTemp){
        RelativeLayout.LayoutParams tvCityNameParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (rbCount>2000)
            tvCityNameParams.addRule(RelativeLayout.BELOW, rbCount-2);
        tvCityNameParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        TextView tvCityName = new TextView(this);
        tvCityName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        tvCityName.setTextColor(Color.parseColor("#ff494cff"));
        tvCityName.setTypeface(null, Typeface.BOLD);
        tvCityName.setId(rbCount);
        rbCount++;
        rl.addView(tvCityName, tvCityNameParams);
        tvCityName.setText(CityName);

        RelativeLayout.LayoutParams tvCountryNameParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvCountryNameParams.addRule(RelativeLayout.BELOW, tvCityName.getId());
        tvCountryNameParams.addRule(RelativeLayout.ALIGN_LEFT,tvCityName.getId());
        tvCountryNameParams.setMargins(20,0,0,0);
        TextView tvCountryName = new TextView(this);
        tvCountryName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        tvCountryName.setTextColor(Color.parseColor("#ff887fff"));
        tvCountryName.setTypeface(null, Typeface.BOLD_ITALIC);
        tvCountryName.setId(rbCount);
        rbCount++;
        rl.addView(tvCountryName, tvCountryNameParams);
        tvCountryName.setText(CountryName);

        RelativeLayout.LayoutParams tvTlabelParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvTlabelParams.addRule(RelativeLayout.ALIGN_TOP,tvCityName.getId());
        tvTlabelParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        TextView tvTlabel = new TextView(this);
        tvTlabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        tvTlabel.setTypeface(null, Typeface.BOLD);
        tvTlabel.setId(rbCount);
        rbCount++;
        rl.addView(tvTlabel, tvTlabelParams);
        tvTlabel.setText("°C");

        RelativeLayout.LayoutParams tvTParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvTParams.addRule(RelativeLayout.LEFT_OF,tvTlabel.getId());
        tvTParams.addRule(RelativeLayout.ALIGN_TOP,tvCityName.getId());
        TextView tvT = new TextView(this);
        tvT.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        tvT.setTypeface(null, Typeface.BOLD);
        tvT.setId(rbCount);
        rbCount++;
        rl.addView(tvT, tvTParams);
        tvT.setText(""+Math.round(CityTemp - 273.15));

        RelativeLayout.LayoutParams ivDividerParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ivDividerParams.addRule(RelativeLayout.BELOW,tvCountryName.getId());
        ivDividerParams.setMargins(0,10,0,0);
        ImageView ivDivider = new ImageView(this);
        ivDivider.setImageResource(R.drawable.choose_divider);
        ivDivider.setId(rbCount);
        rbCount++;
        rl.addView(ivDivider, ivDividerParams);

        RelativeLayout.LayoutParams ivBackParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ivBackParams.addRule(RelativeLayout.ABOVE,ivDivider.getId());
        ivBackParams.addRule(RelativeLayout.ALIGN_TOP,tvCityName.getId());
        ivBackParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        ivBackParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ImageView ivBack = new ImageView(this);
        ivBack.setBackgroundColor(Color.parseColor("#eef7f7f7"));
        ivBack.setId(rbCount);
        rbCount++;
        rl.addView(ivBack, ivBackParams);

        ivBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), OneCityActivity.class);
                intent.putExtra(FULL_CITY_ID, CityID);

                startActivity(intent);
            }
        });

        tvCityName.bringToFront();
        tvCountryName.bringToFront();
        tvTlabel.bringToFront();
        tvT.bringToFront();
        ivDivider.bringToFront();

        JSONWeatherTask task = new JSONWeatherTask();
        task.setmTvCityName(tvCityName);
        task.setmTvCountryName(tvCountryName);
        task.setmTvT(tvT);
        task.execute(""+CityID);
    }


    private class JSONWeatherTask extends AsyncTask<String, Void, Weather> {

        private TextView mTvCityName;
        private TextView mTvCountryName;
        private TextView mTvT;

        public void setmTvCityName(TextView tv) {
            this.mTvCityName = tv;
        }
        public void setmTvCountryName(TextView tv) {
            this.mTvCountryName = tv;
        }
        public void setmTvT(TextView tv) {
            this.mTvT = tv;
        }

        @Override
        protected Weather doInBackground(String... params) {

            Weather weather = new Weather();

            if(ConnectionActions()){
                String data = ( (new WeatherHttpClient()).getWeatherData(params[0]));

                try {
                    weather = JSONWeatherParser.getWeather(data);

                    String where = WeatherContentProvider.WEATHER_city_id + "=" + params[0];
                    Cursor c = getContentResolver().query(WEATHER_URI, null, where, null, null);

                    if (c.moveToFirst()) {
                        int WEATHER_IDIndex  = c.getColumnIndex(WeatherContentProvider.WEATHER_ID);
                        int ID = c.getInt(WEATHER_IDIndex);

                        ContentValues cv = new ContentValues();
                        cv.put(WeatherContentProvider.WEATHER_city_name, weather.location.getCity());
                        cv.put(WeatherContentProvider.WEATHER_country_name, weather.location.getCountry());
                        cv.put(WeatherContentProvider.WEATHER_city_id, weather.location.getCityID());
                        cv.put(WeatherContentProvider.WEATHER_temperature, weather.temperature.getTemp());
                        cv.put(WeatherContentProvider.WEATHER_condition, weather.currentCondition.getCondition());
                        cv.put(WeatherContentProvider.WEATHER_description, weather.currentCondition.getDescr());
                        cv.put(WeatherContentProvider.WEATHER_wind_speed, weather.wind.getSpeed());
                        cv.put(WeatherContentProvider.WEATHER_wind_deg, weather.wind.getDeg());
                        cv.put(WeatherContentProvider.WEATHER_humidity, weather.currentCondition.getHumidity());
                        cv.put(WeatherContentProvider.WEATHER_pressure, weather.currentCondition.getPressure());
                        Uri uri = ContentUris.withAppendedId(WEATHER_URI, ID);
                        int cnt = getContentResolver().update(uri, cv, null, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                String where = WeatherContentProvider.WEATHER_city_id + "=" + params[0];
                Cursor c = getContentResolver().query(WEATHER_URI, null, where, null, null);

                if (c.moveToFirst()) {
                    int WEATHER_temperatureIndex  = c.getColumnIndex(WeatherContentProvider.WEATHER_temperature);
                    float  CityTemp     = c.getFloat(WEATHER_temperatureIndex);
                    weather.temperature.setTemp(CityTemp);
                }
            }
            return weather;
        }
        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            if(weather.location!=null){
                mTvCityName.setText(weather.location.getCity());
                mTvCountryName.setText(weather.location.getCountry());
            }
            mTvT.setText(""+Math.round(weather.temperature.getTemp() - 273.15));

        }

    }

}
