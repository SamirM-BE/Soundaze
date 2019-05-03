package projet4.com.soundaze;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final int AUDIO_SELECTED = 1;
    private static final int REQUEST_ID_PERMISSIONS = 1; //Code utilisé pour le choix du fichier à rogner
    private ImageView btnAudioTrim;
    private static final int ADD_AUDIO = 1001; //Code utilisé pour récupérer le son rogné
    String pickedAudioPath;
    private String recordAudioFileName; // pour l'intent du micro
    private ArrayList<MediaFile> mediaFiles = new ArrayList<>();
    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("djaf", "caca");

        btnAudioTrim = findViewById(R.id.btn_trim);
        btnAudioTrim.setOnClickListener(this);

    }

    //Clique sur le bouton "crop"
    @Override
    public void onClick(View view) {
        if (view == btnAudioTrim) {
            //check storage permission before start trimming
            if (checkStoragePermission()) {
                Intent intent = new Intent(MainActivity.this, FilePickerActivity.class); //Choix fichier pour rognage
                MediaFile file = null;
                for (int i = 0; i < mediaFiles.size(); i++) {
                    if (mediaFiles.get(i).getMediaType() == MediaFile.TYPE_AUDIO) {
                        file = mediaFiles.get(i);
                    }
                }
                //FilePicker, set des options
                intent.putExtra(FilePickerActivity.CONFIGS, new Configurations.Builder()
                        .setCheckPermission(true)
                        .setShowImages(false)
                        .setShowVideos(false)
                        .setShowAudios(true)
                        .setSingleChoiceMode(true)
                        .setSelectedMediaFile(file)
                        .build());
                startActivityForResult(intent, AUDIO_SELECTED);
                dialog = ProgressDialog.show(MainActivity.this, "",
                        "Loading. Please wait...", true);
                //overridePendingTransition(0, 0);
            } else {
                requestStoragePermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUDIO_SELECTED) { //L'utilisateur a choisi un fichier à rogner
            dialog.dismiss();
            if (resultCode == RESULT_OK) {
                mediaFiles.clear();
                mediaFiles.addAll(data.<MediaFile>getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES));

                if (!(mediaFiles.isEmpty())) {
                    MediaFile mediaFile = mediaFiles.get(0);
                    pickedAudioPath = mediaFile.getPath(); //Chemin du fichier choisi

                    Intent intent = new Intent(this, AudioTrimmerActivity.class); //On lance le rognage
                    intent.putExtra("pickedAudioPath", pickedAudioPath); // On lui passe le chemin du fichier choisi par l'user
                    startActivityForResult(intent, ADD_AUDIO);
                }
            }
        } else if (requestCode == ADD_AUDIO) { //Rognage terminé
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String path = data.getExtras().getString("INTENT_AUDIO_FILE");
                    Toast.makeText(MainActivity.this, "Audio stored at " + path, Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    //Bouton workspace
    public void onClickWorkspace(View view) {
        Intent intent = new Intent(this, WorkspaceActivity.class);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivity(intent);
        finish();
        ///TODO : Check ReadStoragePermission
    }

    //si l'user appuye sur le bouton back du téléphone
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, LaunchActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK | intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    //Bouton micro
    public void onClickRecordAudio(View view) {

        final Intent intent = new Intent(this, MicrophoneActivity.class);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);

        if (checkExternalStoragePermission()) {

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

                    recordAudioFileName = input.getText().toString();
                    intent.putExtra("recordedFileName", recordAudioFileName);
                    startActivity(intent); //Lancement du micro
                }
            });
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                    ///TODO : Réaction au cancel ???
                }
            });
            alert.show();
        } else {
            requestExternalStoragePermission();
            ///TODO : check les permissions.
        }

    }


    ///TODO : Permissions génériques
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
