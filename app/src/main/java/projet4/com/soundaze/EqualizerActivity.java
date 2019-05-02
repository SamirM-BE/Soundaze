package projet4.com.soundaze;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import com.bullhead.equalizer.EqualizerFragment;

import androidx.appcompat.app.AppCompatActivity;


public class EqualizerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Uri uri; //Notre son

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);


        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        uri = intent.getParcelableExtra("song");
        mediaPlayer = MediaPlayer.create(this, uri);

        int sessionId = mediaPlayer.getAudioSessionId();
        mediaPlayer.setLooping(true);
        EqualizerFragment equalizerFragment = EqualizerFragment.newBuilder()
                .setAccentColor(Color.DKGRAY)
                .setAudioSessionId(sessionId)
                .build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.eqFrame, equalizerFragment)
                .commit();
    }

    @Override
    protected void onPause() {
        try {
            mediaPlayer.pause();
        } catch (Exception ex) {
            //ignore
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mediaPlayer.start();
        } catch (Exception ex) {
            //ignore
        }
    }
}