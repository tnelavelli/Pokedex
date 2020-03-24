package com.tarannelavelli.Pokedex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView descriptionTextView;
    private String url;
    private String descriptionURL;
    private RequestQueue requestQueue;
    private Button button;
    private boolean caught;
    private ImageView imageView;
    private String spriteURL;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String index;


    @SuppressLint("StaticFieldLeak")
    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            } catch (IOException e) {
                Log.e("error", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }

        protected void execute(String url) {
            String[] array = {url, ""};
            Bitmap bm = doInBackground(array);
            onPostExecute(bm);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        index = url.split("/")[url.split("/").length - 1];
        descriptionURL = getIntent().getStringExtra("descriptionURL");

        imageView = findViewById(R.id.pokemon_image);
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        descriptionTextView = findViewById(R.id.description);
        button = findViewById(R.id.catch_release_button);

        preferences = getPreferences(Context.MODE_PRIVATE);
        editor = preferences.edit();
        if(!preferences.getBoolean(index, false)) {
            caught = false;
        } else {
            caught = true;
            button.setText("Release");
        }

        load();
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");
        descriptionTextView.setText("");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String name = response.getString("name");
                    nameTextView.setText(String.format("%s%s", name.substring(0, 1).toUpperCase(), name.substring(1)));
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");
                        String typeCased = type.substring(0, 1).toUpperCase() + type.substring(1);
                        if (slot == 1)
                            type1TextView.setText(typeCased);
                        if (slot == 2)
                            type2TextView.setText(typeCased);
                    }
                    spriteURL = response.getJSONObject("sprites").getString("front_default");
                    new DownloadSpriteTask().execute(spriteURL);
                } catch (JSONException e) {
                    Log.e("error", "JSONException", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", "Pokemon details exception", error);
            }
        });
        requestQueue.add(request);
        request = new JsonObjectRequest(Request.Method.GET, descriptionURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray flavorTextEntries = response.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < flavorTextEntries.length(); i++) {
                        String flavorTextEntryLanguage = flavorTextEntries.getJSONObject(i).getJSONObject("language").getString("name");
                        if (flavorTextEntryLanguage.equals("en")) {
                            String flavorTextEntry = flavorTextEntries.getJSONObject(i).getString("flavor_text");
                            descriptionTextView.setText(flavorTextEntry);
                            break;
                        }
                    }
                } catch (JSONException error) {
                    Log.e("error", "JSON exception", error);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", "Pokemon details exception", error);
            }
        });
        requestQueue.add(request);
    }

    public void toggleCatch(View view) {
        if (button.getText().equals("Catch")) {
            button.setText("Release");
        } else {
            button.setText("Catch");
        }
        caught = !caught;
        changeValue(caught);
    }

    public void changeValue(boolean caught) {
        editor.remove(index);
        editor.putBoolean(index, caught);
        editor.commit();
    }
}
