package projet4.com.soundaze;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.concurrent.TimeUnit;

public class LaunchActivity extends Activity {

    RelativeLayout rellay1, rellay2;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            rellay1.setVisibility(View.VISIBLE);
            rellay2.setVisibility(View.VISIBLE);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        rellay1 = (RelativeLayout) findViewById(R.id.rellay1);
        rellay2 = (RelativeLayout) findViewById(R.id.rellay2);

        handler.postDelayed(runnable, 2000); //2000 est le temps pour le splash


    }

    //s'il clique sur le bouton getStarted on passe dans la mainActivity

    public void onClickGetStarted(View view){
        Intent intent = new Intent(this, MainActivity.class); //On prépare l'intent pour le passage à l'écran suivant
        startActivity(intent);
    }


}
