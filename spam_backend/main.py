from fastapi import FastAPI, UploadFile, File, HTTPException
import torch
import tempfile
import os
import subprocess
import whisper
from transformers import AutoTokenizer, AutoModelForSequenceClassification
from deep_translator import GoogleTranslator
from pydub import AudioSegment

app = FastAPI()

whisper_model = whisper.load_model("base")

MODEL_PATH = "./spam_distilbert_model"
tokenizer = AutoTokenizer.from_pretrained(MODEL_PATH)
model = AutoModelForSequenceClassification.from_pretrained(MODEL_PATH)
model.eval()


def convert_to_wav(input_path):
    wav_path = input_path.replace(os.path.splitext(input_path)[1], ".wav")
    command = [
        "ffmpeg", "-y",
        "-i", input_path,
        "-ar", "16000",
        "-ac", "1",
        wav_path
    ]
    subprocess.run(command, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    return wav_path


def split_audio(file_path, chunk_length_ms=30000):
    audio = AudioSegment.from_wav(file_path)
    chunks = []

    for i in range(0, len(audio), chunk_length_ms):
        chunk = audio[i:i + chunk_length_ms]
        chunk_file = f"{file_path}_chunk_{i}.wav"
        chunk.export(chunk_file, format="wav")
        chunks.append(chunk_file)

    return chunks


@app.post("/predict")
async def predict_audio(file: UploadFile = File(...)):
    try:
        suffix = os.path.splitext(file.filename)[1]

        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp:
            temp.write(await file.read())
            input_audio = temp.name

        wav_audio = convert_to_wav(input_audio)

       
        chunks = split_audio(wav_audio)

        full_text = ""
        detected_lang = "en"

        for chunk in chunks:
            result = whisper_model.transcribe(chunk)
            detected_lang = result.get("language", "en")
            full_text += result["text"].strip() + " "

            os.remove(chunk)  

        full_text = full_text.strip()

        if not full_text:
            raise ValueError("Empty transcription")

        
        if detected_lang != "en":
            try:
                full_text = GoogleTranslator(
                    source="auto",
                    target="en"
                ).translate(full_text)
            except:
                pass

        inputs = tokenizer(
            full_text,
            return_tensors="pt",
            truncation=True,
            padding=True
        )

        with torch.no_grad():
            outputs = model(**inputs)
            probs = torch.softmax(outputs.logits, dim=1)
            spam_prob = probs[0][1].item()

        return {
            "text": full_text,
            "spam_probability": round(spam_prob * 100, 2),
            "label": "SPAM" if spam_prob > 0.5 else "NOT SPAM"
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))