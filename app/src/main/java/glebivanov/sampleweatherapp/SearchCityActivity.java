package glebivanov.sampleweatherapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;

public class SearchCityActivity extends Activity {

    public final static String BROADCAST_ACTION = "ADD_NEW_CITY";
    public final static String NEW_CITY_NAME    = "new_city_name";
    public final static String NEW_CITY_COUNTRY = "new_city_country";
    public final static String NEW_CITY_ID      = "new_city_id";

    String NewCityName    = "";
    String NewCityCountry = "";
    long   NewCityID      = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);

        final TextView tvSearchResult = (TextView) findViewById(R.id.tvSearchResult);
        final TextView tvHint         = (TextView) findViewById(R.id.tvHint);

        Button btSearchCity = (Button) findViewById(R.id.btSearchCity);
        btSearchCity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(isNetworkConnected()){
                    EditText edCityName = (EditText) findViewById(R.id.edCityName);
                    final String CityName = edCityName.getText().toString();

                    new Thread(new Runnable() {
                        public void run() {
                            String data = ( (new WeatherHttpClient()).getFindCityData(CityName));

                            Weather weather = new Weather();
                            try {
                                weather = JSONWeatherParser.getSearchedCity(data);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            final String SearchResult;

                            if(weather.location!=null){
                                NewCityName    = weather.location.getCity();
                                NewCityCountry = weather.location.getCountry();
                                NewCityID      = weather.location.getCityID();
                                SearchResult = weather.location.getCity() + ", " + weather.location.getCountry();
                            }
                            else
                                SearchResult = "Sorry, nothing is found";

                            SearchCityActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    tvSearchResult.setVisibility(View.VISIBLE);
                                    tvHint.setVisibility(View.VISIBLE);
                                    tvSearchResult.setText(SearchResult);
                                }
                            });
                        }
                    }).start();

                }
                else{
                    tvSearchResult.setVisibility(View.VISIBLE);
                    tvSearchResult.setText("Sorry! Check your network connection!");
                }


            }
        });

        tvSearchResult.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(NewCityID>0){
                    Intent intent = new Intent(BROADCAST_ACTION);
                    intent.putExtra(NEW_CITY_NAME,    NewCityName);
                    intent.putExtra(NEW_CITY_COUNTRY, NewCityCountry);
                    intent.putExtra(NEW_CITY_ID,      NewCityID);
                    sendBroadcast(intent);

                    tvSearchResult.setVisibility(View.INVISIBLE);
                    tvHint.setVisibility(View.INVISIBLE);
                    finish();
                }
            }
        });
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
