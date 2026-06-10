package com.example.spam_new;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

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

    public AudioUploaderHelper(Context context) {
        this.context = context;
    }

    public void uploadAudio(
            Uri audioUri,
            PredictionCallback callback
    ) {

        if (audioUri == null) {

            callback.onResult(
                    "{\"detail\":\"No audio selected\"}"
            );

            return;
        }

        try {

            InputStream inputStream =
                    context.getContentResolver()
                            .openInputStream(audioUri);

            if (inputStream == null) {

                callback.onResult(
                        "{\"detail\":\"Cannot open file\"}"
                );

                return;
            }

            ByteArrayOutputStream buffer =
                    new ByteArrayOutputStream();

            byte[] data = new byte[4096];

            int nRead;

            while ((nRead = inputStream.read(data)) != -1) {

                buffer.write(data, 0, nRead);
            }

            inputStream.close();

            RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse("audio/*"),
                            buffer.toByteArray()
                    );

            MultipartBody.Part body =
                    MultipartBody.Part.createFormData(
                            "file",
                            getFileName(audioUri),
                            requestFile
                    );

            AudioUploader api =
                    ApiClient.getClient()
                            .create(AudioUploader.class);

            api.uploadAudio(body).enqueue(
                    new Callback<ResponseBody>() {

                        @Override
                        public void onResponse(
                                Call<ResponseBody> call,
                                Response<ResponseBody> response
                        ) {

                            try {

                                String responseString;

                                if (
                                        response.isSuccessful()
                                                && response.body() != null
                                ) {

                                    responseString =
                                            response.body().string();

                                } else if (
                                        response.errorBody() != null
                                ) {

                                    responseString =
                                            response.errorBody().string();

                                } else {

                                    responseString =
                                            "{\"detail\":\"Unknown server error\"}";
                                }

                                Log.d(
                                        TAG,
                                        "SERVER RESPONSE: "
                                                + responseString
                                );

                                callback.onResult(
                                        responseString
                                );

                            } catch (Exception e) {

                                callback.onResult(
                                        "{\"detail\":\""
                                                + e.getMessage()
                                                + "\"}"
                                );
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ResponseBody> call,
                                Throwable t
                        ) {

                            callback.onResult(
                                    "{\"detail\":\""
                                            + t.getMessage()
                                            + "\"}"
                            );
                        }
                    });

        } catch (Exception e) {

            callback.onResult(
                    "{\"detail\":\""
                            + e.getMessage()
                            + "\"}"
            );
        }
    }

    public String getFileName(Uri uri) {

        String result = "audio.wav";

        Cursor cursor =
                context.getContentResolver()
                        .query(
                                uri,
                                null,
                                null,
                                null,
                                null
                        );

        try {

            if (
                    cursor != null &&
                            cursor.moveToFirst()
            ) {

                int index =
                        cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                        );

                if (index >= 0) {

                    result = cursor.getString(index);
                }
            }

        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }
}