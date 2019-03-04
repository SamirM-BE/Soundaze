package projet4.com.soundaze;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    MediaPlayer mMediaPlayer = new MediaPlayer();
    static final int AUDIO_SELECTED = 1; //Cette variable sert à vérifier si l'user a choisi un son
    Uri audio; //URI de l'audio séléctionné par l'user
    int alreadyLoaded = 0;


    public void onClickLoadFile(View view)
    {
        if(alreadyLoaded==1) mMediaPlayer.reset(); //on reset le player si un fichier a déjà été chargé, évite la superposition de sons différents
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); //Un intent de type ACTION_GET_CONTENT permet à l'utilisateur de sélectionner des fichiers
        intent.setType("audio/*"); //On veut des fichiers audio
        intent.addCategory(Intent.CATEGORY_OPENABLE); //On veut des fichiers qui sont ouvrable

        Intent finalIntent = Intent.createChooser(intent, "Choisissez un fichier audio"); //Normalement la nouvelle fenetre doit s'appeler "Choisissez .. " mais je vois aucune diff

        startActivityForResult(finalIntent, AUDIO_SELECTED); //ForResult = quand on attend un résultat sinon juste StartActivity()

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == AUDIO_SELECTED) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked an audio
                audio = data.getData(); // The Intent's data Uri identifies which audio was selected. (audio = URI)
                alreadyLoaded = 1;

                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(getApplicationContext(), audio);
                String artist =  metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                ImageView music1 = (ImageView) findViewById(R.id.music1);
                music1.setImageResource(R.drawable.musicicon);

                TextView text_title = (TextView) findViewById(R.id.text_title);
                text_title.setText(title+" : "+artist);

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mMediaPlayer.setDataSource(getApplicationContext(), audio);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Clique sur start
    public void onClickPlay(View view)
    {
        mMediaPlayer.start();
    }

    //Clique sur pause
    public void onClickPause(View view)
    {
        mMediaPlayer.pause();
    }

    //Clique sur reset
    public void onClickReset(View view)
    {
        mMediaPlayer.reset();

        //Chaque audio chargé genère une image, quand on reset on supprime l'image de l'audio précédemment chargé
        ImageView music1 = (ImageView) findViewById(R.id.music1);
        music1.setImageDrawable(null);

    }






}
