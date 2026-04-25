package org.example.service;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Records audio from the default microphone using javax.sound.sampled and
 * transcribes it with Vosk (offline, no internet required for STT).
 *
 * Model path resolution order:
 *   1. System property  "vosk.model.path"
 *   2. src/main/resources/vosk-model-es  (classpath resource)
 *   3. ./vosk-model-es  relative to the working directory
 *
 * Download a Spanish model from https://alphacephei.com/vosk/models
 * e.g. vosk-model-small-es-0.42, extract it and rename the folder to
 * "vosk-model-es" inside src/main/resources/.
 */
public class AudioRecordingService {

    // Vosk requires 16 kHz, 16-bit, mono, signed little-endian PCM
    private static final AudioFormat FORMAT =
            new AudioFormat(16000f, 16, 1, true, false);

    private static final int BUFFER_SIZE = 4096;

    private TargetDataLine microphone;
    private Thread recordThread;
    private ByteArrayOutputStream audioBuffer;
    private long startMs;
    private volatile boolean recording = false;

    private static Model voskModel;
    private static boolean modelLoadAttempted = false;

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Returns true if the Vosk model loaded successfully. */
    public static boolean isModelAvailable() {
        loadModelIfNeeded();
        return voskModel != null;
    }

    /** Start capturing microphone audio. Call {@link #stopAndTranscribe()} to finish. */
    public void startRecording() {
        if (recording) return;

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("AudioRecordingService: microphone line not supported.");
            return;
        }

        try {
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(FORMAT);
            microphone.start();
        } catch (LineUnavailableException e) {
            System.err.println("AudioRecordingService: cannot open microphone – " + e.getMessage());
            return;
        }

        audioBuffer = new ByteArrayOutputStream();
        startMs = System.currentTimeMillis();
        recording = true;

        recordThread = new Thread(() -> {
            byte[] buf = new byte[BUFFER_SIZE];
            while (recording) {
                int n = microphone.read(buf, 0, buf.length);
                if (n > 0) audioBuffer.write(buf, 0, n);
            }
        }, "vosk-recorder");
        recordThread.setDaemon(true);
        recordThread.start();
    }

    /**
     * Stop recording and return the Vosk transcription.
     * If the model is not available or transcription fails, returns an empty string.
     * Also sets the {@code durationSeconds} field accessible via {@link #getLastDuration()}.
     */
    public String stopAndTranscribe() {
        if (!recording) return "";
        recording = false;

        long durationMs = System.currentTimeMillis() - startMs;
        lastDuration = formatDuration(durationMs);

        microphone.stop();
        microphone.close();

        try {
            if (recordThread != null) recordThread.join(2000);
        } catch (InterruptedException ignored) {}

        byte[] pcm = audioBuffer.toByteArray();
        return transcribeBytes(pcm);
    }

    private String lastDuration = "0:00";

    /** Returns a "M:SS" string of how long the last recording was. */
    public String getLastDuration() {
        return lastDuration;
    }

    public boolean isRecording() {
        return recording;
    }

    // -----------------------------------------------------------------------
    // Vosk
    // -----------------------------------------------------------------------

    private static void loadModelIfNeeded() {
        if (modelLoadAttempted) return;
        modelLoadAttempted = true;

        // 1. System property override
        String sysProp = System.getProperty("vosk.model.path");
        if (sysProp != null) {
            tryLoadModel(sysProp);
            return;
        }

        // 2. Classpath resource (works when running from IDE or fat-jar)
        URL res = AudioRecordingService.class.getClassLoader()
                .getResource("vosk-model-es");
        if (res != null) {
            tryLoadModel(res.getPath());
            return;
        }

        // 3. Relative to working directory
        File rel = new File("src/main/resources/vosk-model-es");
        if (rel.exists()) {
            tryLoadModel(rel.getAbsolutePath());
            return;
        }

        System.err.println("AudioRecordingService: Vosk model not found. " +
                "Download vosk-model-small-es-0.42 from https://alphacephei.com/vosk/models " +
                "and place it at src/main/resources/vosk-model-es/");
    }

    private static void tryLoadModel(String path) {
        try {
            voskModel = new Model(path);
            System.out.println("AudioRecordingService: Vosk model loaded from " + path);
        } catch (Exception e) {
            System.err.println("AudioRecordingService: failed to load model – " + e.getMessage());
        }
    }

    private String transcribeBytes(byte[] pcm) {
        loadModelIfNeeded();
        if (voskModel == null || pcm.length == 0) return "";

        try (Recognizer rec = new Recognizer(voskModel, 16000f)) {
            // Feed in chunks — acceptWaveform(byte[], int) takes array + number of bytes
            int offset = 0;
            while (offset < pcm.length) {
                int len = Math.min(BUFFER_SIZE, pcm.length - offset);
                byte[] chunk = new byte[len];
                System.arraycopy(pcm, offset, chunk, 0, len);
                rec.acceptWaveForm(chunk, len);
                offset += len;
            }
            // Get final result JSON: {"text": "hola mundo"}
            String json = rec.getFinalResult();
            return extractText(json);
        } catch (IOException e) {
            System.err.println("AudioRecordingService transcription error: " + e.getMessage());
            return "";
        }
    }

    /** Naive JSON extraction — avoids pulling in an extra dependency. */
    private static String extractText(String json) {
        if (json == null) return "";
        int start = json.indexOf("\"text\"");
        if (start < 0) return "";
        int colon = json.indexOf(':', start);
        if (colon < 0) return "";
        int q1 = json.indexOf('"', colon + 1);
        if (q1 < 0) return "";
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 < 0) return "";
        return json.substring(q1 + 1, q2).trim();
    }

    private static String formatDuration(long ms) {
        long secs = ms / 1000;
        return (secs / 60) + ":" + String.format("%02d", secs % 60);
    }
}
