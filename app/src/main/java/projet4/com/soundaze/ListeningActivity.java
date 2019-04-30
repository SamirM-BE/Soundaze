package projet4.com.soundaze;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class ListeningActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnPlay, btnBack, btnFor;
    private Button btnEql;
    private SeekBar seekBar;
    private MediaPlayer mediaPlayer;
    private Runnable runnable;
    private Handler handler;
    private Uri uri;
    private static final int TRIMER = 1004;
    private static final int REQUEST_ID_PERMISSIONS = 1;
    private static String musicName;//va être utilisée pour le nom de la musique
    String pickedAudioPath; //Musique qui a été choisi dans le workspace

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening);
        // Get a Uri from an Intent
        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        uri = intent.getParcelableExtra("song");
        pickedAudioPath = intent.getStringExtra("pickedAudioPath");
        //on récupère le nom de a musique à partir de son uri

        musicName = getFileName(uri);


        TextView text_title = findViewById(R.id.text_title);
        text_title.setText(musicName);
        btnPlay = findViewById(R.id.btnPlay);
        btnBack = findViewById(R.id.btnBack);
        btnFor = findViewById(R.id.btnFor);

        btnEql = findViewById(R.id.btn_eql);

        handler = new Handler();
        seekBar = findViewById(R.id.seekbar);

        mediaPlayer = MediaPlayer.create(this, this.uri);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setScreenOnWhilePlaying(true);

        btnFor.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnEql.setOnClickListener(this);

        btnEql.setOnClickListener(btn_EqlListener);


        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                seekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                changeSeekbar();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b){
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    //s'il click sur l'option back, on relance la main qui va ouvrir l'explorateur de fichier
    public void onBack(View view) {

        //on arrête le médiaplayer courant
        //mediaPlayer.reset();
        //mediaPlayer.prepare();
        mediaPlayer.stop();
        //mediaPlayer.release();
        //mediaPlayer = null;

        //on retourne sur l'écran précédent de la main

        Intent intent = new Intent(this, WorkspaceActivity.class);
        startActivity(intent);

    }

    //on récupère le nom de la musique via son uri
    public String getFileName(Uri uri) {
        String result = null;
        //TODO: "content" changé en "song"
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void changeSeekbar(){
        seekBar.setProgress(mediaPlayer.getCurrentPosition());

        if(mediaPlayer.isPlaying()){
            runnable = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar();
                }
            };

            handler.postDelayed(runnable, 1000);
        }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnPlay:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    btnPlay.setText(">");
                }else{
                    mediaPlayer.start();
                    btnPlay.setText("||");
                    changeSeekbar();
                }
                break;
            case R.id.btnFor:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+5000);
                break;
            case R.id.btnBack:
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-5000);
                break;
        }
    }

    //on met la méthode en com pour la présentation de mardi


    public void onClickTrim(View view) {
        Intent intent = new Intent(this, AudioTrimmerActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        intent.putExtra("pickedAudioPath", pickedAudioPath);
        //check storage permission before start trimming
        if (checkStoragePermission()) {
            startActivityForResult(intent, TRIMER);
            overridePendingTransition(0, 0);
        } else {
            requestStoragePermission();
        }

    }


    private View.OnClickListener btn_EqlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;

                if (handler != null) {
                    handler.removeCallbacks(runnable);
                }
            }
            onEqual(uri);

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TRIMER) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    //audio trim result will be saved at below path
                    String path = data.getExtras().getString("INTENT_AUDIO_FILE");
                    Toast.makeText(ListeningActivity.this, "Audio stored at " + path, Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    ////CES DEUX METHODES SONT REPETITIVES, IL FAUT LES METTRE DANS UNE CLASSE
    private boolean checkStoragePermission() {
        return (ActivityCompat.checkSelfPermission(ListeningActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ListeningActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(ListeningActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO},
                REQUEST_ID_PERMISSIONS);
    }

    public void onClickConvert(View view) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            Toast.makeText(this, "Stop playing.", Toast.LENGTH_SHORT).show();
            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
        }
        Intent intent = new Intent(this, AudioConversionActivity.class);
        intent.putExtra("uriListenning", uri);
        startActivity(intent);
    }

    public void onEqual(Uri uri){

        Intent intent = new Intent(this, EqualizerActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        intent.putExtra("song", uri);
        //mediaPlayer.release();
        startActivity(intent);
    }

    /*public void openEqualizer()
    {

        //on passe dans l'activité de l'qualizer
        //je récupère la musique sélectionnée et je la lance dans le lecteur
        final Uri vr = Uri.parse(arrayListUriVal.get(position));
        onEqual(vr);
        Intent intent = new Intent(this,EqualizerActivity.class);
        startActivity(intent);
    }*/
}
