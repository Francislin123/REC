package com.Frans.audiorecord;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

@SuppressLint({ "SdCardPath", "SimpleDateFormat", "NewApi" })
public class MainActivity extends GeneralActivity {
	public static final int SAMPLE_RATE = 16000;

	private File audioFile;

	private AudioRecord audioRecorder;
	private MediaPlayer audioPlayer;
	private ProgressBar progressBar;
	private boolean isRecording = false;
	private short[] audioBuffer;

	private boolean faceTrained = false;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Getting UI components references
		final ImageButton btnRecordAudio = (ImageButton) findViewById(R.id.btnRecordAudio);
		final ImageButton btnPlayAudio = (ImageButton) findViewById(R.id.btnPlayAudio);
		progressBar = (ProgressBar) findViewById(R.id.pbarUploadFile);
		final String MusicName = getIntent().getStringExtra("MusicName");

		setEnableImageButton(btnPlayAudio, false, R.drawable.btn_play_min, R.drawable.btn_play_grayed_min);

		int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		audioBuffer = new short[bufferSize];
		audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize);

		// ---------------------------------------------------------------------------------------//
		// ----------------------------- AUDIO SETTINGS
		// ------------------------------------------//
		// ---------------------------------------------------------------------------------------//

		btnRecordAudio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				try {
					if (!isRecording) {
						btnRecordAudio.setImageResource(R.drawable.btn_record2_min);
						audioRecorder.startRecording();
						audioFile = getAudioFile(MusicName, "");
						writeAudioFile(audioFile);
						isRecording = true;
						setEnableImageButton(btnPlayAudio, false, R.drawable.btn_play_min,R.drawable.btn_play_grayed_min);
					} else {
						btnRecordAudio.setImageResource(R.drawable.btn_record1_min);
                        audioRecorder.stop();
                        //AudioActivityUtil.writeWaveHeaders(audioFile);
                        writeWaveHeaders(audioFile);
                        isRecording = false;
                        showToastMessage("Saving Audio..." + audioFile.getName());
                        setEnableImageButton(btnPlayAudio, true, R.drawable.btn_play_min, R.drawable.btn_play_grayed_min);
                        //voiceTrained = true;
						if (faceTrained) {
						}
					}
				} catch (IOException ex) {
					showToastMessage(ex.getMessage());
				}
			}
		});

		btnPlayAudio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {

				if (audioPlayer != null && audioPlayer.isPlaying()) {
					audioPlayer.pause();
					btnPlayAudio.setImageResource(R.drawable.btn_play_min);
				} else {
					try {
						showToastMessage("Playing " + audioFile.getAbsolutePath());
						FileInputStream fis = new FileInputStream(audioFile);
						audioPlayer = new MediaPlayer();
						audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						audioPlayer.reset();
						audioPlayer.setDataSource(fis.getFD());
						audioPlayer.prepare();
						audioPlayer.start();
						btnPlayAudio.setImageResource(R.drawable.btn_pause_min);
						fis.close();
					} catch (Exception ex) {
						//btnPlayAudio.setImageResource(R.drawable.btn_play_min);
						Log.w(MainActivity.class.getName(), ex);
						showToastMessage(ex.getMessage());
					}
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		if (audioRecorder != null) {
			audioRecorder.release();
			audioRecorder = null;
		}
		if (audioPlayer != null) {
			audioPlayer.release();
			audioPlayer = null;
		}
		super.onDestroy();
	}

	// --------------------------------------------------------------------------------------//
	// ------------------------------ UTILITIES
	// ---------------------------------------------//
	// --------------------------------------------------------------------------------------//
	private void writeAudioFile(final File file) {
		Thread mFile = new Thread(new Runnable() {
			@Override
			public void run() {
				DataOutputStream output = null;
				try {
					output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
					while (isRecording) {
						double sum = 0;
						int readSize = audioRecorder.read(audioBuffer, 0, audioBuffer.length);
						for (int i = 0; i < readSize; i++) {
							output.writeShort(audioBuffer[i]);
							sum += audioBuffer[i] * audioBuffer[i];
						}
						if (readSize > 0) {
							final double amplitude = sum / readSize;
							progressBar.setProgress((int) Math.sqrt(amplitude));
						}
					}
				} catch (IOException e) {
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
				} finally {
					progressBar.setProgress(0);
					if (output != null) {
						try {
							output.flush();
							output.close();
						} catch (IOException e) {
							Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		});
		mFile.start();
	}

}