package com.example.musicplayer;

import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class MediaCursorAdapter extends CursorAdapter {
    private Context context;
    private LayoutInflater layoutInflater;

    public MediaCursorAdapter(Context context) {
        super(context,null,0);
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = layoutInflater.inflate(R.layout.list_item,viewGroup,false);

        if(view != null){
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = view.findViewById(R.id.title1);
            viewHolder.artist = view.findViewById(R.id.artist);
            viewHolder.order = view.findViewById(R.id.order);
            viewHolder.divider = view.findViewById(R.id.divider);

            view.setTag(viewHolder);

            return  view;
        }

        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

        String title = cursor.getString(titleIndex);
        String artist = cursor.getString(artistIndex);

        int position = 0;
        position = cursor.getPosition();

        if (viewHolder!=null){
            viewHolder.title.setText(title);
            viewHolder.artist.setText(artist);
            viewHolder.order.setText(Integer.toString(position+1));
        }
    }

    public class ViewHolder{
        TextView title;
        TextView artist;
        TextView order;
        View divider;
    }
}
