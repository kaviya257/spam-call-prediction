package com.example.spam_new;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AudioUploaderHelper {

    public interface PredictionCallback {
        void onResult(String response);
    }

    private final Context context;
    private static final String TAG = "AudioUploaderHelper";

    // ✅ FIXED CONSTRUCTOR (ONLY Context)
    public AudioUploaderHelper(Context context) {
        this.context = context;
    }

    // ✅ FIXED METHOD SIGNATURE
    public void uploadAudio(Uri audioUri, PredictionCallback callback) {

        if (audioUri == null) {
            Toast.makeText(context, "No audio selected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(audioUri);

            if (inputStream == null) {
                callback.onResult("{\"error\":\"Cannot open audio file\"}");
                return;
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;

            while ((nRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }

            inputStream.close();

            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("audio/*"),
                    buffer.toByteArray()
            );

            MultipartBody.Part body = MultipartBody.Part.createFormData(
                    "file",
                    getFileName(audioUri),
                    requestFile
            );

            AudioUploader api = ApiClient.getClient().create(AudioUploader.class);

            api.uploadAudio(body).enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            String json = response.body().string();
                            callback.onResult(json);
                        } else {
                            callback.onResult("{\"error\":\"Empty server response\"}");
                        }
                    } catch (Exception e) {
                        callback.onResult("{\"error\":\"" + e.getMessage() + "\"}");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onResult("{\"error\":\"" + t.getMessage() + "\"}");
                }
            });

        } catch (Exception e) {
            Toast.makeText(context, "Audio error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ✅ MAKE THIS PUBLIC
    public String getFileName(Uri uri) {

        String name = "audio.wav";

        try {
            Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    name = cursor.getString(index);
                }
                cursor.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "Filename error", e);
        }

        return name;
    }
}