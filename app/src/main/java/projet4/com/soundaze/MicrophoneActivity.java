package projet4.com.soundaze;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;
import java.io.IOException;
import android.content.Intent;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MicrophoneActivity extends AppCompatActivity {

    private Button play, stop, record;
    private MediaRecorder myMicrophoneRecorder;
    private String outputFile;

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



        /**************/
        /****Buttons***/
        /**************/
        play = (Button) findViewById(R.id.play);
        stop = (Button) findViewById(R.id.stop);
        record = (Button) findViewById(R.id.record);
        stop.setEnabled(false);
        play.setEnabled(false);
        /**************************/
        /****MicrophoneRecorder****/
        /**************************/
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
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
                    myMicrophoneRecorder.prepare();
                    myMicrophoneRecorder.start();
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
                stop.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
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
}