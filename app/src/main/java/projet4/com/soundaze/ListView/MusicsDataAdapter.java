package projet4.com.soundaze.ListView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import projet4.com.soundaze.R;
import projet4.com.soundaze.WorkspaceActivity;

public class MusicsDataAdapter extends RecyclerView.Adapter<MusicsDataAdapter.MusicViewHolder> { // implements View.OnClickListener
    public List<Music> musics;

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        private TextView musicName;//, duration;

        public MusicViewHolder(View view) {
            super(view);
            musicName = (TextView) view.findViewById(R.id.musicName);
            //musicName.setOnClickListener();
            //duration = (TextView) view.findViewById(R.id.duration);
        }
    }

    /*@Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        Toast.makeText(view.getContext(),Integer.toString(position),Toast.LENGTH_SHORT).show();
    }*/
    public MusicsDataAdapter(List<Music> musics) {
        this.musics = musics;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_row, parent, false);

        return new MusicViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        Music music = musics.get(position);
        holder.musicName.setText(music.getMusicName());
        holder.musicName.setTag(position);
        //holder.duration.setText(music.getDuration());
    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    public void updateList(List<Music> data) {
        musics = data;
        notifyDataSetChanged();
    }
}