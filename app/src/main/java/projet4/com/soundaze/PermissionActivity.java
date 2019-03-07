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

public class PermissionActivity extends AppCompatActivity
{

    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        confirmButton = (Button) findViewById(R.id.accept);
        confirmButton.setOnClickListener(confirmButtonListener);
    }

    private View.OnClickListener confirmButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            backToRecording();
        }
    };

    public void backToRecording()
    {
        Intent intent = new Intent(this,MicrophoneActivity.class);
        startActivity(intent);

    }
}