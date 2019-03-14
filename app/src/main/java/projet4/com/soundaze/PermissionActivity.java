package projet4.com.soundaze;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PermissionActivity extends AppCompatActivity
{

    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        confirmButton = findViewById(R.id.accept);
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