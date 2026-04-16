package com.example.spam_new;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;

public class ComplaintPdfGenerator extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ComplaintData data =
                (ComplaintData) getIntent().getSerializableExtra("complaintData");

        generatePdf(data);
    }

    private void generatePdf(ComplaintData data) {
        try {
            File dir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOCUMENTS),
                    "SpamComplaints");

            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "Complaint_" + System.currentTimeMillis() + ".pdf");

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            document.add(new Paragraph("CYBER CRIME COMPLAINT\n\n",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));

            document.add(new Paragraph("Phone Number Used by Scammer: " + data.phoneNumber));
            document.add(new Paragraph("Type of Scam: " + data.scamType));
            document.add(new Paragraph("Amount Lost: " + data.amountLost));
            document.add(new Paragraph("\nDescription:\n" + data.description));

            document.add(new Paragraph("\n\nI hereby declare that the above information is true.",
                    FontFactory.getFont(FontFactory.HELVETICA, 10)));

            document.add(new Paragraph("\nSignature:\nDate:"));

            document.close();

            Toast.makeText(this, "PDF Generated!", Toast.LENGTH_SHORT).show();

            // 🔥 OPEN PDF
            openPdf(file);

        } catch (Exception e) {
            Toast.makeText(this, "PDF generation failed", Toast.LENGTH_LONG).show();
        }
    }

    private void openPdf(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            Uri uri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file
            );

            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_LONG).show();
        }

        finish(); // close activity after opening
    }
}