package com.Frans.audiorecord;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Main_Screen extends Activity {

	EditText musicName;
	ImageButton btnConfiguration;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_screen);

		ImageButton btnConfiguration = (ImageButton) findViewById(R.id.btnUserConfiguration);
		final EditText musicName = (EditText) findViewById(R.id.txtMusic);

		btnConfiguration.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final String Name = musicName.getText().toString();
				if (Name.length() == 0) {
					musicName.requestFocus();
					musicName.setError("Digite o nome do áudio !");

				} else if (!Name.matches("^\\p{L}+[\\p{L}\\p{Z}\\p{P}]+[_A-Za-z0-9-\\+ s]{0,}")) { //"^\\p{L}+[\\p{L}\\p{Z}\\p{P}]{0,}" "^[_A-Za-z0-9-\\+ s]+"
					musicName.requestFocus();
					musicName.setError("Escreva a musica com apenas letras");

				} else {
					Toast.makeText(Main_Screen.this, "Bem vindo ao Rec!", Toast.LENGTH_LONG).show();
					Intent i = new Intent(getApplicationContext(), MainActivity.class);
					i.putExtra("MusicName", musicName.getText().toString());
					startActivity(i);
				}

			}

		});

	}
}
