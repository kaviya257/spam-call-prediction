# Intelligent AI-Based Spam Call Detection System

## Overview

The Intelligent AI-Based Spam Call Detection System is an AI-powered mobile application that detects whether an incoming call is spam or legitimate by analyzing both the audio characteristics and the conversation content. The system combines speech recognition, natural language processing, and machine learning techniques to improve spam call identification accuracy.

## Features

* Real-time spam call detection
* Speech-to-text transcription using Whisper
* Multilingual call support with automatic language detection
* Automatic translation of non-English conversations
* Text-based spam classification using DistilBERT
* Audio-based spam classification using MFCC feature extraction
* Combined multimodal prediction using both audio and text analysis
* Complaint generation and PDF report support
* User-friendly Android interface

## System Architecture

1. User uploads or records a call.
2. Audio is converted into WAV format.
3. Whisper transcribes the call conversation.
4. Language is detected automatically.
5. Non-English content is translated to English.
6. DistilBERT analyzes the transcript for spam content.
7. MFCC audio features are extracted from the call.
8. Audio classifier predicts spam probability.
9. Both predictions are combined to generate the final result.
10. The application displays Spam or Not Spam along with confidence scores.

## Tech Stack

### Frontend

* Android Studio
* Java
* XML

### Backend

* Python
* FastAPI

### Machine Learning & AI

* Whisper
* DistilBERT
* TensorFlow / Keras
* PyTorch
* Librosa
* NumPy

### Additional Libraries

* Google Translator API
* Pydub
* FFmpeg

## Novelty

Unlike traditional spam detection systems that rely only on blacklist databases or text analysis, this project uses a multimodal approach by combining voice characteristics and conversation content, resulting in more reliable spam call detection.

## Screenshots

### Login Page

![Login Page](Outputs/Login_Page.jpeg)

### Home Page

![Home Page](Outputs/home_page.jpeg)

### Spam Prediction

![Spam Prediction](Outputs/Spam_prediction.jpeg)

### Normal Call Detection

![Normal Call Detection](Outputs/normal_call.jpeg)

### Spam Call Transcription

![Spam Call Transcription](Outputs/spam_Call_Transcrption.jpeg)

### Normal Call Transcription

![Normal Call Transcription](Outputs/normal_call_Transcription.jpeg)

### Complaint Form

![Complaint Form](Outputs/complaint_pdf_form.jpeg)

### Complaint PDF

![Complaint PDF](Outputs/complaint_pdf.jpeg)

## Installation

```bash
git clone https://github.com/kaviya257/spam-call-prediction.git
```

### Backend Setup

```bash
cd spam_backend
pip install -r requirements.txt
uvicorn main:app --reload
```

### Android Setup

1. Open the project in Android Studio.
2. Configure the backend API URL.
3. Build and run the application on an Android device or emulator.

## Future Enhancements

* Live call monitoring
* Advanced fraud category detection
* Cloud deployment
* User feedback-based model retraining
* Integration with telecom spam databases

## Author

Kaviya B
B.E. Computer Science and Engineering
Coimbatore Institute of Technology
