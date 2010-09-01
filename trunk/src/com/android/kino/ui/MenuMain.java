package com.android.kino.ui;

import android.content.Intent;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.kino.Kino;
import com.android.kino.R;
import com.android.kino.logic.AlbumList;
import com.android.kino.logic.ArtistList;
import com.android.kino.logic.MediaProperties;
import com.android.kino.logic.Playlist;
import com.android.kino.ui.listAdapters.SongAdapter;

public class MenuMain extends KinoUI {
			 
    private ListView playlistView=null;
    Playlist playlist=null;
    private ArrayAdapter<MediaProperties> playlistAdapter;
    
    
    //listener for all song list
    OnItemClickListener songsClickListener=new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
			   // set the player to start playing the right song in the playlist
			   mPlayer.setCurrentPlaylist(playlist);
			   mPlayer.setCurrentMedia(position);
			   mPlayer.togglePlayPause();
			   startActivity(new Intent(MenuMain.this,PlayerMain.class));
		};
	};
	
	
    
	

	
	@Override
	protected void initUI() {	    				

			
			setContentView(R.layout.menu_main);
			playlistView=(ListView)findViewById(R.id.mainmenu_list);			
									
			Button bAllSongs = (Button) findViewById(R.id.mainmenu_btn_allsongs);
			bAllSongs.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					setAllSongs();
					
				}
			});
			
			Button bArtists = (Button) findViewById(R.id.mainmenu_btn_artists);
			bArtists.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {

					Intent albumlistIntent=new Intent(MenuMain.this,MenuArtistBrowse.class);
					ArtistList allArtists=library.getAllArtists();
					//TODO fetch the allsongs playlist from the library and pass it
					albumlistIntent.putExtra("artistlist",(Parcelable)allArtists);						
		    		startActivity(albumlistIntent);
					
				}
			});
			
			Button bAlbums = (Button) findViewById(R.id.mainmenu_btn_albums);
			bAlbums.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					
					Intent albumlistIntent=new Intent(MenuMain.this,MenuAlbumBrowse.class);
					AlbumList allAlbums =library.getAllAlbums();
					//TODO fetch the allsongs playlist from the library and pass it
					albumlistIntent.putExtra("albumlist",(Parcelable)allAlbums);						
		    		startActivity(albumlistIntent);
					
					
					
				}
			});
			
			Button bPreferences = (Button) findViewById(R.id.mainmenu_btn_preferences);
			bPreferences.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Toast.makeText(MenuMain.this, "Not impemented yet... :(", Toast.LENGTH_SHORT).show();
					
				}
			});
	}
	
	private void setAllSongs(){
		playlist=library.getAllSongs();
		playlistAdapter = new SongAdapter(this,0, playlist,false);		
		playlistView.setAdapter(playlistAdapter);					
		playlistView.setOnItemClickListener(songsClickListener);
	}
	

	
	
	@Override
	public void onKinoInit(Kino kino) {	
		super.onKinoInit(kino);
		setAllSongs();
	}
	
	@Override
	public void updateUI() {	
		super.updateUI();
		playlistAdapter.notifyDataSetChanged();
	}
	

}