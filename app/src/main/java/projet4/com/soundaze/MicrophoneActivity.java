package projet4.com.soundaze;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MicrophoneActivity extends AppCompatActivity {

    private Button play, stop, record;
    private MediaRecorder myMicrophoneRecorder;
    private String outputFile;
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    Uri ff= null;
    //private int isRecording = 0;


    /************************************************************/
    /***********************PERMISSIONS STRINGS******************/
    /************************************************************/
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToWriteAccepted = false;
    private String [] permissions = {"android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"};
    /************************************************************/



    /************************************************************/
    /***********************PERMISSIONS**************************/
    /************************************************************/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 200:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                permissionToWriteAccepted  = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) MicrophoneActivity.super.finish();
        if (!permissionToWriteAccepted ) MicrophoneActivity.super.finish();

    }
    /************************************************************/


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microphone);

        // Add the following code to your onCreate
        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            requestPermissions(permissions, requestCode);
        }

        //on check les permission pour les uri et manage les doc
        checkPermission();

        //on récupère l'intent du nom du fichier choisi par l'user

        Intent intent = getIntent();
        String easyPuzzle = intent.getExtras().getString("epuzzle");



        /**************/
        /****Buttons***/
        /**************/
        play = findViewById(R.id.play);
        stop = findViewById(R.id.stop);
        record = findViewById(R.id.record);
        stop.setEnabled(false);
        play.setEnabled(false);
        /**************************/
        /****MicrophoneRecorder****/
        /**************************/




        String tmp = "/"+easyPuzzle;

        Toast.makeText(getApplicationContext(), "name :" + tmp, Toast.LENGTH_LONG).show();


        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath()+tmp;

        Toast.makeText(getApplicationContext(), "name2 :" + outputFile, Toast.LENGTH_LONG).show();


        //ff = getAudioContentUri(this); je crois que cette méthode pose problème

        ff = Uri.fromFile(new File(outputFile));

        boolean so = ff==null;

        String f = ff.toString();


        myMicrophoneRecorder = new MediaRecorder();
        try
        {
            myMicrophoneRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }
        catch(Exception e)
        {
            openPermissionComfirmation();
        }
        myMicrophoneRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myMicrophoneRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myMicrophoneRecorder.setOutputFile(outputFile);
        /**********************/
        /****RecordListener****/
        /**********************/
        record.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    if(myMicrophoneRecorder == null){
                        myMicrophoneRecorder = new MediaRecorder();
                        try
                        {
                            myMicrophoneRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        }
                        catch(Exception e)
                        {
                            openPermissionComfirmation();
                        }
                        myMicrophoneRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        myMicrophoneRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                        myMicrophoneRecorder.setOutputFile(outputFile);
                    }
                    myMicrophoneRecorder.prepare();
                    myMicrophoneRecorder.start();

                    //on récupère l'uri seulement s'il appuye sur record, sinon ça sert à rien



                    //on va ensuite save cet uri dans notre workspace via notre fichier interne dans la
                    //class workSpaceActivity

                    if(! containsInternal(ff) ){
                        save(ff.toString());
                    }
                    Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), "Find your record in workspace", Toast.LENGTH_LONG).show();
                }
                catch (IllegalStateException ise)
                {
                    // make something ...
                }
                catch (IOException ioe)
                {
                    // make something
                }
                record.setEnabled(false);
                play.setEnabled(false);
                //isRecording = 1;
                //on fait attendre 1 sec ici avant de rendre pause enable pour faire éviter de bugger
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stop.setEnabled(true);

            }
        });
        /**********************/
        /****StopListener******/
        /**********************/
        stop.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                myMicrophoneRecorder.stop();
                myMicrophoneRecorder.release();
                myMicrophoneRecorder = null;
                //isRecording = 0;
                record.setEnabled(true);
                stop.setEnabled(false);
                play.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Audio Recorder stopped", Toast.LENGTH_LONG).show();

            }
        });
        /**********************/
        /******PlayListener****/
        /**********************/
        play.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(outputFile);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    // make something
                }
            }
        });

    }

    public void openPermissionComfirmation()
    {
        Intent intent = new Intent(this,PermissionActivity.class);
        startActivity(intent);

    }

    //click sur le bouton back
    public void onBack(View view){

        if(myMicrophoneRecorder != null){
            //on fait attendre 1 sec ici avant de stop le tout

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //if (isRecording == 1) myMicrophoneRecorder.stop();

            //myMicrophoneRecorder.stop();
            myMicrophoneRecorder.release();
            myMicrophoneRecorder = null;
        }
        Intent intent = new Intent(this, MainActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        startActivity(intent);
    }

    //méthode pour pour avoir un uri sur base d'un file path audio

    public Uri getAudioContentUri(Context context) {
        String filePath = outputFile;
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID },
                MediaStore.Audio.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            //if (imageFile.exists()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.DATA, filePath);
            return context.getContentResolver().insert(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            //} else {
            // return null;
            //}
        }
    }


    //cette méthode check les permission pour les uri
    public void checkPermission(){

        //check if we have the permission

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(WorkspaceActivity.this, "permission is not granted", Toast.LENGTH_SHORT).show();
        }

        //request the permission
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                //Toast.makeText(WorkspaceActivity.this, "permission is granted", Toast.LENGTH_SHORT).show();

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
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