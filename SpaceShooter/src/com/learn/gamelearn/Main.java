/**
 * Filename: Main.java
 * Purpose: Entry point activity for the app on the phone. 
 * Handles pause, resume, and saving the game state of the game.
 */

package com.learn.gamelearn;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class Main extends Activity {
	/** Called when the activity is first created. */
	//private variables used for various purposes. 
	GameView view;
	GameLoopThread loop;
	DatabaseHelper dbh;
	SQLiteDatabase db;
	ContentValues values;
	Editor editor;
	int initialX = 50;
	int x = 0;
	int y = 0;
	int speed = 0;
	String serializedCollision = "";
	boolean isBackPressed = false;
	int score = 0;
	private AudioManager manager;
	private int mediaVolume;
	ArrayList<Integer> highScore = new ArrayList<Integer>();
	
	//onCreate method or the entry point of the app.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//Remove notification bar
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		view = new GameView(this);
		setContentView(view);

		dbh = new DatabaseHelper(this);
		/*db = dbh.getReadableDatabase();
		Cursor c = db.query("gamedata", new String[] {"alien_x", "alien_Y", "alien_speed"}, null, null, null, null, null);
		if (c != null){
			while(c.moveToNext()){
				x = c.getInt(0);
				y = c.getInt(1);
				speed = c.getInt(2);
			}
		}*/
		manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		mediaVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		loop = new GameLoopThread(view);
	}

	//onPause method
	@Override
	public void onPause(){
		super.onPause();
		finish();
		if (!isBackPressed){
			db = dbh.getWritableDatabase();
			values = new ContentValues();
			//get the game state from gameView class
			x = view.getInitialX();
			y = view.getInitialY();
			values.put("alien_x", x);
			values.put("alien_y", y);
			speed = view.getSpeed();
			//store the game state in the database
			values.put("alien_speed", speed);
			values.put("collision", view.getSerializedCollision());
			values.put("current_score", view.getCurrentScore());

			db.insert("gamedata", null, values);
			db.close();
		}

	}

	//overriden onResume method
	@Override
	public void onResume(){
		super.onResume();
		db = dbh.getReadableDatabase();
		//define a cursor for the database
		Cursor c = db.query("gamedata", 
				new String[] {"alien_x", "alien_y", "alien_speed","collision","current_score"}, 
				null, null, null, null, null);
		//get the last saved game state from the database table
		if (c != null){
			while(c.moveToNext()){
				x = c.getInt(0);
				y = c.getInt(1);
				speed = c.getInt(2);
				serializedCollision = c.getString(3);
				score = c.getInt(4);
			}
		}

		//update the game state from the retrieved saved state.
		view.putX(x);
		view.putY(y);
		view.putSpeed(speed);
		if(serializedCollision != "")
			view.setSerializedCollision(serializedCollision);
		view.setScore(score);
	}

	//if back button is pressed, exits the game
	@Override
	public void onBackPressed(){


		finish();
		isBackPressed = true;
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Log.e("game", e.getMessage());
		}
		db = dbh.getWritableDatabase();
		db.delete("gamedata", null, null);
	}
	
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
			if(mediaVolume > 0){
				mediaVolume--;
				return true;
			}
		}
		else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			if(mediaVolume < manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)){
				mediaVolume++;
				return true;
			}
		}
		return false;
	}
*/
}