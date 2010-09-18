package com.jakewharton.tronwallpaper;

import com.jakewharton.tronwallpaper.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * An apple a day...
 * 
 * @author Jake Wharton
 */
public class Wallpaper extends WallpaperService {
	/**
	 * SharedPreferences instance.
	 */
	/*package*/static SharedPreferences PREFERENCES;
	
	/**
	 * Wallpaper Context instance.
	 */
	/*package*/static Context CONTEXT;
    
	/**
	 * Whether or not we are logging in debug mode.
	 */
	/*package*/static final boolean LOG_DEBUG = false;
	
	/**
	 * Whether or not we are logging in verbose mode.
	 */
	/*package*/static final boolean LOG_VERBOSE = false;
	
	/**
	 * Whether or not the wallpaper should automatically advance to the next frame.
	 */
    /*package*/ static final boolean AUTO_FPS = true;

    /**
     * Whether or not to allow playback (and debugging!) in the Picker activity.
     */
	/*package*/static final boolean PLAY_DEBUG = false;
    
    /**
     * Height (in DIP) of the status bar. Usually.
     */
    private static final int STATUS_BAR_HEIGHT = 24;
    
    /**
     * Height (in DIP) of the app drawer on the launcher.
     */
    private static final int APP_DRAWER_HEIGHT = 50;
	
	/**
	 * Number of millisecond in a second.
	 */
	/*package*/static final int MILLISECONDS_IN_SECOND = 1000;
	
	/**
	 * Maximum time between taps that will reset the game.
	 */
	/*package*/static final long RESET_THRESHOLD = 100;
    
    
    
    /**
     * The timed callback handler.
     */
    private final Handler mHandler = new Handler();

    
    
    @Override
    public Engine onCreateEngine() {
    	Wallpaper.PREFERENCES = this.getSharedPreferences(Preferences.SHARED_NAME, Context.MODE_PRIVATE);
    	Wallpaper.CONTEXT = this;
    	
    	this.performFirstRunCheckAndSetup();
    	
        return new LightCycleEngine();
    }
    
    /**
     * Sets up some preferences based on screen size on the first run only.
     */
    private void performFirstRunCheckAndSetup() {
    	final Resources resources = this.getResources();
        final int defaultVersion = resources.getInteger(R.integer.version_code_default);
        final int previousVersion = Wallpaper.PREFERENCES.getInt(resources.getString(R.string.version_code_key), defaultVersion);
        
        if (previousVersion == defaultVersion) {
        	//First install
        	final float density = this.getResources().getDisplayMetrics().density;
        	final SharedPreferences.Editor editor = Wallpaper.PREFERENCES.edit();
        	
        	//Base top and bottom padding off of known metrics
        	final int topHeight = (int)(density * Wallpaper.STATUS_BAR_HEIGHT);
        	final int bottomHeight = (int)(density * Wallpaper.APP_DRAWER_HEIGHT);
        	editor.putInt(resources.getString(R.string.settings_display_padding_top_key), topHeight);
        	editor.putInt(resources.getString(R.string.settings_display_padding_bottom_key), bottomHeight);
        	
        	editor.commit();
        } else {
        	//Check for any upgrade steps in upgrade order
        	
        	//XXX: Put upgrade scripts here...
        }
    }

    
    
    /**
     * Wallpaper engine to manage the Game instance.
     * 
     * @author Jake Wharton
     */
    private class LightCycleEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
    	/**
    	 * Tag used for logging.
    	 */
    	private static final String TAG = "TronWallpaper.LightCycleEngine";
    	
    	
    	
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
         * Whether or not user input is taken into consideration.
         */
        private boolean mIsControllable;
        
        /**
         * The absolute center of the screen horizontally.
         */
        private float mScreenCenterX;
        
        /**
         * The absolute center of the screen vertically.
         */
        private float mScreenCenterY;
        
        /**
         * The system milliseconds of the last user touch.
         */
        private long mLastTouch;

        /**
         * A runnable which automates the frame rendering.
         */
        private final Runnable mDraw = new Runnable() {
            public void run() {
            	LightCycleEngine.this.newFrame();
                LightCycleEngine.this.draw();
            }
        };

        
        
        /**
         * Create instance of the engine.
         */
        public LightCycleEngine() {
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(LightCycleEngine.TAG, "> LightCycleEngine()");
        	}
        	
            this.mGame = new Game();
            this.mLastTouch = 0;

            //Load all preferences or their defaults
            Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
            this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);
            
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(LightCycleEngine.TAG, "< LightCycleEngine()");
        	}
        }

        
        
        /**
         * Handle the changing of a preference.
         */
		public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(LightCycleEngine.TAG, "> onSharedPreferenceChanged()");
        	}
        	
			final boolean all = (key == null);
			final Resources resources = Wallpaper.CONTEXT.getResources();
			
			final String fps = Wallpaper.this.getString(R.string.settings_display_fps_key);
			if (all || key.equals(fps)) {
				this.mFPS = preferences.getInt(fps, resources.getInteger(R.integer.display_fps_default));
				
				if (Wallpaper.LOG_DEBUG) {
					Log.d(LightCycleEngine.TAG, "FPS: " + this.mFPS);
				}
			}
			
			final String userControl = Wallpaper.this.getString(R.string.settings_game_usercontrol_key);
			if (all || key.equals(userControl)) {
				this.mIsControllable = preferences.getBoolean(userControl, resources.getBoolean(R.bool.game_usercontrol_default));
				
				if (Wallpaper.LOG_DEBUG) {
					Log.d(LightCycleEngine.TAG, "Is User Controllable: " + this.mIsControllable);
				}
			}

        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(LightCycleEngine.TAG, "< onSharedPreferenceChanged()");
        	}
		}

        @Override
        public void onVisibilityChanged(final boolean visible) {
            this.mIsVisible = visible;
            if (visible) {
                this.draw();
                
                if (Wallpaper.AUTO_FPS) {
                	this.newFrame();
                }
            } else {
                Wallpaper.this.mHandler.removeCallbacks(this.mDraw);
            }
        }
        
        @Override
        public void onCreate(final SurfaceHolder surfaceHolder) {
        	super.onCreate(surfaceHolder);

        	if (this.mIsControllable) {
        		//By default we don't get touch events, so enable them.
        		this.setTouchEventsEnabled(true);
        	}
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            Wallpaper.this.mHandler.removeCallbacks(mDraw);
        }
        
        @Override
        public void onTouchEvent(final MotionEvent event) {
        	if (event.getAction() == MotionEvent.ACTION_DOWN) {
        		final long touch = System.currentTimeMillis();
        		if (touch - this.mLastTouch < Wallpaper.RESET_THRESHOLD) {
        			this.mGame.newBoard();
        			this.mLastTouch = 0;
        		} else if (this.mIsControllable) {
	        		this.mLastTouch = touch;
	        		
	        		final float deltaX = this.mScreenCenterX - event.getX();
	        		final float deltaY = this.mScreenCenterY - event.getY();
	        		
	        		if (Math.abs(deltaX) > Math.abs(deltaY)) {
	        			if (deltaX > 0) {
	        				this.mGame.setWantsToGo(Game.Direction.WEST);
	        			} else {
	        				this.mGame.setWantsToGo(Game.Direction.EAST);
	        			}
	        		} else {
	        			if (deltaY > 0) {
	        				this.mGame.setWantsToGo(Game.Direction.NORTH);
	        			} else {
	        				this.mGame.setWantsToGo(Game.Direction.SOUTH);
	        			}
	        		}
        		}
        		
        		if (!Wallpaper.AUTO_FPS) {
        			this.mGame.tick();
        			this.draw();
        		}
        	}
        }

        @Override
        public void onSurfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
        	if (Wallpaper.LOG_VERBOSE) {
        		Log.v(LightCycleEngine.TAG, "> onSurfaceChanged(width = " + width + ", height = " + height + ")");
        	}
        	
            super.onSurfaceChanged(holder, format, width, height);
            
            this.mScreenCenterX = width / 2.0f;
            this.mScreenCenterY = height / 2.0f;
            
            if (Wallpaper.LOG_DEBUG) {
            	Log.d(LightCycleEngine.TAG, "Center X: " + this.mScreenCenterX);
            	Log.d(LightCycleEngine.TAG, "Center Y: " + this.mScreenCenterY);
            }
            
            //Trickle down
            this.mGame.performResize(width, height);
            
            //Redraw with new settings
            this.draw();
            
            if (Wallpaper.LOG_VERBOSE) {
            	Log.v(LightCycleEngine.TAG, "< onSurfaceChanged()");
            }
        }

        @Override
        public void onSurfaceDestroyed(final SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.mIsVisible = false;
            Wallpaper.this.mHandler.removeCallbacks(this.mDraw);
        }
        
        /**
         * Advance the game by one frame.
         */
        private void newFrame() {
        	this.mGame.tick();

        	if (Wallpaper.AUTO_FPS) {
        		if (this.mIsVisible) {
            		Wallpaper.this.mHandler.postDelayed(this.mDraw, Wallpaper.MILLISECONDS_IN_SECOND / this.mFPS);
            	}
            }
        }

        /**
         * Draws the current state of the game to the wallpaper.
         */
        private void draw() {
            final SurfaceHolder holder = this.getSurfaceHolder();

            Canvas c = null;
            try {
                c = holder.lockCanvas();
                if (c != null) {
                    this.mGame.draw(c);
                }
            } finally {
                if (c != null) {
                	holder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}
