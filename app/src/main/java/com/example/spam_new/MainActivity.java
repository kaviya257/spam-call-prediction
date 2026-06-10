package com.example.spam_new;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Button btnChoose, btnUpload;

    private TextView txtFile, txtResult;

    private Uri selectedAudioUri = null;

    private AudioUploaderHelper audioUploaderHelper;

    private static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        btnChoose = findViewById(R.id.btnChoose);
        btnUpload = findViewById(R.id.btnUpload);

        txtFile = findViewById(R.id.txtFile);
        txtResult = findViewById(R.id.txtResult);

        audioUploaderHelper = new AudioUploaderHelper(this);

        btnChoose.setOnClickListener(v -> chooseAudio());

        btnUpload.setOnClickListener(v -> uploadAndPredict());
    }

    // =========================================
    // CHOOSE AUDIO
    // =========================================

    private void chooseAudio() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType("audio/*");

        startActivityForResult(intent, 100);
    }

    // =========================================
    // RECEIVE AUDIO
    // =========================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(requestCode, resultCode, data);

        if (
                requestCode == 100 &&
                        resultCode == RESULT_OK &&
                        data != null
        ) {

            selectedAudioUri = data.getData();

            txtFile.setText(
                    "Selected File:\n" +
                            audioUploaderHelper.getFileName(selectedAudioUri)
            );
        }
    }

    // =========================================
    // UPLOAD AUDIO
    // =========================================

    private void uploadAndPredict() {

        if (selectedAudioUri == null) {

            Toast.makeText(
                    this,
                    "Choose audio first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        txtResult.setText("Uploading...\nPlease wait...");

        audioUploaderHelper.uploadAudio(
                selectedAudioUri,

                result -> runOnUiThread(() -> {

                    try {

                        Log.d(TAG, "RAW RESPONSE = " + result);

                        if (
                                result == null ||
                                        result.isEmpty()
                        ) {

                            txtResult.setText(
                                    "Empty server response"
                            );

                            return;
                        }

                        JSONObject jsonObject =
                                new JSONObject(result);

                        // ====================================
                        // HANDLE SERVER ERRORS
                        // ====================================

                        if (jsonObject.has("detail")) {

                            txtResult.setText(

                                    "SERVER ERROR\n\n"

                                            + jsonObject.getString(
                                            "detail"
                                    )
                            );

                            return;
                        }

                        String transcription =
                                jsonObject.optString(
                                        "transcription",
                                        "No transcription"
                                );

                        String language =
                                jsonObject.optString(
                                        "detected_language",
                                        "Unknown"
                                );

                        double textScore =
                                jsonObject.optDouble(
                                        "text_spam_score",
                                        0
                                );

                        double audioScore =
                                jsonObject.optDouble(
                                        "audio_spam_score",
                                        0
                                );

                        double finalScore =
                                jsonObject.optDouble(
                                        "final_spam_score",
                                        0
                                );

                        String prediction =
                                jsonObject.optString(
                                        "prediction",
                                        "UNKNOWN"
                                );

                        // ====================================
                        // DISPLAY RESULT
                        // ====================================

                        String finalText =

                                "TRANSCRIPTION\n\n"
                                        + transcription

                                        + "\n\n----------------------\n\n"

                                        + "LANGUAGE\n\n"
                                        + language

                                        + "\n\n----------------------\n\n"

                                        + "TEXT SPAM SCORE\n\n"
                                        + textScore + "%"

                                        + "\n\n----------------------\n\n"

                                        + "AUDIO SPAM SCORE\n\n"
                                        + audioScore + "%"

                                        + "\n\n----------------------\n\n"

                                        + "FINAL SCORE\n\n"
                                        + finalScore + "%"

                                        + "\n\n----------------------\n\n"

                                        + "PREDICTION\n\n"
                                        + prediction;

                        txtResult.setText(finalText);

                        showResultDialog(
                                prediction,
                                finalScore
                        );

                    } catch (Exception e) {

                        Log.e(TAG, "JSON ERROR", e);

                        txtResult.setText(

                                "JSON Parsing Failed\n\n"

                                        + e.getMessage()

                                        + "\n\nRAW RESPONSE:\n\n"

                                        + result
                        );

                        Toast.makeText(
                                MainActivity.this,
                                "Parsing Error",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                })
        );
    }

    // =========================================
    // RESULT DIALOG
    // =========================================

    private void showResultDialog(
            String prediction,
            double score
    ) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle("Spam Detection");

        if (
                prediction.equalsIgnoreCase("SPAM")
        ) {

            builder.setMessage(

                    "⚠ SPAM CALL DETECTED\n\n"

                            + "Confidence Score:\n"
                            + score + "%"
            );

        } else {

            builder.setMessage(

                    "✅ SAFE CALL\n\n"

                            + "Safety Score:\n"
                            + (100 - score) + "%"
            );
        }

        builder.setPositiveButton(
                "OK",
                (dialog, which) -> dialog.dismiss()
        );

        builder.show();
    }
}