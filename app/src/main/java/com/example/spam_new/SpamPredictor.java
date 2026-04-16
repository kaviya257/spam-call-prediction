package com.example.spam_new;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class SpamPredictor {

    private final String url = "http://10.91.249.184:8000/predict"; // Your backend
    private final RequestQueue queue;
    private final Context context;

    public interface PredictionCallback {
        void onResult(String result);
    }

    public SpamPredictor(Context context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(context);
    }

    public void predict(String messageText, PredictionCallback callback) {

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("text", messageText);
        } catch (JSONException e) {
            Toast.makeText(context, "JSON Error", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try {
                        String result = response.getString("label");
                        double probability = response.getDouble("probability");

                        callback.onResult(result + " (" + probability + "%)");

                        if (result.equalsIgnoreCase("SPAM")) {
                            Toast.makeText(context,
                                    "Spam detected. Launching AI Assistant...",
                                    Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(context, AiAssistantActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }

                    } catch (JSONException e) {
                        Toast.makeText(context,
                                "Parsing Error",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context,
                        "Request Error",
                        Toast.LENGTH_SHORT).show()
        );

        queue.add(jsonObjectRequest);
    }
}
