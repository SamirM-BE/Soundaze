package projet4.com.soundaze;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import projet4.com.soundaze.utils.VisualizerView;

public class EqualizerActivity extends AppCompatActivity
{

    private static final float VISUALIZER_HEIGHT_DIP = 50f ;

    private MediaPlayer myMediaPlayer;
    private Equalizer myEqualizer;
    private LinearLayout myLinearLayout;
    private Visualizer myVisualizer;
    private VisualizerView myVisualizerView;
    private Uri uri; //pour la récup de l'uri en intent
    private static String yourRealPath;//va être utilisée pour le nom de la musique

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        /**************LAUNCH ACTIVITY*****************/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);
        //on récupère l'intent pour le son qu'on sélectionné
        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        uri = intent.getParcelableExtra("tab");

        //on récupère le nom de a musique à partir de son uri

        if(uri!=null) {

            yourRealPath = getFileName(uri);
        }


        /************************************************************/
        /***********************PERMISSIONS**************************/
        /************************************************************/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            int hasAudioPermission = checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            int hasInternetPermission = checkSelfPermission(Manifest.permission.INTERNET);
            int hasRecordingPermission = checkSelfPermission(Manifest.permission.RECORD_AUDIO);

            List<String> permissions = new ArrayList<>();


            if (hasAudioPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            }

            if (hasRecordingPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }

            if (hasInternetPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.INTERNET);
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 200);
            }
        }
        /************************************************************/


        //Setting Volume Control to Audio Stream
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        /************Creating MediaPlayer Object*****************/
        //myMediaPlayer = MediaPlayer.create(this, R.raw.test_audio_file);
        myMediaPlayer = MediaPlayer.create(this, this.uri);
        myMediaPlayer.start();
        /**********Creating Equalizer Engine*****************/
        myEqualizer = new Equalizer(0, myMediaPlayer.getAudioSessionId());
        myEqualizer.setEnabled(true);

        setupVisualizerFXandUI();
        setupEqualizerFXandUI();

        // enable the visualizer
        myVisualizer.setEnabled(true);


        /**********Check when MediaPlayer is completed*****************/
        myMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer)
            {
                myVisualizer.setEnabled(false);
            }
        });

        /**********Check when MediaPlayer is prepared*****************/
       /* myMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer)
            {

                myMediaPlayer.setLooping(true);

            }
        });*/


    }

    private void setupVisualizerFXandUI()
    {
        myLinearLayout = findViewById(R.id.linearLayoutVisual);

        // Create a VisualizerView to display the audio waveform for the current settings
        myVisualizerView = new VisualizerView(this);
        myVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (VISUALIZER_HEIGHT_DIP * getResources().getDisplayMetrics().density)));
        myLinearLayout.addView(myVisualizerView);

        // Create the Visualizer object and attach it to our media player.

        //Todo: getAudioSessionId() BUG -> Put 0 instead

        //myVisualizer = new Visualizer(0);  //ERR0R!
        myVisualizer = new Visualizer(myMediaPlayer.getAudioSessionId());
        //myMediaPlayer.attachAuxEffect(myVisualizer.getId());
        myVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        myVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate) {
                myVisualizerView.updateVisualizer(bytes);
            }

            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    private void setupEqualizerFXandUI()
    {
        myLinearLayout = findViewById(R.id.linearLayoutEqual);

        //HEADING TITLE OF EQUALIZER
        TextView equalHeading = new TextView(this);
        equalHeading.setText(yourRealPath); //partie djaf on affiche le nom de la musique
        equalHeading.setTextSize(20);
        equalHeading.setTextColor(Color.parseColor("#3b31c4"));
        equalHeading.setGravity(Gravity.CENTER_HORIZONTAL);
        myLinearLayout.addView(equalHeading);

        //GET BANDS NUMBER
        short numberFreqBands = myEqualizer.getNumberOfBands();
        final short lowerEqualizerBandLevel = myEqualizer.getBandLevelRange()[0];
        final short upperEqualizerBandLevel = myEqualizer.getBandLevelRange()[1];

        for(short i = 0; i < numberFreqBands; i ++)
        {
            final short eqBandIndex = i;

            //HEADER HZ FOR EACH BAND
            TextView freqHeader = new TextView(this);
            freqHeader.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            freqHeader.setGravity(Gravity.CENTER_HORIZONTAL);

            //freqHeader.setText("Hz");
            freqHeader.setText((myEqualizer.getCenterFreq(eqBandIndex) / 1000) + "Hz");
            myLinearLayout.addView(freqHeader);

            //set up linear layout to contain each seekBar
            LinearLayout seekBarRowLayout = new LinearLayout(this);
            //Todo: CHANGED FROM HORIZONTAL TO VERTICAL
            seekBarRowLayout.setOrientation(LinearLayout.HORIZONTAL);

            ////set up lower level textview for this seekBar
            TextView lowerEqualizerBandLevelTextview = new TextView(this);
            lowerEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            lowerEqualizerBandLevelTextview.setText((lowerEqualizerBandLevel / 100) + " dB");

            ////set up upper level textview for this seekBar
            TextView upperEqualizerBandLevelTextview = new TextView(this);
            upperEqualizerBandLevelTextview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            upperEqualizerBandLevelTextview.setText((upperEqualizerBandLevel / 100) + " dB");

            //**********  the seekBar  **************
            //set the layout parameters for the seekbar
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;

            SeekBar seekBar = new SeekBar(this);

            seekBar.setId(i);
            seekBar.setLayoutParams(layoutParams);
            seekBar.setMax(upperEqualizerBandLevel - lowerEqualizerBandLevel);
//            set the progress for this seekBar
            seekBar.setProgress(myEqualizer.getBandLevel(eqBandIndex));

            //change progress as its changed by moving the sliders
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser)
                {
                    myEqualizer.setBandLevel(eqBandIndex,
                            (short) (progress + lowerEqualizerBandLevel));
                }

                public void onStartTrackingTouch(SeekBar seekBar)
                {
                    //not used
                }

                public void onStopTrackingTouch(SeekBar seekBar)
                {
                    //not used
                }
            });

            //add the lower and upper band level textviews and the seekBar to the row layout
            seekBarRowLayout.addView(lowerEqualizerBandLevelTextview);
            seekBarRowLayout.addView(seekBar);
            seekBarRowLayout.addView(upperEqualizerBandLevelTextview);

            myLinearLayout.addView(seekBarRowLayout);

            //show the spinner
            equalizeSound();
        }
    }

    private void equalizeSound()
    {
        //        set up the spinner
        ArrayList<String> equalizerPresetNames = new ArrayList<String>();
        ArrayAdapter<String> equalizerPresetSpinnerAdapter
                = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                equalizerPresetNames);
        equalizerPresetSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner equalizerPresetSpinner = findViewById(R.id.spinner);

//        get list of the device's equalizer presets
        for (short i = 0; i < myEqualizer.getNumberOfPresets(); i++) {
            equalizerPresetNames.add(myEqualizer.getPresetName(i));
        }

        equalizerPresetSpinner.setAdapter(equalizerPresetSpinnerAdapter);

//        handle the spinner item selections
        equalizerPresetSpinner.setOnItemSelectedListener(new AdapterView
                .OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                //first list item selected by default and sets the preset accordingly
                myEqualizer.usePreset((short) position);
//                get the number of frequency bands for this equalizer engine
                short numberFrequencyBands = myEqualizer.getNumberOfBands();
//                get the lower gain setting for this equalizer band
                final short lowerEqualizerBandLevel = myEqualizer.getBandLevelRange()[0];

//                set seekBar indicators according to selected preset
                for (short i = 0; i < numberFrequencyBands; i++) {
                    short equalizerBandIndex = i;
                    SeekBar seekBar = findViewById(equalizerBandIndex);
//                    get current gain setting for this equalizer band
//                    set the progress indicator of this seekBar to indicate the current gain value
                    seekBar.setProgress(myEqualizer
                            .getBandLevel(equalizerBandIndex) - lowerEqualizerBandLevel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
//                not used
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (isFinishing() && myMediaPlayer != null) {
            myVisualizer.release();
            myEqualizer.release();
            myMediaPlayer.release();
            myMediaPlayer = null;
        }
    }

    //on récupère le nom de la musique via son uri
    public String getFileName(Uri uri) {
        String result = null;
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

}
