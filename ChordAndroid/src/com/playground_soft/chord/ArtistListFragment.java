package com.playground_soft.chord;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.app.SherlockListFragment;

import com.playground_soft.chord.dialog.AboutDialog;
import com.playground_soft.chord.type.Artist;

public class ArtistListFragment
        extends SherlockListFragment 
        implements RefreshThread.OnFinishHandler {
    private ArrayAdapter<Artist> mAdapter;
    private DatabaseHelper mDbHelper;
    private SongListFragment mSonglistFragment;

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String artistName;
        Artist artist = mAdapter.getItem(position);
        if (artist.getId() == -1) {
            artistName = null;
        } else {
            artistName = artist.getName();
        }

        if (mSonglistFragment != null) {
            mSonglistFragment.updateSongList(artistName);
        } else {
            Intent intent = new Intent(this.getActivity(),
                    SongListActivity.class);

            intent.putExtra("artist", artistName);
            this.startActivity(intent);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView lv = this.getListView();
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mSonglistFragment = (SongListFragment) this.getFragmentManager()
                .findFragmentById(R.id.song_list_fragment);
        updateArtistList();
        lv.setItemChecked(0, true);
    }

    private void updateArtistList() {
        int layout = android.R.layout.simple_list_item_1;
        if (mSonglistFragment != null) {
            layout = android.R.layout.simple_list_item_single_choice;
        }
        mAdapter = new ArrayAdapter<Artist>(this.getActivity(), layout);

        mAdapter.add(new Artist("All", -1));

        mDbHelper = new DatabaseHelper(getActivity());

        for (Artist artist : mDbHelper.getArtistList()) {
            mAdapter.add(artist);
        }
        setListAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_item_synchronize) {
            RefreshThread thread = new RefreshThread(this.getActivity(), this);
            thread.start();

        } else if (itemId == R.id.menu_item_about) {
            AboutDialog dialog = new AboutDialog();
            dialog.show(this.getFragmentManager(), "");
        } else if(itemId == R.id.menu_item_help){
            Intent intent = new Intent(this.getActivity(), HelpActivity.class);
            this.startActivity(intent);
            
        } else {
            return super.onOptionsItemSelected(item);
        }
        
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onFinished() {
        updateArtistList();
        SongListFragment songlistFragment = (SongListFragment) this
                .getFragmentManager().findFragmentById(R.id.song_list_fragment);

        if (songlistFragment != null) {
            songlistFragment.updateSongList(null);
        }
    }

}
