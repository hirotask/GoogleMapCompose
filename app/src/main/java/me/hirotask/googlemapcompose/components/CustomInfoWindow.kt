package me.hirotask.googlemapcompose.components

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.hirotask.googlemapcompose.BuildConfig
import me.hirotask.googlemapcompose.R
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class CustomInfoWindow(context: Context) : GoogleMap.InfoWindowAdapter {

    var mWindow = (context as Activity).layoutInflater.inflate(R.layout.custom_info_window, null)

    private fun rendowWindowText(marker: Marker, view: View){

        val tvWeather = view.findViewById<TextView>(R.id.weather)
        val tvMax = view.findViewById<TextView>(R.id.max)
        val tvMin = view.findViewById<TextView>(R.id.min)

        val uri = "https://api.openweathermap.org/data/2.5/weather?lang=ja"
        val weather_api = BuildConfig.WEATHER_API_KEY

        CoroutineScope(Dispatchers.Main).launch {
            val url =
                "$uri&lat=${marker.position.latitude}&lon=${marker.position.longitude}&appid=$weather_api"
            val response = weatherBackgroundTask(url)
            val jsonObj = JSONObject(response)

            val weather = jsonObj.getJSONArray("weather")
                .getJSONObject(0)
                .getString("description")

            val max = getTempFromKelvin(
                jsonObj.getJSONObject("main").getDouble("temp_max")
            ).toInt()

            //最低気温
            val min = getTempFromKelvin(
                jsonObj.getJSONObject("main").getDouble("temp_min")
            ).toInt()

            tvWeather.text = "天気:$weather"
            tvMax.text = "最高気温:$max"
            tvMin.text = "最低気温: $min"
        }
    }

    /**
     * ケルビンを摂氏に変換する関数
     */
    private fun getTempFromKelvin(kelvin: Double): Double {
        return kelvin - 273.5
    }

    /**
     * HTTP通信をする関数
     */
    //※suspend = 中断する可能性がある関数につける
    private suspend fun weatherBackgroundTask(url: String): String {
        //withContext = スレッド分離。Dispathers.IO=ワーカースレッド
        val response = withContext(Dispatchers.IO) {
            var httpResult = ""

            try {
                val urlObj = URL(url)

                //URLから情報を取得
                val br = BufferedReader(InputStreamReader(urlObj.openStream()))
                httpResult = br.readText()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return@withContext httpResult
        }

        return response
    }

    override fun getInfoContents(marker: Marker): View? {
        rendowWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoWindow(marker: Marker): View? {
        rendowWindowText(marker, mWindow)
        return mWindow
    }
}