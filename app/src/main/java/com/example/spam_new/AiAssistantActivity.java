package com.example.spam_new;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class AiAssistantActivity extends AppCompatActivity {

    EditText etDescription, etAmount, etPhone;
    String detectedText, conclusion;

    Spinner scamTypeSpinner;
    Button btnGenerate;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        etPhone = findViewById(R.id.etPhone);
        scamTypeSpinner = findViewById(R.id.scamTypeSpinner);
        btnGenerate = findViewById(R.id.btnGenerate);

        String[] scamTypes = {
                "Bank / OTP Fraud",
                "Prize / Lottery Scam",
                "Loan Scam",
                "Investment Scam",
                "Other"
        };

        scamTypeSpinner.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        scamTypes)
        );

        btnGenerate.setOnClickListener(v -> {
            ComplaintData data = new ComplaintData(
                    etPhone.getText().toString(),
                    scamTypeSpinner.getSelectedItem().toString(),
                    etDescription.getText().toString(),
                    etAmount.getText().toString()
            );

            Intent intent = new Intent(this, ComplaintPdfGenerator.class);
            intent.putExtra("complaintData", data);
            startActivity(intent);
        });
        detectedText = getIntent().getStringExtra("detectedText");
        conclusion = getIntent().getStringExtra("conclusion");

// AI-like auto suggestion
        if (conclusion != null && conclusion.equalsIgnoreCase("Spam")) {
            etDescription.setText(
                    "I received a suspicious call where the caller attempted to obtain sensitive information. "
                            + "Based on the call content, this appears to be a scam. "
                            + "Extracted text: " + detectedText
            );
        }

    }
}
