package projet4.com.soundaze;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jaiselrahman.filepicker.activity.FilePickerActivity;
import com.jaiselrahman.filepicker.config.Configurations;
import com.jaiselrahman.filepicker.model.MediaFile;

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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import projet4.com.soundaze.ListView.Music;
import projet4.com.soundaze.ListView.MusicsDataAdapter;
import projet4.com.soundaze.ListView.SwipeController;
import projet4.com.soundaze.ListView.SwipeControllerActions;


public class WorkspaceActivity extends AppCompatActivity {

    static final int AUDIO_SELECTED = 1; //Cette variable sert à vérifier si l'user a choisi un son
    //je mets le fichier interne en public pour pouvoir l'utiliser dans les autres activités qui ont besoin du syst de fichier
    public static String filename = "fileApp.txt";
    /*******************Déclaration variables globales*************/
    private static String yourRealPath;//va être utilisée pour le nom de la musique
    ArrayList<String> displayedAudio = new ArrayList<>(); // Musiques sensé être dans la ListView
    ArrayList<String> savedAudios = new ArrayList<>(); // Musiques sauvegardés dans la mémoire interne
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    //pour la sélection de fichier dans le load file
    String pickedAudioPath;
    private MusicsDataAdapter mAdapter;
    SwipeController swipeController = null; //Pour gérer les swipe
    List<Music> musics; //Liste des musiques
    Uri uri; //URI de l'audio séléctionné par l'user
    int alreadyLoaded = 1;
    private ArrayList<MediaFile> mediaFiles = new ArrayList<>();
    ProgressDialog dialog;

    /******************Quand l'activité Wroksapce se lance********************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);

        //on check les permission
        checkPermission();

        //on lit le contenu di fichier qu'on vient mettre dans l'arraylist
        try {
            savedAudios = readInternal();
        } catch (FileNotFoundException e) {


            ///TODO : pas oublier d'ajouter ici le cas ou le type aurait supprimer le fichier
            //Toast.makeText(WorkspaceActivity.this,"file app has been removed", Toast.LENGTH_SHORT).show();
        }
        //Appel de méthode configurant le display de la Listview avec les musique déjà présente en interne
        if (savedAudios.size() != 0) {
            setItem(1);
            setupRecyclerView();

        }

    }

    /*
     *@pré -
     * @post méthode qui vérifie les permissions pour accéder aux uri
     *
     */
    public void checkPermission() {

        //request the permission
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_DOCUMENTS) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.MANAGE_DOCUMENTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.MANAGE_DOCUMENTS},
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


    /*
     *@pré -
     * @post méthode pour set la listview et lire le fichier en interne
     *
     */

    public void setItem(int firstTime) {

        //on fait tout ceci seulement si le readinternal renvoit une arraylist remplie de musique
        //en gros, il faut que savedAudios ne soit pas empty
        if (!(savedAudios.isEmpty())) {
            for (int i = 0; i < savedAudios.size(); i++) {

                // on stocke dans un uri temporaire l'uri en cours de lecture en le parsant de String vers Uri
                Uri tmp = Uri.parse(savedAudios.get(i));

                if (firstTime == 1) {
                    displayedAudio.add(getFileName(tmp));
                }
            }
            int i = 0;
            Uri tmp2;
            musics = new ArrayList<>(); //Liste des musiques
            Iterator<String> iterator = displayedAudio.iterator();
            while (iterator.hasNext()) {
                tmp2 = Uri.parse(savedAudios.get(i));
                Music music = new Music();
                music.setMusicName(iterator.next());
                music.setDuration(getMusicDuration(tmp2));
                musics.add(music);
            }
            mAdapter = new MusicsDataAdapter(musics);
        }
    }

    @SuppressLint("WrongConstant")
    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                mAdapter.musics.remove(position);
                mAdapter.notifyItemRemoved(position);
                mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());
                displayedAudio.remove(position); //suppression du nom dans la listView qui est affiché
                //supression de l'uri dans l'arraylist qui a lu lors de la création dans le fichier interne
                savedAudios.remove(position);
                deleteInternal();
                //ensuite, je recrée mon file avec la nouvelle arraylist mise à jour
                //je réenregistre un par un les uri qui sont encore d'actualité
                for (int i = 0; i < savedAudios.size(); i++) {
                    save(savedAudios.get(i)); // du pûre interne
                }

                Toast.makeText(WorkspaceActivity.this, "song deleted", Toast.LENGTH_SHORT).show();
            }
        });
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });
    }

    public void onClickMusicName(View view) {
        int position = (int) view.getTag();
        Toast.makeText(view.getContext(), Integer.toString(position), Toast.LENGTH_SHORT).show();
        final Uri clickedMusicUri = Uri.parse(savedAudios.get(position));
        Intent intent = new Intent(this, ListeningActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        // Add a Uri instance to an Intent
        intent.putExtra("song", clickedMusicUri);
        if (containsInternal(uri)) {
            pickedAudioPath = uri.getPath();

        }
        intent.putExtra("pickedAudioPath", pickedAudioPath);
        startActivity(intent);

    }

    /*
     *@pré -
     * @post méthode qui va lancer le menu de sélection de fichier audio à l'aide de la lib externe FilePicker
     *
     */
    public void onClickLoadFile(View view) {
        Intent intent = new Intent(WorkspaceActivity.this, FilePickerActivity.class);
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
        dialog = ProgressDialog.show(WorkspaceActivity.this, "",
                "Loading. Please wait...", true);

    }

    /*
     *@pré -
     *@post on récupère le choix de l'user quant à la musique qu'il souhaite ajouter dans son Worksapce
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Vérifie à quelle requête nous sommes en train de répondre
        if (requestCode == AUDIO_SELECTED) {
            dialog.dismiss();
            // On s'assure que la requête a été réussie
            if (resultCode == RESULT_OK) {
                mediaFiles.clear();
                mediaFiles.addAll(data.<MediaFile>getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES));

                if (!(mediaFiles.isEmpty())) {
                    Log.e("SAMIR", "audio pas choisi0");


                    MediaFile mediaFile = mediaFiles.get(0);

                    pickedAudioPath = mediaFile.getPath();

                    //on récupère l'uri du son sélectionné par l'user
                    uri = Uri.fromFile(new File(pickedAudioPath));


                    //avant d'ajouter l'uri dans le fichier interne, on check s'il n'y est pas déjà
                    if (!containsInternal(uri)) {
                        savedAudios.add(uri.toString()); //à enlever si problème

                    }

                    //on récupère le nom de a musique à partir de son uri
                    yourRealPath = getFileName(uri);


                    //on ajoute la suite d'uri, mais avant d'ajouter, on vérifie si l'elem est pas déjà dans l'arraylist de ceux récemment ajouté
                    if (!(displayedAudio.contains(yourRealPath))) { //je peux utiliser displayedAudio utilisé en n°2
                        displayedAudio.add(yourRealPath); // on ajoute le son dans la liste
                        alreadyLoaded = 0;

                    }

                    //on save l'uri seulement s'il n'est pas déjà dans le fichier, double check

                    if (!containsInternal(uri)) {
                        save(uri.toString());
                    }


                    if (musics != null && alreadyLoaded == 0) {
                        Music music = new Music();
                        music.setMusicName(getFileName(uri));
                        music.setDuration(getMusicDuration(uri));
                        musics.add(music);
                        mAdapter.updateList(musics);
                        alreadyLoaded = 1;
                    } else if (musics == null) {

                        setItem(0);
                        setupRecyclerView();
                    }
                }
            } else {
                Log.e("SAMIR", "audio pas choisi");
            }
        } else {
            Log.e("SAMIR", "audio pas choisi");
        }

    }

    public String getMusicDuration(Uri uri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this, uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);
        durationStr = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millSecond) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millSecond)),
                TimeUnit.MILLISECONDS.toSeconds(millSecond) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millSecond)));
        return durationStr;
    }

    /*********************Partie sur les changement d'activité**************************/


    ///TODO: gérer stack
    /*
     *@pré -
     * @post méthode de retour vers la mainActivity
     *
     */
    public void onBack(View view) {

        Intent intent = new Intent(this, MainActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        startActivity(intent);
    }


    /******************************Partie système de fichier*************************************************/


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


    //méthode pour lire dans un fichier interne au téléphone l'arraylist d'uri
    //lance une file not foud exeption si le fichier aurait été supprimé


    public ArrayList<String> readInternal() throws FileNotFoundException {
        //on crée l'arraylist destinée à contenir les uri
        ArrayList<String> uri = new ArrayList<>();
        try {
            FileInputStream fis = openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                //on ajoute l'uri dans l'arraylist, attention de le reconvertir en uri valide
                uri.add(line);
            }
            reader.close();
            return uri;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new FileNotFoundException();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;


    }

    //méthode pour supprimer le fichier filename qu'on a crée, on ne va pas l'utiliser mais pour mes test je vais le faire pour ne pas
    //surcharger la mémoire de mon téléphone
    public void deleteInternal() {
        //le contexte va localiser et supprimer le fichier en mémoire interne
        this.deleteFile(filename);


    }

    //on fait une méthode contains pour voir si un son souhaitant être ajouté par l'user n'est pas déjà dans le fichier interne
    public boolean containsInternal(Uri uri) {
        try {
            FileInputStream fis = openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {

                if (Uri.parse(line).equals(uri)) {
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    //return true si il contien et false sinon
    //pour sauvegarder lign par ligne les uri dans le fichier

    public void save(String string) {

        //je fais un readline pour récupérer la liste déjà existante
        ArrayList<String> ch = new ArrayList<>();

        try {
            ch = readInternal();
        } catch (FileNotFoundException e) {
            //Toast.makeText(WorkspaceActivity.this,"file app has been removed", Toast.LENGTH_SHORT).show();
        }

        //on ajoute le nouveau string à ajouter à ceux déjà existant
        ch.add(string);

        try {

            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos), true);
            //on écrit l'arraylist dans le fichier

            for (int i = 0; i < ch.size(); i++) {

                writer.println(ch.get(i));

            }
            //on ferme le writer
            writer.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /****************************************Fin partie système de fichier****************************/


}