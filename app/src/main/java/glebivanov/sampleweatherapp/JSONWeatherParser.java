package glebivanov.sampleweatherapp;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

/*
 * Copyright (C) 2014 Gleb Ivanov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class JSONWeatherParser {

    public static Weather getWeather(String data) throws JSONException  {
        Weather weather = new Weather();

        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);

        // We start extracting the info
        WeatherLocation loc = new WeatherLocation();

        JSONObject coordObj = getObject("coord", jObj);
        loc.setLatitude(getFloat("lat", coordObj));
        loc.setLongitude(getFloat("lon", coordObj));

        JSONObject sysObj = getObject("sys", jObj);
        loc.setCountry(getString("country", sysObj));
        loc.setSunrise(getInt("sunrise", sysObj));
        loc.setSunset(getInt("sunset", sysObj));
        loc.setCity(getString("name", jObj));
        loc.setCityID(getInt("id", jObj));
        weather.location = loc;

        // We get weather info (This is an array)
        JSONArray jArr = jObj.getJSONArray("weather");

        // We use only the first value
        JSONObject JSONWeather = jArr.getJSONObject(0);
        weather.currentCondition.setWeatherId(getInt("id", JSONWeather));
        weather.currentCondition.setDescr(getString("description", JSONWeather));
        weather.currentCondition.setCondition(getString("main", JSONWeather));
        weather.currentCondition.setIcon(getString("icon", JSONWeather));

        JSONObject mainObj = getObject("main", jObj);
        weather.currentCondition.setHumidity(getInt("humidity", mainObj));
        weather.currentCondition.setPressure(getInt("pressure", mainObj));
        weather.temperature.setMaxTemp(getFloat("temp_max", mainObj));
        weather.temperature.setMinTemp(getFloat("temp_min", mainObj));
        weather.temperature.setTemp(getFloat("temp", mainObj));

        // Wind
        JSONObject wObj = getObject("wind", jObj);
        weather.wind.setSpeed(getFloat("speed", wObj));
        weather.wind.setDeg(getFloat("deg", wObj));

        // Clouds
        JSONObject cObj = getObject("clouds", jObj);
        weather.clouds.setPerc(getInt("all", cObj));

        // We download the icon to show


        return weather;
    }

    public static Weather getSearchedCity(String data) throws JSONException  {
        Weather weather = new Weather();

        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);

        int count = getInt("count", jObj);
        if(count > 0){
            JSONArray citylistArray = getArray("list", jObj);
            weather = getWeather(citylistArray.get(0).toString());
        }

        return weather;
    }

    public static Weather getForecastDayWeather(String data, int nTimeDay) throws JSONException  {
        Weather weather = new Weather();

        // We create out JSONObject from the data
        JSONObject jObj = new JSONObject(data);

        JSONArray jArr = jObj.getJSONArray("list");

        JSONObject jDay = jArr.getJSONObject(nTimeDay);

        JSONObject jTemp = getObject("temp", jDay);
        weather.temperature.setTemp(getFloat("day", jTemp));

        JSONArray jArrW = jDay.getJSONArray("weather");
        JSONObject jW = jArrW.getJSONObject(0);
        weather.currentCondition.setDescr(getString("description", jW));

        return weather;
    }


    private static JSONObject getObject(String tagName, JSONObject jObj)  throws JSONException {
        JSONObject subObj = jObj.getJSONObject(tagName);
        return subObj;
    }

    private static JSONArray getArray(String tagName, JSONObject jObj)  throws JSONException {
        JSONArray subArray = jObj.getJSONArray(tagName);
        return subArray;
    }


    private static String getString(String tagName, JSONObject jObj){
        try {
            return jObj.getString(tagName);
        } catch (JSONException e) {
            return "";
        }
    }

    private static float  getFloat(String tagName, JSONObject jObj){
        try {
            return (float) jObj.getDouble(tagName);
        } catch (JSONException e) {
            return 0;
        }
    }

    private static int  getInt(String tagName, JSONObject jObj){
        try {
            return jObj.getInt(tagName);
        } catch (JSONException e) {
            return 0;
        }
    }

}