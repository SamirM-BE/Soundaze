package projet4.com.soundaze;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity /*implements View.OnClickListener*/ {

    private static final int ADD_AUDIO = 1001;
    private static final int REQUEST_ID_PERMISSIONS = 1;
    private ImageView btnAudioTrim;

    private String dia; // pour l'intent du micro

    private boolean clickedOk = false; //pour ma méthode handle pour savoir quand l'user a clické sur ok
    private boolean clickedCancel = false; //pour savoir si l'user a cliqué sur canceled sur le pop up
    private boolean hand = false; //variable pour vérifier qu'on appelle hand seulement 1 seule fois

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*partie liée au trim momentanément effacée
        //btnAudioTrim = findViewById(R.id.btn_trim);
        //btnAudioTrim.setOnClickListener(this);

        */
    }

    static final int AUDIO_SELECTED = 1;
    String pickedAudioPath;
    private ArrayList<MediaFile> mediaFiles = new ArrayList<>();


    /* partie éliminée de la mainactivity pour le moment pour éviter que le prof click dessus

    //Clique sur le bouton "crop"
    @Override
    public void onClick(View view) {
        if (view == btnAudioTrim) {
            //check storage permission before start trimming
            if (checkStoragePermission()) {
                //startActivityForResult(new Intent(MainActivity.this, AudioTrimmerActivity.class), ADD_AUDIO); //écran suivant
                Intent intent = new Intent(MainActivity.this, FilePickerActivity.class);
                MediaFile file = null;
                for (int i = 0; i < mediaFiles.size(); i++) {
                    if (mediaFiles.get(i).getMediaType() == MediaFile.TYPE_AUDIO) {
                        file = mediaFiles.get(i);
                    }
                }
                intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(false)
                        .setShowVideos(false)
                        .setShowAudios(true)
                        .setSingleChoiceMode(true)
                        .setSelectedMediaFile(file)
                        .build());
                startActivityForResult(intent, AUDIO_SELECTED);
                //overridePendingTransition(0, 0);
            } else {
                requestStoragePermission();
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_AUDIO) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    //audio trim result will be saved at below path
                    String path = data.getExtras().getString("INTENT_AUDIO_FILE");
                    Toast.makeText(MainActivity.this, "Audio stored at " + path, Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == AUDIO_SELECTED) {
            mediaFiles.clear();
            mediaFiles.addAll(data.<MediaFile>getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES));

            MediaFile mediaFile = mediaFiles.get(0);
            pickedAudioPath = mediaFile.getPath();

            Intent intent = new Intent(this, AudioTrimmerActivity.class); //On prépare l'intent pour le passage à l'écran suivant
            intent.putExtra("pickedAudioPath", pickedAudioPath);
            startActivityForResult(intent, ADD_AUDIO);

        }
    }

    */

    public void onClickWorkspace(View view) {
        Intent intent = new Intent(this, WorkspaceActivity.class);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivity(intent);
        requestAudioPermission();

    }

    //click pour record un audio
    public void onClickAudio(View view){

        final Intent intent = new Intent(this, MicrophoneActivity.class);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("What name do you wanna give to your audio");
        alert.setMessage("Be creative!");
        final String string;

        //on gère la boite de dialogue

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Do something with value!




                dia = input.getText().toString();

                if(checkExternalStoragePermission() /*&& clickedOk*/) {
                    intent.putExtra("epuzzle", dia);
                    startActivity(intent);
                }else {
                    //requestAudioPermission();
                    requestExternalStoragePermission();
                    //startActivity(intent);
                }

                clickedOk = true;


            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
                clickedCancel = true;
            }
        });

        alert.show();

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO},
                REQUEST_ID_PERMISSIONS);
    }

    private boolean checkStoragePermission() {
        return (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean checkAudioPermission() {
        return (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.RECORD_AUDIO},
                REQUEST_ID_PERMISSIONS);
    }

    private void requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO},
                REQUEST_ID_PERMISSIONS);
    }

    private boolean checkExternalStoragePermission() {
        return (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission granted, Click again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
