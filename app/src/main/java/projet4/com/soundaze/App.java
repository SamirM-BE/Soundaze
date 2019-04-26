package projet4.com.soundaze;

import android.app.Application;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidAudioConverter.load(this, new ILoadCallback() {
            @Override
            public void onSuccess() {
                // Great!
            }

            @Override
            public void onFailure(Exception error) {
                // FFmpeg is not supported by device
            }
        });
    }
}