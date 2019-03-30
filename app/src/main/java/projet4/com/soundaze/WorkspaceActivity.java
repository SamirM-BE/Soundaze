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


    /*******************Déclaration variables globales*************/


    SwipeMenuListView listView;
    private static String yourRealPath;//va être utilisée pour le nom de la musique
    //les 2 ici pour la séletion de musiques
    //les 2 ici pour le oncreate lors de l'arrivée sur le worksapce
    ArrayList<String> arrayListUri = new ArrayList<>(); // n°2 dans l'utilisation dans setItem(), contient les noms de musiques récupérer de l'arraylist des uri du fichier interne de arrayListUriVal
    ArrayList<String> arrayListUriVal = new ArrayList<>(); // n°1 dans l'utilisation dans onCreate, contient les uri du fichier interne en String
    //pour la listview
    ArrayAdapter arrayAdapter;
    //je mets le fichier interne en public pour pouvoir l'utiliser dans les autres activités qui ont besoin du syst de fichier
    public static String filename = "fileApp.txt";
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    //pour la sélection de fichier dans le load file
    String pickedAudioPath;
    private ArrayList<MediaFile> mediaFiles = new ArrayList<>();


    /*****************Fin déclaration variables globales*********************/



    /******************Quand l'activité Wroksapce se lance********************/



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

            //pas oublier d'ajouter ici le cas ou le type aurait supprimer le fichier
            //Toast.makeText(WorkspaceActivity.this,"file app has been removed", Toast.LENGTH_SHORT).show();
        }

        //Appel de méthode configurant le display de la Listview avec les musique déjà présente en interne
        setItem();
    }

    /*
    *@pré -
    * @post méthode qui vérifie les permissions pour accéder aux uri
    *
    */
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


    /*
     *@pré -
     * @post méthode pour set la listview et lire le fichier en interne
     *
     */

    public void setItem(){

        //on fait tout ceci seulement si le readinternal renvoit une arraylist remplie de musique
        //en gros, il faut que arrayListUriVal ne soit pas empty
        if(!(arrayListUriVal.isEmpty())) {
            for (int i = 0; i < arrayListUriVal.size(); i++) {

                // on stocke dans un uri temporaire l'uri en cours de lecture en le parsant de String vers Uri
                Uri tmp = Uri.parse(arrayListUriVal.get(i));
                arrayListUri.add(getFileName(tmp));

            }

            //on utilise mnt la SwipeMenulistview
            listView = findViewById(R.id.listView);
            //on utilise l'arrayListUri à afficher car ce sont les noms qui intéressent l'user
            arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, arrayListUri);
            listView.setAdapter(arrayAdapter);
            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // on crée l'elément "delete"
                    SwipeMenuItem deleteItem = new SwipeMenuItem(
                            getApplicationContext());
                    // on set le fond en rouge
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                            0x3F, 0x25)));
                    // on set la largeur de l'element
                    deleteItem.setWidth(170);
                    // on set l'icone de la poubelle
                    deleteItem.setIcon(R.drawable.ic_delete_swipe);
                    // on l'ajoue de menu du Swipe
                    menu.addMenuItem(deleteItem);
                }
            };

            listView.setMenuCreator(creator);

            //on gère le listener si l'user click sur la poubelle
            listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch (index) {
                        case 0:
                            Toast.makeText(WorkspaceActivity.this, "song deleted", Toast.LENGTH_SHORT).show();

                            //on doit supprimer de la Listview et du fichier interne l'élément sur lequel on a double click

                            arrayListUri.remove(position); //suppression du nom dans la listView qui est affiché
                            //supression de l'uri dans l'arraylist qui a lu lors de la création dans le fichier interne
                            arrayListUriVal.remove(position);
                            //on notifie un changement au niveau de la listView qui est affichée
                            arrayAdapter.notifyDataSetChanged();

                            Toast.makeText(WorkspaceActivity.this,"song deleted", Toast.LENGTH_SHORT).show();

                            //en 1, je delete mon file existant, le "fileApp" contenant tous les uri sélectionnés
                            deleteInternal();
                            //ensuite, je recrée mon file avec la nouvelle arraylist mise à jour
                            //je réenregistre un par un les uri qui sont encore d'actualité
                            for(int i = 0;i<arrayListUriVal.size(); i++){
                                save(arrayListUriVal.get(i)); // du pûre interne
                            }

                            //une fois que tout ceci est fait je peux refermer le SwipeMenu en sortant du case
                            //break;
                            return false;
                        case 1:

                            //je n'ai rien à faire dans ce cas car je n'ai qu'1 option dans mon menu

                            break;
                    }
                    // false : ferme le menu; true : ne ferme pas le menu
                    return false;
                }
            });

            //je gère le listener dans le cas ou l'user click sur une musique qu'il souhaite jouer avec le player
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

    /************************Fin du Lancement de l'activité Worksapce**********************/


    static final int AUDIO_SELECTED = 1; //Cette variable sert à vérifier si l'user a choisi un son
    Uri uri; //URI de l'audio séléctionné par l'user
    int alreadyLoaded = 0;


    /*
     *@pré -
     * @post méthode qui va lancer le menu de sélection de fichier audio à l'aide de la lib externe FilePicker
     *
     */
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

    /*
     *@pré -
     *@post on récupère le choix de l'user quant à la musique qu'il souhaite ajouter dans son Worksapce
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Vérifie à quelle requête nous sommes en train de répondre
        if (requestCode == AUDIO_SELECTED) {
            // On s'assure que la requête a été réussie
            if (resultCode == RESULT_OK) {
                mediaFiles.clear();
                mediaFiles.addAll(data.<MediaFile>getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES));

                MediaFile mediaFile = mediaFiles.get(0);
                pickedAudioPath = mediaFile.getPath();

                //on récupère l'uri du son sélectionné par l'user
                uri = Uri.fromFile(new File(pickedAudioPath));

                alreadyLoaded = 1; // à éliminer éventuellement

                //avant d'ajouter l'uri dans le fichier interne, on check s'il n'y est pas déjà
                if (!containsInternal(uri)) {
                    arrayListUriVal.add(uri.toString()); //à enlever si problème
                }


                //on récupère le nom de a musique à partir de son uri
                yourRealPath = getFileName(uri);


                //on rempli déjà l'arraylist visuelle afin que lorsque l'user choisit une musique
                //la musique vienne s'ajouter à une autre liste de musique déjà existante

                    for (int j = 0; j < arrayListUriVal.size(); j++) {
                        //arrayListUriVal.contains etc. et arrayListUri en deuxième lieu
                        if (!arrayListUri.contains(getFileName(Uri.parse(arrayListUriVal.get(j)))) && containsInternal(Uri.parse(arrayListUriVal.get(j)))) { // on triple check
                            arrayListUri.add(getFileName(Uri.parse(arrayListUriVal.get(j))));
                        }
                    }

                //on ajoute la suite d'uri, mais avant d'ajouter, on vérifie si l'elem est pas déjà dans l'arraylist de ceux récemment ajouté
                if (!(arrayListUri.contains(yourRealPath))) { //je peux utiliser arrayListUri utilisé en n°2
                    arrayListUri.add(yourRealPath); // on ajoute le son dans la liste
                }

                //on save l'uri seulement s'il n'est pas déjà dans le fichier, double check

                if (!containsInternal(uri)) {
                    save(uri.toString());
                }

                //on utilise mnt la swipemenulistview
                listView = findViewById(R.id.listView);

                //je pourrais mettre l'arrayListUri n°2 à la place de l'arrayList

                //avant arrayList

                arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, arrayListUri);


                listView.setAdapter(arrayAdapter);

                SwipeMenuCreator creator = new SwipeMenuCreator() {

                    @Override
                    public void create(SwipeMenu menu) {
                        // création de l'élément delete
                        SwipeMenuItem deleteItem = new SwipeMenuItem(
                                getApplicationContext());
                        // on set l'arrière plan en rouge
                        deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                                0x3F, 0x25)));
                        // on set la largeur de l'élément
                        deleteItem.setWidth(170);
                        // on set l'image de l'icone poubelle
                        deleteItem.setIcon(R.drawable.ic_delete_swipe);
                        // on l'ajoute au menu
                        menu.addMenuItem(deleteItem);
                    }
                };

                listView.setMenuCreator(creator);
                listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                        switch (index) {
                            case 0:

                                Toast.makeText(WorkspaceActivity.this, "clické sur delete", Toast.LENGTH_SHORT).show();

                                //on doit supprimer de l'arraylist et du fichier l'élément sur lequel on a double click

                                //on doit supprimer de l'arraylist et du fichier l'élément sur lequel on a double click

                                //je remove de mon arraylist de noms de musique
                                arrayListUri.remove(position);//à supprimer si prob
                                //je remove de mon arraylist de nom d'uri sous forme de String

                                if (!(arrayListUriVal.get(position) == null)) {
                                    arrayListUriVal.remove(position);//du pure interne
                                }

                                arrayAdapter.notifyDataSetChanged();

                                Toast.makeText(WorkspaceActivity.this, "song deleted", Toast.LENGTH_SHORT).show();

                                //restera pour demain qu'à supprimer dans le fichier

                                //en 1, je delete mon file existant
                                deleteInternal();
                                //ensuite, je recrée mon file avec la nouvelle arraylist mise à jour

                                for (int i = 0; i < arrayListUriVal.size(); i++) {
                                    save(arrayListUriVal.get(i)); // du pûre interne
                                }
                                //break;
                                return false;

                            case 1:
                                //je n'ai rien à faire dans ce cas car je n'ai qu'1 option dans mon menu
                                break;
                        }
                        // false : ferme le menu; true : ne ferme pas le menu
                        return false;
                    }
                });
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        //je récupère la musique sélectionnée et je la lance dans le lecteur
                        onClickMusic(uri);

                    }
                });


            }
        }
    }

    /*********************Partie sur les changement d'activité**************************/

    /*
     *@pré uri != null
     * @post lance l'intent du player de musique en lui transmettant l'uri du song qui doit être joué
     *
     */
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

    /*
     *@pré -
     * @post méthode de retour vers la mainActivity
     *
     */
    public void onBack(View view){

        Intent intent = new Intent(this, MainActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        startActivity(intent);
    }


    /******************************Fin partie sur les changements d'activité********************************/



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

    /****************************************Fin partie système de fichier****************************/

}