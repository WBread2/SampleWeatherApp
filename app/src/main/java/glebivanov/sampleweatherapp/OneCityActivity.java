package glebivanov.sampleweatherapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class OneCityActivity extends ActionBarActivity {
   
    int rbCount = 3000;

    private TextView cityText;
    private TextView condDescr;
    private TextView temp;
    private TextView press;
    private TextView windSpeed;
    private TextView windDeg;
    private TextView hum;
    private RelativeLayout rl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_city);

        rl = (RelativeLayout)findViewById(R.id.rlOneCityForecast);

        cityText = (TextView) findViewById(R.id.cityText);
        condDescr = (TextView) findViewById(R.id.condDescr);
        temp = (TextView) findViewById(R.id.temp);
        hum = (TextView) findViewById(R.id.hum);
        press = (TextView) findViewById(R.id.press);
        windSpeed = (TextView) findViewById(R.id.windSpeed);
        windDeg = (TextView) findViewById(R.id.windDeg);

        Intent intent = getIntent();
        final long ncid   = intent.getLongExtra(MainActivity.FULL_CITY_ID, 0);

        String where = WeatherContentProvider.WEATHER_city_id + "=" + ncid;
        Cursor c = getContentResolver().query(MainActivity.WEATHER_URI, null, where, null, null);

        if (c.moveToFirst()) {
            int WEATHER_city_nameIndex    = c.getColumnIndex(WeatherContentProvider.WEATHER_city_name);
            int WEATHER_country_nameIndex = c.getColumnIndex(WeatherContentProvider.WEATHER_country_name);
            int WEATHER_conditionIndex    = c.getColumnIndex(WeatherContentProvider.WEATHER_condition);
            int WEATHER_descriptionIndex  = c.getColumnIndex(WeatherContentProvider.WEATHER_description);
            int WEATHER_temperatureIndex  = c.getColumnIndex(WeatherContentProvider.WEATHER_temperature);
            int WEATHER_wind_degIndex     = c.getColumnIndex(WeatherContentProvider.WEATHER_wind_deg);
            int WEATHER_wind_speedIndex   = c.getColumnIndex(WeatherContentProvider.WEATHER_wind_speed);
            int WEATHER_humidityIndex     = c.getColumnIndex(WeatherContentProvider.WEATHER_humidity);
            int WEATHER_pressureIndex     = c.getColumnIndex(WeatherContentProvider.WEATHER_pressure);

            String city_name    = c.getString(WEATHER_city_nameIndex);
            String country_name = c.getString(WEATHER_country_nameIndex);
            String condition    = c.getString(WEATHER_conditionIndex);
            String description  = c.getString(WEATHER_descriptionIndex);
            float  temperature  = c.getFloat(WEATHER_temperatureIndex);
            float  wind_deg     = c.getFloat(WEATHER_wind_degIndex);
            float  wind_speed   = c.getFloat(WEATHER_wind_speedIndex);
            float  humidity     = c.getFloat(WEATHER_humidityIndex);
            float  pressure     = c.getFloat(WEATHER_pressureIndex);

            cityText.setText(city_name + ", " + country_name);
            condDescr.setText(condition + " (" + description + ")");
            temp.setText("" + Math.round((temperature - 273.15)) + "°C");
            hum.setText("" + humidity + "%");
            press.setText("" + pressure + " hPa");
            windSpeed.setText("" + wind_speed + " mps");
            windDeg.setText("" + wind_deg + "°");

            if(isNetworkConnected()){
                new Thread(new Runnable() {
                    public void run() {
                        final String data = ( (new WeatherHttpClient()).getForecastData("" + ncid));
                        OneCityActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Weather weather = new Weather();
                                for(int i=0; i<=6; i++){
                                    try {
                                        weather = JSONWeatherParser.getForecastDayWeather(data, i);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    AddForecastDay(weather, i);
                                }
                            }
                        });
                    }
                }).start();
            }
        }
    }

    String GetForecastDay(int nDay){
        GregorianCalendar cal = new GregorianCalendar();
        int year  = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day   = cal.get(Calendar.DAY_OF_MONTH);

        int daysinmonth = 31;
        switch (month) {
            case 1: daysinmonth = 31;break;
            case 2: if(cal.isLeapYear(year))
                        daysinmonth = 29;
                    else
                        daysinmonth = 28;
                break;
            case 3: daysinmonth = 31;break;
            case 4: daysinmonth = 30;break;
            case 5: daysinmonth = 31;break;
            case 6: daysinmonth = 30;break;
            case 7: daysinmonth = 31;break;
            case 8: daysinmonth = 31;break;
            case 9: daysinmonth = 30;break;
            case 10: daysinmonth = 31;break;
            case 11: daysinmonth = 30;break;
            case 12: daysinmonth = 31;break;
        }

        int foreDay   = day + nDay;
        int foreMonth = month;
        int foreYear  = year;

        if(foreDay>daysinmonth){
            foreDay = foreDay - daysinmonth;
            foreMonth++;
            if(foreMonth>12){
                foreMonth = 1;
                foreYear++;
            }
        }
        return foreYear+"-"+foreMonth+"-"+foreDay;
    }

    void AddForecastDay(Weather weather, int nDay){
        RelativeLayout.LayoutParams tvForecastDayParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (rbCount>3000)
            tvForecastDayParams.addRule(RelativeLayout.BELOW, rbCount-2);

        tvForecastDayParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        TextView tvForecastDay = new TextView(this);
        tvForecastDay.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        tvForecastDay.setTextColor(Color.parseColor("#ff494cff"));
        tvForecastDay.setTypeface(null, Typeface.BOLD);
        tvForecastDay.setId(rbCount);
        rbCount++;
        rl.addView(tvForecastDay, tvForecastDayParams);
        tvForecastDay.setText(GetForecastDay(nDay));

        RelativeLayout.LayoutParams tvDescriptionParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvDescriptionParams.addRule(RelativeLayout.BELOW, tvForecastDay.getId());
        tvDescriptionParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        tvDescriptionParams.setMargins(20,0,0,0);
        TextView tvDescription = new TextView(this);
        tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        tvDescription.setTextColor(Color.parseColor("#ff887fff"));
        tvDescription.setTypeface(null, Typeface.BOLD_ITALIC);
        tvDescription.setId(rbCount);
        rbCount++;
        rl.addView(tvDescription, tvDescriptionParams);
        tvDescription.setText(weather.currentCondition.getDescr());

        RelativeLayout.LayoutParams tvTlabelParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        tvTlabelParams.addRule(RelativeLayout.ALIGN_TOP,tvForecastDay.getId());
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
        tvTParams.addRule(RelativeLayout.ALIGN_TOP,tvForecastDay.getId());
        TextView tvT = new TextView(this);
        tvT.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        tvT.setTypeface(null, Typeface.BOLD);
        tvT.setId(rbCount);
        rbCount++;
        rl.addView(tvT, tvTParams);
        tvT.setText(""+Math.round(weather.temperature.getTemp() - 273.15));

        RelativeLayout.LayoutParams ivDividerParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ivDividerParams.addRule(RelativeLayout.BELOW,tvDescription.getId());
        ivDividerParams.setMargins(0,10,0,0);
        ImageView ivDivider = new ImageView(this);
        ivDivider.setImageResource(R.drawable.choose_divider);
        ivDivider.setId(rbCount);
        rbCount++;
        rl.addView(ivDivider, ivDividerParams);

        RelativeLayout.LayoutParams ivBackParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ivBackParams.addRule(RelativeLayout.ABOVE,ivDivider.getId());
        ivBackParams.addRule(RelativeLayout.ALIGN_TOP,tvForecastDay.getId());
        ivBackParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        ivBackParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ImageView ivBack = new ImageView(this);
        ivBack.setBackgroundColor(Color.parseColor("#eef7f7f7"));
        ivBack.setId(rbCount);
        rbCount++;
        rl.addView(ivBack, ivBackParams);

        tvForecastDay.bringToFront();
        tvDescription.bringToFront();
        tvTlabel.bringToFront();
        tvT.bringToFront();
        ivDivider.bringToFront();
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
}
