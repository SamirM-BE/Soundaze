package projet4.com.soundaze;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;


public class WorkspaceActivity extends AppCompatActivity {

    ListView listView;
    private static String yourRealPath;//va être utilisée pour le nom de la musique
    //les 2 ici pour la séletion de musiques
    ArrayList<String>  arrayList = new ArrayList<>(); // pour contenir les noms des musiques
    ArrayList<String> arrayListVr = new ArrayList<>();
    //les 2 ici pour le oncreate
    ArrayList<String> arrayListUri = new ArrayList<>(); // pour contenir les uri sous forme de string
    ArrayList<String> arrayListUriVal = new ArrayList<>(); // pour contenir les uri sous forme de string
    int done = 0; // variable pour ne pas recréer une nouvelle arraylist pour chaque son sélectionné
    ArrayAdapter arrayAdapter;
    String filename = "fileApp.txt"; // le nom de notre fichier intern qui va contenir les uri des musiques choisies par l'user
    int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    //ensembles de variables utilisées pour gérer le double click
    private boolean nonDoubleClick = true;
    private long firstClickTime = 0L;
    private final int DOUBLE_CLICK_TIMEOUT = 200;//ViewConfiguration.getDoubleTapTimeout();






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);






        Intent intent = new Intent(); //Un intent de type ACTION_GET_CONTENT permet à l'utilisateur de sélectionner des fichiers
        intent.setType("audio/*"); //On veut des fichiers audio
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

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


        try {
            arrayListUriVal = readInternal();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //on fait tout ceci seulement si le readinternal renvoit une arraylist remplie de musique

        if(!(arrayListUriVal.isEmpty())) {

            for (int i = 0; i < arrayListUriVal.size(); i++) {

                // create a Uri for the content provider suggested by the error message
                Uri tmp = Uri.parse(arrayListUriVal.get(i));

                String ff = getFileName(tmp);

                arrayListUri.add(ff);

            }


            listView = (ListView) findViewById(R.id.listView);

            arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, arrayListUri);


            listView.setAdapter(arrayAdapter);




            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    // @TODO check and catch the double click event
                    synchronized(this) {
                        if(firstClickTime == 0) {
                            firstClickTime = SystemClock.elapsedRealtime();
                            nonDoubleClick = true;
                        } else {
                            long deltaTime = SystemClock.elapsedRealtime() - firstClickTime;
                            firstClickTime = 0;
                            if(deltaTime < DOUBLE_CLICK_TIMEOUT) {
                                nonDoubleClick = false;
                                this.onItemDoubleClick(adapterView, view, i, l);
                                return;
                            }
                        }

                        final Uri vr = Uri.parse(arrayListUriVal.get(i));

                        view.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if(nonDoubleClick) {
                                    //Uri vr = Uri.parse(arrayListUriVal.get(i));

                                    //on appelle la méthode qui part sur l'activité du player avec un intent

                                    onClickMusic(vr);
                                }
                            }

                        }, DOUBLE_CLICK_TIMEOUT);
                    }


                }

                //méthode pour check le double click
                public void onItemDoubleClick(AdapterView<?> adapterView, View view, int position, long l) {
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

                    //restera pour demain qu'à supprimer dans le fichier
                }
            });

        }

    }




    //MediaPlayer mMediaPlayer = new MediaPlayer();
    static final int AUDIO_SELECTED = 1; //Cette variable sert à vérifier si l'user a choisi un son
    Uri uri; //URI de l'audio séléctionné par l'user
    int alreadyLoaded = 0;
    Intent intent;


    public void onClickLoadFile(View view)
    {
        //if(alreadyLoaded==1) mMediaPlayer.reset(); //on reset le player si un fichier a déjà été chargé, évite la superposition de sons différents
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); //Un intent de type ACTION_GET_CONTENT permet à l'utilisateur de sélectionner des fichiers
        intent.setType("audio/*"); //On veut des fichiers audio
        intent.setFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE); //On veut des fichiers qui sont ouvrable

        final int takeFlags = intent.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);



        Intent finalIntent = Intent.createChooser(intent, "Choisissez un fichier audio"); //Normalement la nouvelle fenetre doit s'appeler "Choisissez .. " mais je vois aucune diff

        startActivityForResult(finalIntent, AUDIO_SELECTED); //ForResult = quand on attend un résultat sinon juste StartActivity()

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == AUDIO_SELECTED) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked an audio
                uri = data.getData(); // The Intent's data Uri identifies which audio was selected. (audio = URI)





                alreadyLoaded = 1;

                //on ajoute l'élément dans mon arraylist contenant les uri sous forme de string, seulement si l'uri n'y est pas déjà
                //on s'assure de ne pas avoir de doublon dans notre fichier, double check pour le containsInternal
                try {
                    if(!containsInternal(uri)) {
                        arrayListVr.add(uri.toString()); //juste pour l'interne
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //on récupère le nom de a musique à partir de son uri

                yourRealPath = getFileName(uri);

                //partie pour gérer la listeView

                //on rempli déjà l'arraylist visuelle afin que lorsque l'user choisit une musique
                //la musique vienne s'ajouter à une autre liste de musique déjà existante

                try {
                    ArrayList<String> gg = readInternal();
                    for(int j = 0; j<gg.size();j++){
                        if(!containsInternal(Uri.parse(gg.get(j)))) { // on triple check
                            arrayList.add(getFileName(Uri.parse(gg.get(j))));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //on ajoute la suite d'uri, mais avant d'ajouter, on vérifie si l'elem est pas déjà dans l'arraylist de ceux récemment ajouté
                if(!(arrayList.contains(yourRealPath))) {
                    arrayList.add(yourRealPath); // on ajoute le son dans la liste

                }

                //on save l'uri seulement s'il n'est pas déjà dans le fichier, double check
                try {
                    if(!containsInternal(uri)){

                        save(uri.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //on gère la listview

                listView = (ListView) findViewById(R.id.listView);





                //Toast.makeText(WorkspaceActivity.this,uri.toString(), Toast.LENGTH_SHORT).show();

                arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, arrayList);

                listView.setAdapter(arrayAdapter);


                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                        // @TODO check and catch the double click event
                        synchronized(this) {
                            if(firstClickTime == 0) {
                                firstClickTime = SystemClock.elapsedRealtime();
                                nonDoubleClick = true;
                            } else {
                                long deltaTime = SystemClock.elapsedRealtime() - firstClickTime;
                                firstClickTime = 0;
                                if(deltaTime < DOUBLE_CLICK_TIMEOUT) {
                                    nonDoubleClick = false;
                                    this.onItemDoubleClick(adapterView, view, i, l);
                                    return;
                                }
                            }

                            view.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    if(nonDoubleClick) {
                                        onClickMusic(uri);
                                    }
                                }

                            }, DOUBLE_CLICK_TIMEOUT);
                        }



                    }

                    //méthode pour check le double click
                    public void onItemDoubleClick(AdapterView<?> adapterView, View view, int position, long l) {
                        //on doit supprimer de l'arraylist et du fichier l'élément sur lequel on a double click

                        //je remove de mon arraylist de noms de musique
                        arrayList.remove(position); // du pure visuel
                        //je remove de mon arraylist de nom d'uri sous forme de String
                        arrayListVr.remove(position);//du pure interne

                        arrayAdapter.notifyDataSetChanged();


                        Toast.makeText(WorkspaceActivity.this,"song deleted", Toast.LENGTH_SHORT).show();

                        //restera pour demain qu'à supprimer dans le fichier

                        //en 1, je delete mon file existant
                        deleteInternal();
                        //ensuite, je recrée mon file avec la nouvelle arraylist mise à jour
                        for(int i = 0;i<arrayListVr.size(); i++){
                            save(arrayListVr.get(i)); // du pûre interne
                        }
                    }

                });

            }
        }
    }

    //Clique sur une musiqu pour l'écouter
    public void onClickMusic(Uri uri)
    {

        Intent intent = new Intent(this, ListeningActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        // Add a Uri instance to an Intent
        intent.putExtra("song", uri);
        startActivity(intent);


    }

    //s'il click sur l'option pour le recorder audio
    protected void onRecord(View view) {

        Toast.makeText(WorkspaceActivity.this,"inch", Toast.LENGTH_SHORT).show();


        Intent intent = new Intent(this, MicrophoneActivity.class); //On prépare l'intent pour le passage à l'écran suivant
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


    public ArrayList<String> readInternal() throws IOException {
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
    //return true si il contien et false sinon
    public boolean containsInternal(Uri uri) throws IOException {
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

    //pour sauvegarder lign par ligne les uri dans le fichier

    public void save(String string) {

        //je fais un readline pour récupérer la liste déjà existante
        ArrayList<String> ch = new ArrayList<>();


        try {
            ch = readInternal();
        } catch (IOException e) {
            e.printStackTrace();
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
