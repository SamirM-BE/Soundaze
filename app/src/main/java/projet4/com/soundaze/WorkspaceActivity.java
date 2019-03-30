package projet4.com.soundaze;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class WorkspaceActivity extends AppCompatActivity {

    SwipeMenuListView listView;
    private static String yourRealPath;//va être utilisée pour le nom de la musique
    //les 2 ici pour la séletion de musiques
    ArrayList<String>  arrayList = new ArrayList<>(); // pour contenir les noms des musiques
    ArrayList<String> arrayListVr = new ArrayList<>();
    //les 2 ici pour le oncreate
    ArrayList<String> arrayListUri = new ArrayList<>(); // pour contenir les uri sous forme de string
    ArrayList<String> arrayListUriVal = new ArrayList<>(); // pour contenir les uri sous forme de string
    int done = 0; // variable pour ne pas recréer une nouvelle arraylist pour chaque son sélectionné
    ArrayAdapter arrayAdapter;
    //je mets le filename en public pour pouvoir l'utiliser dans mon activity microphone
    public static String filename = "fileApp.txt"; // le nom de notre fichier intern qui va contenir les uri des musiques choisies par l'user
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    //ensembles de variables utilisées pour gérer le double click
    private boolean nonDoubleClick = true;
    private long firstClickTime = 0L;
    private final int DOUBLE_CLICK_TIMEOUT = 200;//ViewConfiguration.getDoubleTapTimeout();
    String pickedAudioPath;
    private ArrayList<MediaFile> mediaFiles = new ArrayList<>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);

        //on check les permission
        checkPermission();

        //on lit le contenu di fichier qu'on vient mettre dans l'arraylist

        try {
            arrayListUriVal = readInternal();
        }catch(FileNotFoundException e){
            //Toast.makeText(WorkspaceActivity.this,"file app has been removed", Toast.LENGTH_SHORT).show();
        }

        //va venir configurer le display avec les musique déjà présente en interne
        setItem();
    }


    //cette méthode check les permission pour les uri
    public void checkPermission(){

        //check if we have the permission

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_DOCUMENTS)!= PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(WorkspaceActivity.this, "permission is not granted", Toast.LENGTH_SHORT).show();
        }

        //request the permission
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.MANAGE_DOCUMENTS)
                != PackageManager.PERMISSION_GRANTED) {

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

    //méthode pour set la listview et lire le fichier en interne
    public void setItem(){

        //on fait tout ceci seulement si le readinternal renvoit une arraylist remplie de musique

        if(!(arrayListUriVal.isEmpty())) {

            for (int i = 0; i < arrayListUriVal.size(); i++) {

                // create a Uri for the content provider suggested by the error message
                Uri tmp = Uri.parse(arrayListUriVal.get(i));

                String ff = getFileName(tmp);

                arrayListUri.add(ff);

            }


            //listView = findViewById(R.id.listView);

            //on utilise mnt la swipemenulistview
            listView = findViewById(R.id.listView);

            arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, arrayListUri);


            listView.setAdapter(arrayAdapter);

            /******************nouvelle partie test swipeListView***********************/


            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // create "open" item
                    SwipeMenuItem openItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    openItem.setBackground(new ColorDrawable(Color.rgb(0x00, 0x66,
                            0xff)));
                    // set item width
                    openItem.setWidth(170);
                    // set item title
                    openItem.setTitle("equalizer");
                    // set item title fontsize
                    openItem.setTitleSize(18);
                    // set item title font color
                    openItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem);

                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                            0x3F, 0x25)));
                    // set item width
                    deleteItem.setWidth(170);
                    // set a icon
                    deleteItem.setIcon(R.drawable.ic_delete_swipe);
                    // add to menu
                    menu.addMenuItem(deleteItem);
                }
            };

            listView.setMenuCreator(creator);

            listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch (index) {
                        case 0:
                            Toast.makeText(WorkspaceActivity.this, "clické sur equalizer", Toast.LENGTH_SHORT).show();

                            //on passe dans l'activité de l'qualizer
                            //je récupère la musique sélectionnée et je la lance dans le lecteur
                            final Uri vr = Uri.parse(arrayListUriVal.get(position));
                            onEqual(vr);
                            break;
                        case 1:
                            Toast.makeText(WorkspaceActivity.this, "clické sur delete", Toast.LENGTH_SHORT).show();

                            //on doit supprimer de l'arraylist et du fichier l'élément sur lequel on a double click

                            arrayListUri.remove(position);

                            arrayListUriVal.remove(position);//en interne
                            arrayAdapter.notifyDataSetChanged();


                            Toast.makeText(WorkspaceActivity.this,"song deleted", Toast.LENGTH_SHORT).show();

                            //restera pour demain qu'à supprimer dans le fichier

                            //en 1, je delete mon file existant
                            deleteInternal();
                            //ensuite, je recrée mon file avec la nouvelle arraylist mise à jour
                            for(int i = 0;i<arrayListUriVal.size(); i++){
                                save(arrayListUriVal.get(i)); // du pûre interne
                            }

                            break;
                    }
                    // false : close the menu; true : not close the menu
                    return false;
                }
            });






            /*****************fin nouvelle partie test swipeListview*********************/


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    //je récupère la musique sélectionnée et je la lance dans le lecteur
                    final Uri vr = Uri.parse(arrayListUriVal.get(position));
                    onClickMusic(vr);
                }
            });


        }

    }



    //MediaPlayer mMediaPlayer = new MediaPlayer();
    static final int AUDIO_SELECTED = 1; //Cette variable sert à vérifier si l'user a choisi un son
    Uri uri; //URI de l'audio séléctionné par l'user
    int alreadyLoaded = 0;

    public void onClickLoadFile(View view)
    {
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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == AUDIO_SELECTED) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mediaFiles.clear();
                mediaFiles.addAll(data.<MediaFile>getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES));

                MediaFile mediaFile = mediaFiles.get(0);
                pickedAudioPath = mediaFile.getPath();

                uri = Uri.fromFile(new File(pickedAudioPath));

                // The user picked an audio
                //uri = data.getData(); // The Intent's data Uri identifies which audio was selected. (audio = URI)

                alreadyLoaded = 1;

                //on ajoute l'élément dans mon arraylist contenant les uri sous forme de string, seulement si l'uri n'y est pas déjà
                //on s'assure de ne pas avoir de doublon dans notre fichier, double check pour le containsInternal

                if (!containsInternal(uri)) {
                    arrayListVr.add(uri.toString()); //juste pour l'interne
                }


                //on récupère le nom de a musique à partir de son uri

                yourRealPath = getFileName(uri);

                //partie pour gérer la listeView

                //on rempli déjà l'arraylist visuelle afin que lorsque l'user choisit une musique
                //la musique vienne s'ajouter à une autre liste de musique déjà existante


                try {
                    ArrayList<String> gg = readInternal();
                    for (int j = 0; j < gg.size(); j++) {
                        if (!arrayList.contains(getFileName(Uri.parse(gg.get(j)))) && containsInternal(Uri.parse(gg.get(j)))) { // on triple check
                            arrayList.add(getFileName(Uri.parse(gg.get(j))));
                        }
                    }
                } catch (FileNotFoundException e) {
                    //Toast.makeText(WorkspaceActivity.this,"file app has been removed", Toast.LENGTH_SHORT).show();
                }


                //on ajoute la suite d'uri, mais avant d'ajouter, on vérifie si l'elem est pas déjà dans l'arraylist de ceux récemment ajouté
                if (!(arrayList.contains(yourRealPath))) {
                    arrayList.add(yourRealPath); // on ajoute le son dans la liste
                }

                //on save l'uri seulement s'il n'est pas déjà dans le fichier, double check

                if (!containsInternal(uri)) {
                    save(uri.toString());
                }

                /******************intermed***************/

                //on utilise mnt la swipemenulistview
                listView = findViewById(R.id.listView);

                arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, arrayList);


                listView.setAdapter(arrayAdapter);

                SwipeMenuCreator creator = new SwipeMenuCreator() {

                    @Override
                    public void create(SwipeMenu menu) {
                        // create "open" item
                        SwipeMenuItem openItem = new SwipeMenuItem(
                                getApplicationContext());
                        // set item background
                        openItem.setBackground(new ColorDrawable(Color.rgb(0x00, 0x66,
                                0xff)));
                        // set item width
                        openItem.setWidth(170);
                        // set item title
                        openItem.setTitle("equalizer");
                        // set item title fontsize
                        openItem.setTitleSize(18);
                        // set item title font color
                        openItem.setTitleColor(Color.WHITE);
                        // add to menu
                        menu.addMenuItem(openItem);

                        // create "delete" item
                        SwipeMenuItem deleteItem = new SwipeMenuItem(
                                getApplicationContext());
                        // set item background
                        deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                                0x3F, 0x25)));
                        // set item width
                        deleteItem.setWidth(170);
                        // set a icon
                        deleteItem.setIcon(R.drawable.ic_delete_swipe);
                        // add to menu
                        menu.addMenuItem(deleteItem);
                    }
                };

                listView.setMenuCreator(creator);

                listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                        switch (index) {
                            case 0:
                                Toast.makeText(WorkspaceActivity.this, "clické sur equalizer", Toast.LENGTH_SHORT).show();

                                //on passe dans l'activité de l'qualizer
                                onEqual(uri);
                                break;
                            case 1:
                                Toast.makeText(WorkspaceActivity.this, "clické sur delete", Toast.LENGTH_SHORT).show();

                                //on doit supprimer de l'arraylist et du fichier l'élément sur lequel on a double click

                                //on doit supprimer de l'arraylist et du fichier l'élément sur lequel on a double click

                                //je remove de mon arraylist de noms de musique
                                arrayList.remove(position); // du pure visuel
                                //je remove de mon arraylist de nom d'uri sous forme de String
                                if (!(arrayListVr.get(position) == null)) {
                                    arrayListVr.remove(position);//du pure interne
                                }

                                arrayAdapter.notifyDataSetChanged();


                                Toast.makeText(WorkspaceActivity.this, "song deleted", Toast.LENGTH_SHORT).show();

                                //restera pour demain qu'à supprimer dans le fichier

                                //en 1, je delete mon file existant
                                deleteInternal();
                                //ensuite, je recrée mon file avec la nouvelle arraylist mise à jour
                                for (int i = 0; i < arrayListVr.size(); i++) {
                                    save(arrayListVr.get(i)); // du pûre interne
                                }

                                break;
                        }
                        // false : close the menu; true : not close the menu
                        return false;
                    }
                });


                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        onClickMusic(uri);

                    }
                });


            }


            /****************fin intermed**************/


            //on gère la listview


        }
    }


    //Clique sur une musiqu pour l'écouter
    public void onClickMusic(Uri uri)
    {
        Intent intent = new Intent(this, ListeningActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        // Add a Uri instance to an Intent
        intent.putExtra("song", uri);
        if (containsInternal(uri)) {
            pickedAudioPath = uri.getPath();
        }
        intent.putExtra("pickedAudioPath", pickedAudioPath);
        startActivity(intent);


    }

    //click sur le bouton back
    public void onBack(View view){

        Intent intent = new Intent(this, MainActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        startActivity(intent);
    }

    //clique sur le bouton equalizer, TEMPORAIRE
    public void onEqual(Uri uri){

        Intent intent = new Intent(this, EqualizerActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        intent.putExtra("tab", uri);
        startActivity(intent);
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

    //méthode pour écrire dans un fichier interne au téléphone l'arraylist d'uri
    //on écrit un uri par ligne

    public void writeInternal(ArrayList<String> uri){
        try{
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(fos));
            //on parcours l'arraylist
            int N = uri.size();
            for(int i= 0; i < N; i++){
                writer. println(uri.get(i)); //on écrit l'uri sous forme de String
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
            FileInputStream fis = openFileInput(filename);
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

    //méthode pour supprimer le fichier filename qu'on a crée, on ne va pas l'utiliser mais pour mes test je vais le faire pour ne pas
    //surcharger la mémoire de mon téléphone
    public void deleteInternal(){
        //le contexte va localiser et supprimer le fichier en mémoire interne
        this.deleteFile(filename);



    }

    //on fait une méthode contains pour voir si un son souhaitant être ajouté par l'user n'est pas déjà dans le fichier interne
    public boolean containsInternal(Uri uri) {
        try {
            FileInputStream fis = openFileInput(filename);
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

    //return true si il contien et false sinon
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

            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);
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

}