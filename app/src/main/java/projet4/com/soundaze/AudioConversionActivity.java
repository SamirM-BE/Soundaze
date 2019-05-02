package projet4.com.soundaze;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URISyntaxException;

import androidx.appcompat.app.AppCompatActivity;
import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class AudioConversionActivity extends AppCompatActivity {

    String musicName;
    Uri uri;
    private TextView txtMusicNameConv;
    private TextView txtInputFormat;
    private Spinner spinnerType;

    public static String getMimeType(Context context, Uri uri) {
        String extension;

        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());

        }

        return extension;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_conversion);

        Intent listenningIntent = getIntent();
        uri = listenningIntent.getParcelableExtra("uriListenning");
        musicName = getFileName(uri);

        txtMusicNameConv = findViewById(R.id.txtMusicNameConv);
        txtInputFormat = findViewById(R.id.txtInputFormat);

        spinnerType = findViewById(R.id.spinnerType);
        spinnerType.getBackground().setColorFilter(getResources().getColor(R.color.blanc), PorterDuff.Mode.SRC_ATOP);

        txtMusicNameConv.setText(musicName);
        txtInputFormat.setText(getMimeType(this, uri));


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

    public void onClickConvert(View view) throws URISyntaxException {


        String type = String.valueOf(spinnerType.getSelectedItem());


/**
 *  Update with a valid audio file!
 *  Supported formats: {@link AndroidAudioConverter.AudioFormat}
 */
        //android.net.Uri auri = new android.net.Uri(what ever);
        java.net.URI juri = new java.net.URI(uri.toString());
        File filetoConvert = new File(juri);
        Log.e("SAMIR", filetoConvert.getAbsolutePath());
        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                Toast.makeText(AudioConversionActivity.this, "SUCCESS: " + convertedFile.getPath(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(AudioConversionActivity.this, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        Toast.makeText(this, "Converting audio file...", Toast.LENGTH_SHORT).show();

        AudioFormat format = null;
        switch (type) {
            case "AAC":
                format = AudioFormat.AAC;
                break;
            case "MP3":
                format = AudioFormat.MP3;
                break;
            case "M4A":
                format = AudioFormat.M4A;
                break;
            case "WMA":
                format = AudioFormat.WMA;
                break;
            case "WAV":
                format = AudioFormat.WAV;
                break;
            case "FLAC":
                format = AudioFormat.FLAC;
                break;
            default:
                // code block
        }

        //Log.e("samir",testFile.getAbsolutePath());
        AndroidAudioConverter.with(this)
                .setFile(filetoConvert)
                .setFormat(format)
                .setCallback(callback)
                .convert();
    }

    public void onClickBack(View view) {
        Intent intent = new Intent(this, ListeningActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        intent.putExtra("song", uri);
        //mediaPlayer.release();
        startActivity(intent);
    }


}



