package com.android.kino.logic.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;

import com.android.kino.Kino;
import com.android.kino.logic.AlbumProperties;
import com.android.kino.logic.TaskMasterService;
import com.android.kino.musiclibrary.LastFmHandlerAlbum;
import com.android.kino.musiclibrary.LastFmHandlerAlbum.lastfmAlbumDetails;
import com.android.kino.utils.ConvertUtils;

public class FetchAlbumDetails extends KinoTask{		
	private String mArtistTitle;
	private String mAlbumTitle;			
	private AlbumProperties mAlbum;
		
	private final String LOGTAG="FetchAlbumDetails";
	private LastFmHandlerAlbum queryHandler = new LastFmHandlerAlbum();
	
	public FetchAlbumDetails(AlbumProperties album){	
		mAlbum=album;
		mArtistTitle=album.getArtistName();
		mAlbumTitle=album.getAlbumName();
		mTaskTitle="Fetching \"" +mAlbumTitle + "\" from last.fm...";
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		//getXML();		
							
		URL xmlURL = buildURL(queryHandler.getAlbumQueryURL(mArtistTitle, mAlbumTitle));
		downloadFile(xmlURL, "fetching XML",null);
		
		//parse XML
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser sp=null;
	
    try {
		sp = spf.newSAXParser();
	} catch (ParserConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
      XMLReader xr=null;
	try {
		xr = sp.getXMLReader();
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}           
      xr.setContentHandler(queryHandler);
      
      ByteArrayInputStream xmlStream = new ByteArrayInputStream(mDownloadedFile);      
      InputSource input=new InputSource(xmlStream);
      try {
		xr.parse(input);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SAXException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
		lastfmAlbumDetails albumDetails=queryHandler.getAlbumDetails();
		String imagePath=albumDetails.images.get("large");
		
		if (imagePath==null){
			mAlbum.disableImage();
			displayMessage("No image on LastFM for "+mArtistTitle+" - "+mAlbumTitle+" :(");
			
			cancel(true);
		}
		else{
			
			URL imageURL=buildURL(imagePath);
			String postfix=imagePath.substring(imagePath.length()-4,imagePath.length());
			
			final String albumImagePath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Kino.ALBUM_DIR;
			final String albumFileName=ConvertUtils.safeFileName(mArtistTitle)+"-"+ConvertUtils.safeFileName(mAlbumTitle)+postfix;
			File albumImageFile = new File(albumImagePath,albumFileName);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(albumImageFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			//check for available storage size
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath()); 
            long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getBlockCount(); 
            
			 if (mDownloaded> bytesAvailable){
	         		displayMessage("Oops! not enough free space on SD card!");
	        		cancel(true);
	        		Log.e("Kino task "+mTaskId, "not enough free space on SD card. needed "+mDownloaded+" available "+bytesAvailable);
				}
			
			downloadFile(imageURL, fos, "downloading album image...", new onDownloadComplete() {
				
				@Override
				public void finishedDownload() {	
								
					Log.d(LOGTAG, "succesfully wrote "+albumImagePath+"/"+albumFileName);
					   
					Bitmap albumImage = BitmapFactory.decodeFile(albumImagePath+"/"+albumFileName);
					if (albumImage !=null){
						mAlbum.setImage(albumImage);
						mAlbum.stopSearching();
						updateUI();
					}
					else{
						Log.e("Kino task "+mTaskId, "couldn't decode downloaded image!");						
					}
														
					
				}
			});
		
		}
	
		return null;
	}				

	protected void publishProgress(int progress) {
		//Log.d("progress",downloadURL+" "+progress);
		
		mTaskProgress=progress;			
		updateUI();
	}	
		
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		mTaskMaster.taskDone(this);		
	}
	
	protected void updateUI(){
		Message msg = new Message();
		msg.what=TaskMasterService.MSG_UPDATEVIEW;
		mTaskMaster.getMessageHandler().sendMessage(msg);	
	}
	
	public boolean equals(FetchAlbumDetails another){
		return (mAlbumTitle.equals(another.mAlbumTitle) &&
				mArtistTitle.equals(another.mArtistTitle));
	}

}
