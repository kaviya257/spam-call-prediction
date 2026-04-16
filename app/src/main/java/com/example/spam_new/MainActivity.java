package com.example.spam_new;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

    private void uploadAndPredict() {

        if (selectedAudioUri == null) {
            Toast.makeText(this,
                    "Please choose an audio file first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        audioUploaderHelper.uploadAudio(selectedAudioUri, result -> {

            // 🔥 Always switch to UI thread
            runOnUiThread(() -> {

                try {

                    // Debug: confirm callback working
                    Toast.makeText(MainActivity.this,
                            "Prediction received",
                            Toast.LENGTH_SHORT).show();

                    JSONObject jsonObject = new JSONObject(result);

                    if (!jsonObject.has("label")) {
                        txtResult.setText("Server Error:\n" + result);
                        return;
                    }

                    String label = jsonObject.getString("label");
                    double probability = jsonObject.getDouble("spam_probability");
                    String text = jsonObject.getString("text");

                    String displayText =
                            "Converted Text:\n" + text +
                                    "\n\nSpam Accuracy:\n" + probability + "%" +
                                    "\n\nFinal Conclusion:\n" + label;

                    txtResult.setText(displayText);

                    showResultDialog(label);

                } catch (Exception e) {
                    txtResult.setText("Parsing Error:\n" + e.getMessage());
                    Toast.makeText(MainActivity.this,
                            "Parsing Failed",
                            Toast.LENGTH_LONG).show();
                }

            });

        });
    }

    private void showResultDialog(String label) {

        if (isFinishing() || isDestroyed()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Spam Detection Result");

        if (label.equalsIgnoreCase("SPAM")) {
            builder.setMessage("⚠ Warning!\nThis call is SPAM.");
        } else {
            builder.setMessage("✅ Safe!\nThis call is NOT SPAM.");
        }

        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void chooseAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 &&
                resultCode == RESULT_OK &&
                data != null) {

            selectedAudioUri = data.getData();
            txtFile.setText("Selected: " +
                    audioUploaderHelper.getFileName(selectedAudioUri));
        }
    }
}