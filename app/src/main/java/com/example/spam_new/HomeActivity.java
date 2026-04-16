package com.example.spam_new;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    private static final int PICK_AUDIO = 100;

    Uri selectedAudioUri = null;
    MediaPlayer mediaPlayer;
    AudioUploaderHelper audioUploaderHelper;

    Button btnChooseAudio, btnPlayAudio, btnSubmitAudio, btnAiHelp;
    TextView txtAudioFile, txtConvertedText, txtAccuracy, txtConclusion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Views
        btnChooseAudio = findViewById(R.id.btnChooseAudio);
        btnPlayAudio = findViewById(R.id.btnPlayAudio);
        btnSubmitAudio = findViewById(R.id.btnSubmitAudio);
        btnAiHelp = findViewById(R.id.btnAiHelp);

        txtAudioFile = findViewById(R.id.txtAudioFile);
        txtConvertedText = findViewById(R.id.txtConvertedText);
        txtAccuracy = findViewById(R.id.txtAccuracy);
        txtConclusion = findViewById(R.id.txtConclusion);

        audioUploaderHelper = new AudioUploaderHelper(this);

        // Button actions
        btnChooseAudio.setOnClickListener(v -> chooseAudio());
        btnPlayAudio.setOnClickListener(v -> playAudio());
        btnSubmitAudio.setOnClickListener(v -> submitAudio());

        btnAiHelp.setOnClickListener(v -> {
            Intent intent = new Intent(this, AiAssistantActivity.class);
            intent.putExtra("detectedText", txtConvertedText.getText().toString());
            intent.putExtra("conclusion", txtConclusion.getText().toString());
            startActivity(intent);
        });
    }

    private void chooseAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO && resultCode == RESULT_OK && data != null) {
            selectedAudioUri = data.getData();
            txtAudioFile.setText("Selected: " +
                    audioUploaderHelper.getFileName(selectedAudioUri));
        }
    }

    private void playAudio() {
        if (selectedAudioUri == null) {
            txtAudioFile.setText("Please select an audio file first");
            return;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(this, selectedAudioUri);
            mediaPlayer.start();

        } catch (Exception e) {
            txtAudioFile.setText("Error playing audio");
        }
    }

    private void submitAudio() {

        if (selectedAudioUri == null) {
            txtConvertedText.setText("Please choose an audio file first");
            txtConclusion.setText("--");
            txtAccuracy.setText("--");
            return;
        }

        audioUploaderHelper.uploadAudio(selectedAudioUri, response -> {

            runOnUiThread(() -> {

                try {
                    JSONObject obj = new JSONObject(response);

                    if (obj.has("text")) {

                        String text = obj.getString("text");
                        double prob = obj.getDouble("spam_probability");
                        String label = obj.getString("label");

                        // Show on screen
                        txtConvertedText.setText(text);
                        txtAccuracy.setText(prob + " %");
                        txtConclusion.setText(label);

                        // 🔥 UI COLOR IMPROVEMENT
                        if (label.equalsIgnoreCase("SPAM")) {
                            txtConclusion.setTextColor(Color.RED);
                        } else {
                            txtConclusion.setTextColor(Color.GREEN);
                        }

                        // Popup
                        AlertDialog dialog = new AlertDialog.Builder(HomeActivity.this)
                                .setTitle("Spam Detection Result")
                                .setMessage(
                                        label.equalsIgnoreCase("SPAM") ?
                                                "⚠ Warning!\nThis call is SPAM." :
                                                "✅ Safe!\nThis call is NOT SPAM."
                                )
                                .setCancelable(false)
                                .setPositiveButton("OK", null)
                                .create();

                        dialog.show();

                    } else if (obj.has("detail")) {
                        txtConclusion.setText("Server error: " + obj.getString("detail"));
                    } else if (obj.has("error")) {
                        txtConclusion.setText(obj.getString("error"));
                    } else {
                        txtConclusion.setText("Unexpected response");
                    }

                } catch (Exception e) {
                    txtConclusion.setText("Invalid server response");
                }

            });

        });
    }
}