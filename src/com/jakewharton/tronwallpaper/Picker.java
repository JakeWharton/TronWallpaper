package com.jakewharton.tronwallpaper;

import com.jakewharton.tronwallpaper.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Activity which launches the live wallpaper picker and prompts for the
 * user to install our wallpaper.
 * 
 * @author Jake Wharton
 */
public class Picker extends Activity {
	/**
	 * Intent for live wallpaper picker activity.
	 */
	private static final String LIVE_WALLPAPER_CHOOSER = "android.service.wallpaper.LIVE_WALLPAPER_CHOOSER";
    
	
	
    /**
     * The timed callback handler.
     */
    private final Handler mHandler = new Handler();
	
    
    
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Wallpaper.PLAY_DEBUG) {
			//Used across the package for gameplay
	    	Wallpaper.PREFERENCES = this.getSharedPreferences(Preferences.SHARED_NAME, Context.MODE_PRIVATE);
	    	Wallpaper.CONTEXT = this;
	    	
	    	//Game it up!
	    	this.setContentView(new Bootstrapper(this));
		} else {
			//Prompt to choose our wallpaper
			Toast.makeText(this, this.getResources().getString(R.string.welcome_picker_toast), Toast.LENGTH_LONG).show();
			
			//Display wallpaper picker
			this.startActivity(new Intent(Picker.LIVE_WALLPAPER_CHOOSER));
			
			//Close this helper activity
			this.finish();
		}
	}
	
	
	
	
	private class Bootstrapper extends View implements SharedPreferences.OnSharedPreferenceChangeListener {
    	/**
    	 * Tag used for logging.
    	 */
		private static final String TAG = "TronWallpaper.Bootstrapper";
    	
    	
    	
    	/**
    	 * Instance of the game.
    	 */
    	private Game mGame;
    	
    	/**
    	 * Whether or not the wallpaper is currently visible on screen.
    	 */
        private boolean mIsVisible;
        
        /**
         * The number of FPS the user wants us to render.
         */
        private int mFPS;

        /**
         * A runnable which automates the frame rendering.
         */
        private final Runnable mDraw = new Runnable() {
            public void run() {
            	Bootstrapper.this.newFrame();
            	Bootstrapper.this.invalidate();
            }
        };
		
        
        
		public Bootstrapper(final Context context) {
			super(context);
			
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(Bootstrapper.TAG, "> Bootstrapper()");
        	}
        	
            this.mGame = new Game();

            //Load all preferences or their defaults
            Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
            this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);
            
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(Bootstrapper.TAG, "< Bootstrapper()");
        	}
		}

        
        
        /**
         * Handle the changing of a preference.
         */
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(Bootstrapper.TAG, "> onSharedPreferenceChanged()");
        	}
        	
			final boolean all = (key == null);
			final Resources resources = Wallpaper.CONTEXT.getResources();
			
			final String fps = resources.getString(R.string.settings_display_fps_key);
			if (all || key.equals(fps)) {
				this.mFPS = preferences.getInt(fps, resources.getInteger(R.integer.display_fps_default));
				
				if (Wallpaper.LOG_DEBUG) {
					Log.d(Bootstrapper.TAG, "FPS: " + this.mFPS);
				}
			}

        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(Bootstrapper.TAG, "< onSharedPreferenceChanged()");
        	}
		}

		@Override
		protected void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(Bootstrapper.TAG, "> onSurfaceChanged(width = " + width + ", height = " + height + ")");
        	}
        	
			super.onSizeChanged(width, height, oldWidth, oldHeight);
            
            //Trickle down
            this.mGame.performResize(width, height);
            
            //Redraw with new settings
            this.invalidate();
            
            if (Wallpaper.LOG_VERBOSE) {
            	Log.v(Bootstrapper.TAG, "< onSurfaceChanged()");
            }
		}

		@Override
		public void onWindowFocusChanged(final boolean hasWindowFocus) {
			super.onWindowFocusChanged(hasWindowFocus);
			this.mIsVisible = hasWindowFocus;
			
			if (hasWindowFocus) {
				this.newFrame();
			} else {
	            Picker.this.mHandler.removeCallbacks(this.mDraw);
			}
		}
        
        /**
         * Advance the game by one frame.
         */
        private void newFrame() {
        	this.mGame.tick();

        	if (Wallpaper.AUTO_FPS) {
        		if (this.mIsVisible) {
            		Picker.this.mHandler.postDelayed(this.mDraw, Wallpaper.MILLISECONDS_IN_SECOND / this.mFPS);
            	}
            }
        }

		@Override
		protected void onDraw(final Canvas canvas) {
			this.mGame.draw(canvas);
		}
	}
}
