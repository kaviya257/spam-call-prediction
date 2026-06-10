from fastapi import FastAPI, UploadFile, File, HTTPException

import torch
import tempfile
import os
import subprocess
import whisper
import librosa
import numpy as np

from transformers import (
    AutoTokenizer,
    AutoModelForSequenceClassification
)

from deep_translator import GoogleTranslator
from pydub import AudioSegment

from tensorflow.keras.models import load_model

# =========================================
# FASTAPI
# =========================================

app = FastAPI()

# =========================================
# LOAD WHISPER MODEL
# =========================================

whisper_model = whisper.load_model("base")

# =========================================
# LOAD TEXT MODEL
# =========================================

MODEL_PATH = "./spam_distilbert_model"

tokenizer =AutoTokenizer.from_pretrained(MODEL_PATH)

text_model =AutoModelForSequenceClassification.from_pretrained(
        MODEL_PATH
    )

text_model.eval()

# =========================================
# LOAD AUDIO MODEL
# =========================================

audio_model = load_model("spam_call_classifier.h5")

# =========================================
# CONVERT AUDIO TO WAV
# =========================================

def convert_to_wav(input_path):

    wav_path = input_path.replace(
        os.path.splitext(input_path)[1],
        ".wav"
    )

    command = [
        "ffmpeg",
        "-y",
        "-i",
        input_path,
        "-ar",
        "16000",
        "-ac",
        "1",
        wav_path
    ]

    subprocess.run(
        command,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL
    )

    return wav_path

# =========================================
# SPLIT AUDIO
# =========================================

def split_audio(
        file_path,
        chunk_length_ms=30000
):

    audio = AudioSegment.from_wav(file_path)

    chunks = []

    for i in range(
            0,
            len(audio),
            chunk_length_ms
    ):

        chunk = audio[
                i:i + chunk_length_ms
                ]

        chunk_file =f"{file_path}_chunk_{i}.wav"

        chunk.export(
            chunk_file,
            format="wav"
        )

        chunks.append(chunk_file)

    return chunks

# =========================================
# EXTRACT FEATURES
# =========================================

def extract_features(file_path):

    audio, sr = librosa.load(
        file_path,
        sr=16000,
        duration=30
    )

    mfcc = librosa.feature.mfcc(
        y=audio,
        sr=sr,
        n_mfcc=40
    )

    mfcc = np.mean(
        mfcc.T,
        axis=0
    )

    mfcc = mfcc.reshape(1, 40, 1)

    return mfcc

# =========================================
# AUDIO PREDICTION
# =========================================

def predict_audio_spam(file_path):

    features = extract_features(file_path)

    prediction =audio_model.predict(features)

    score = float(prediction[0][0])

    return score

# =========================================
# API
# =========================================

@app.post("/predict")

async def predict_audio(
        file: UploadFile = File(...)
):

    try:

        suffix = os.path.splitext(file.filename)[1]

        with tempfile.NamedTemporaryFile(
                delete=False,
                suffix=suffix
        ) as temp:

            temp.write(await file.read())

            input_audio = temp.name

        # =====================================
        # CONVERT TO WAV
        # =====================================

        wav_audio =convert_to_wav(input_audio)

        # =====================================
        # AUDIO MODEL
        # =====================================

        audio_spam_score = predict_audio_spam(wav_audio)

        # =====================================
        # WHISPER
        # =====================================

        chunks = split_audio(wav_audio)

        full_text = ""

        detected_lang = "en"

        for chunk in chunks:

            result =   whisper_model.transcribe(chunk)

            detected_lang =  result.get("language", "en")

            full_text += (
                    result["text"].strip()
                    + " "
            )

            os.remove(chunk)

        full_text = full_text.strip()

        if not full_text:

            raise Exception(
                "Empty transcription"
            )

        # =====================================
        # TRANSLATE
        # =====================================

        if detected_lang != "en":

            try:

                full_text =      GoogleTranslator(
                        source="auto",
                        target="en"
                    ).translate(full_text)

            except Exception as e:

                print("Translation Error:", e)

        # =====================================
        # TEXT MODEL
        # =====================================

        inputs = tokenizer(
            full_text,
            return_tensors="pt",
            truncation=True,
            padding=True
        )

        with torch.no_grad():

            outputs =    text_model(**inputs)

            probs =  torch.softmax(
                    outputs.logits,
                    dim=1
                )

            text_spam_score =   float(probs[0][1])

        # =====================================
        # FINAL SCORE
        # =====================================

        final_score = (
                text_spam_score +
                audio_spam_score
        ) / 2

        prediction_label = (
            "SPAM"
            if final_score > 0.5
            else "NOT SPAM"
        )

        # =====================================
        # CLEANUP
        # =====================================

        os.remove(input_audio)
        os.remove(wav_audio)

        response = {

            "transcription":
                full_text,

            "detected_language":
                detected_lang,

            "text_spam_score":
                round(
                    text_spam_score * 100,
                    2
                ),

            "audio_spam_score":
                round(
                    audio_spam_score * 100,
                    2
                ),

            "final_spam_score":
                round(
                    final_score * 100,
                    2
                ),

            "prediction":
                prediction_label
        }

        print(response)

        return response

    except Exception as e:

        print("SERVER ERROR:", str(e))

        raise HTTPException(
            status_code=500,
            detail=str(e)
        )