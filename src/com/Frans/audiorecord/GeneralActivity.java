package com.Frans.audiorecord;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GeneralActivity extends Activity {

	private AlertDialog alertDialog;
	public static final String AUDIO_FILE_DIRECTORY = "Recorded audios";
	public static final String AUDIO_WAV_EXT = ".wav";
	public static final int SAMPLE_RATE = 16000;

	@SuppressLint("InflateParams")
	private void showAlertDialog(String message, int iconType) {
		LayoutInflater li = getLayoutInflater();
		View view = li.inflate(R.layout.alert_dialog, null);
		view.findViewById(R.id.btAlertDialog).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Log.i(GeneralActivity.class.getName(), " alertDialog.OK doing DISMISS:  " + alertDialog);
				alertDialog.dismiss();
			}
		});

		ImageView imgAlertDialog = (ImageView) view.findViewById(R.id.imgAlertDialog);
		imgAlertDialog.setImageResource(iconType);
		TextView txtAlertDialog = (TextView) view.findViewById(R.id.txtAlertDialog);
		txtAlertDialog.setText(message);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Confirmação");
		builder.setView(view);
		alertDialog = builder.create();
		alertDialog.show();
	}

	public void showAlertDialogInfo(String message) {
		showAlertDialog(message, R.drawable.icon_info);
	}

	public void showAlertDialogError(String message) {
		showAlertDialog(message, R.drawable.icon_error);
	}

	public void showToastMessage(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	public void setEnableImageButton(ImageButton btn, boolean enabled, int btnEnabledImage, int btnDisabledImage) {
		btn.setEnabled(enabled);
		if (enabled) {
			btn.setImageResource(btnEnabledImage);
		} else {
			btn.setImageResource(btnDisabledImage);
		}
	}

	@SuppressLint("SdCardPath")
	public File getAudioFile(String MusicName, String audioSufix) {
		File mediaStorageDir = new File("/sdcard/", AUDIO_FILE_DIRECTORY);
		if (!mediaStorageDir.exists()) {
			mediaStorageDir.mkdirs();
		}
		File imageLocalFile = new File(mediaStorageDir.getPath() + File.separator + MusicName + audioSufix +AUDIO_WAV_EXT);
		Log.i(MainActivity.class.getName(), " getAudioFile.exists: " + imageLocalFile.exists());
		if (imageLocalFile.exists()) {
			Log.i(GeneralActivity.class.getName(), " FILE_ALREADY_EXIST... DELETING...");
			imageLocalFile.delete(); // help!!
		}
		return imageLocalFile;
	}

	public void writeWaveHeaders(final File waveFile) throws IOException {
		DataInputStream input = null;
		DataOutputStream output = null;
		try {
			byte[] rawData = new byte[(int) waveFile.length()];
			input = new DataInputStream(new FileInputStream(waveFile));
			input.read(rawData);

			output = new DataOutputStream(new FileOutputStream(waveFile));
			// WAVE header
			// see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
			writeString(output, "RIFF"); // chunk id
			writeInt(output, 36 + rawData.length); // chunk size
			writeString(output, "WAVE"); // format
			writeString(output, "fmt "); // subchunk 1 id
			writeInt(output, 16); // subchunk 1 size
			writeShort(output, (short) 1); // audio format (1 = PCM)
			writeShort(output, (short) 1); // number of channels
			writeInt(output, SAMPLE_RATE); // sample rate
			writeInt(output, SAMPLE_RATE * 2); // byte rate
			writeShort(output, (short) 2); // block align
			writeShort(output, (short) 16); // bits per sample
			writeString(output, "data"); // subchunk 2 id
			writeInt(output, rawData.length); // subchunk 2 size
			// Audio data (conversion big endian -> little endian)
			short[] shorts = new short[rawData.length / 2];
			ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
			ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
			for (short s : shorts) {
				bytes.putShort(s);
			}
			output.write(bytes.array());
		} finally {
			try {
				if (input != null)
					input.close();
				if (output != null)
					output.close();
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void writeInt(final DataOutputStream output, final int value) throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
		output.write(value >> 16);
		output.write(value >> 24);
	}

	private void writeShort(final DataOutputStream output, final short value) throws IOException {
		output.write(value >> 0);
		output.write(value >> 8);
	}

	private void writeString(final DataOutputStream output, final String value) throws IOException {
		for (int i = 0; i < value.length(); i++) {
			output.write(value.charAt(i));
		}
	}
}
