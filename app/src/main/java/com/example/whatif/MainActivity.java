package com.example.whatif;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    private TypedArray ifferArray;
    private String textToSend = "";
    private boolean isMediaPlayerPrepared = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivity(checkIntent);

        ifferArray = getResources().obtainTypedArray(R.array.iffer);
        mMediaPlayer = new MediaPlayer();
    }

    public void rand_new_if(View view) {
        //Toast.makeText(this, "Saying: " + "A GDYBY TAK", Toast.LENGTH_LONG).show();
        Random random = new Random();
        textToSend = ifferArray.getString(random.nextInt(ifferArray.length()));
        System.out.println(textToSend);
        new Thread(runnableSend).start();
    }

    private Runnable runnableSend = new Runnable() {
        @Override
        public void run() {
            String mVoiceMessage = "{\"input\":{\"text\":\"" + textToSend + "\"},\"voice\":{\"languageCode\":\"pl-PL\",\"name\":\"pl-PL-Standard-A\"},\"audioConfig\":{\"audioEncoding\":\"MP3\",\"speakingRate\":\"1.0\",\"pitch\":\"0.0\"}}";

            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    mVoiceMessage.toString());
            Request request = new Request.Builder()
                    .url(Config.SYNTHESIZE_ENDPOINT)
                    .addHeader(Config.API_KEY_HEADER, Config.API_KEY)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .post(body)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    //speakFail(mMessage, e);
                    Log.e("TAG", "onFailure error : " + e.getMessage());
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response != null) {
                        Log.i("TAG", "onResponse code = " + response.code());
                        if (response.code() == 200) {
                            String text = response.body().string();
                            JsonElement jsonElement = new JsonParser().parse(text);
                            JsonObject jsonObject = jsonElement.getAsJsonObject();

                            if (jsonObject != null) {
                                String json = jsonObject.get("audioContent").toString();
                                json = json.replace("\"", "");
                                playAudio(json);
                                return;
                            }
                        }
                    }

                    //speakFail(mMessage, new NullPointerException("get response fail"));
                }
            });
        }
    };

    private void playAudio(String base64EncodedString) {
        synchronized (mMediaPlayer) {
            try {
                stopAudio();

                String url = "data:audio/mp3;base64," + base64EncodedString;

                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                //speakSuccess(mMessage);
            } catch (Exception e) {
                //speakFail(mMessage, IoEx);
                e.printStackTrace();
            }
        }
    }

    private void stopAudio() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying() && isMediaPlayerPrepared) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

}
