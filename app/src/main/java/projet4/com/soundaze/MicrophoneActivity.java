package projet4.com.soundaze;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

import static android.graphics.Color.parseColor;

public class MicrophoneActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO = 0;
    private static String AUDIO_FILE_PATH;
    Uri audio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microphone);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark)));
        }

        requestPermission(this, Manifest.permission.RECORD_AUDIO);
        requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Intent intent = getIntent();
        String recordedFileName = intent.getExtras().getString("recordedFileName");
        AUDIO_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/"+recordedFileName+".wav";
        recordAudio();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Audio recorded successfully!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Audio was not recorded", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }


    public void recordAudio() {


        AndroidAudioRecorder.with(this)
                // Required
                .setFilePath(AUDIO_FILE_PATH)
                .setColor(parseColor("#111312")) /// COULEUR peut être a changer
                .setRequestCode(REQUEST_RECORD_AUDIO)

                // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_48000)
                .setAutoStart(false)
                .setKeepDisplayOn(true)

                // Start recording
                .record();

        audio = Uri.fromFile(new File(AUDIO_FILE_PATH));
        if(! containsInternal(audio) ){
            save(audio.toString());
        }
    }

    public static void requestPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 0);
        }
    }

    public boolean containsInternal(Uri uri) {
        try {
            FileInputStream fis = openFileInput(WorkspaceActivity.filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while((line = reader.readLine()) != null){

                if(Uri.parse(line).equals(uri)){
                    return true;
                }
            }
            return false;
        }catch(FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //pour sauvegarder lign par ligne les uri dans le fichier

    public void save(String string) {

        //je fais un readline pour récupérer la liste déjà existante
        ArrayList<String> ch = new ArrayList<>();
        try {
            ch = readInternal();
        }catch(FileNotFoundException e){
            //Toast.makeText(WorkspaceActivity.this,"file app has been removed", Toast.LENGTH_SHORT).show();
        }
        //on ajoute le nouveau string à ajouter à ceux déjà existant
        ch.add(string);
        try {

            FileOutputStream fos = openFileOutput(WorkspaceActivity.filename, Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos), true);
            //on écrit l'arraylist dans le fichier

            for(int i = 0; i<ch.size();i++) {

                writer.println(ch.get(i));

            }
            //on ferme le writer
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //méthode pour lire dans un fichier interne au téléphone l'arraylist d'uri
    //lance une file not foud exeption si le fichier aurait été supprimé


    public ArrayList<String> readInternal() throws FileNotFoundException {
        //on crée l'arraylist destinée à contenir les uri
        ArrayList<String> uri = new ArrayList<>();
        try {
            FileInputStream fis = openFileInput(WorkspaceActivity.filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while((line = reader.readLine()) != null){
                //on ajoute l'uri dans l'arraylist, attention de le reconvertir en uri valide
                uri.add(line);
            }
            reader.close();
            return uri;
        }catch(FileNotFoundException e){
            e.printStackTrace();
            throw new FileNotFoundException();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;


    }

}