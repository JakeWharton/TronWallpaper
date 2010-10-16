package com.jakewharton.tronwallpaper;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import com.jakewharton.tronwallpaper.R;
import com.jakewharton.utilities.WidgetLocationsPreference;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * Tron game.
 * 
 * @author Jake Wharton
 */
public class Game implements SharedPreferences.OnSharedPreferenceChangeListener {
	/**
	 * Possible directions of travel.
	 * 
	 * @author Jake Wharton
	 */
	enum Direction {
		NORTH, SOUTH, EAST, WEST;
		
		
		
		/**
		 * Get the direction that is the opposite of this one.
		 * 
		 * @return Opposite direction.
		 */
		public Game.Direction getOpposite() {
			switch (this) {
				case NORTH:
					return Game.Direction.SOUTH;
				case SOUTH:
					return Game.Direction.NORTH;
				case EAST:
					return Game.Direction.WEST;
				case WEST:
					return Game.Direction.EAST;
				default:
					throw new IllegalStateException("This is impossible.");
			}
		}
	}
	
	
	
	/**
	 * Single random number generate for this wallpaper.
	 */
	/*package*/static final Random RANDOM = new Random();
	
	/**
	 * Tag used for logging.
	 */
	private static final String TAG = "TronWallpaper.Game";
	
	/**
	 * Cell value for a wall.
	 */
	private static final boolean CELL_WALL = false;
	
	/**
	 * Cell value for a blank space.
	 */
	private static final boolean CELL_BLANK = true;
	
	/**
	 * Block cells between icon rows.
	 */
	private static final int CELLS_BETWEEN_ROW = 4;
	
	/**
	 * Block cells between icon columns.
	 */
	private static final int CELLS_BETWEEN_COLUMN = 4;
	
	/**
	 * If a random number [0, RANDOM_TURN_MULTIPLIER) is 0 then randomly turn the light cycle.
	 */
	private static final int RANDOM_TURN_MULTIPLIER = 250;
	

	
	/**
	 * Number of cells on the board horizontally.
	 */
	private int mCellsWide;
	
	/**
	 * Number of cells on the board vertically.
	 */
	private int mCellsTall;
	
	/**
	 * Number of cells horizontally between the columns.
	 */
	private int mCellColumnSpacing;
	
	/**
	 * Number of cells vertically between the rows.
	 */
	private int mCellRowSpacing;
	
	/**
	 * Width (in pixels) of a single cell.
	 */
	private float mScaleX;
	
	/**
	 * Height (in pixels) of a single cell.
	 */
	private float mScaleY;
	
	/**
	 * Height (in pixels) of the screen.
	 */
    private int mScreenHeight;
    
    /**
     * Width (in pixels) of the screen.
     */
    private int mScreenWidth;
    
    /**
     * Whether or not the screen is currently in landscape mode.
     */
    private boolean mIsLandscape;
    
    /**
     * Number of icon rows on the launcher.
     */
    private int mIconRows;
    
    /**
     * Number of icon columns on the launcher.
     */
    private int mIconCols;
    
    /**
     * 2-dimensional array of the board's cells.
     */
	private boolean[][] mBoard;
    
    /**
     * Color of the background.
     */
    private int mGameBackground;
    
    /**
     * Top padding (in pixels) of the grid from the screen top.
     */
    private float mDotGridPaddingTop;
    
    /**
     * Left padding (in pixels) of the grid from the screen left.
     */
    private float mDotGridPaddingLeft;
    
    /**
     * Bottom padding (in pixels) of the grid from the screen bottom.
     */
    private float mDotGridPaddingBottom;
    
    /**
     * Right padding (in pixels) of the grid from the screen right.
     */
    private float mDotGridPaddingRight;
    
    /**
     * Path to the user background image (if any).
     */
    private String mBackgroundPath;
    
    /**
     * The user background image (if any).
     */
    private Bitmap mBackground;
    
    /**
     * The locations of widgets on the launcher.
     */
    private List<Rect> mWidgetLocations;
    
    /**
     * Paint to draw the background color.
     */
    private final Paint mBackgroundPaint;
    
    /**
     * Walls foreground color.
     */
    private final Paint mWallsForeground;
    
    /**
     * Whether or not we are displaying icon walls
     */
    private boolean mIsDisplayingWalls;
    
    /**
     * Player cycle positions
     */
    private final LinkedList<Point> mPlayer;
    
    /**
     * Opponent cycle positions
     */
    private final LinkedList<Point> mOpponent;
    
    /**
     * Player cycle direction.
     */
    private Game.Direction mDirectionPlayer;
    
    /**
     * Opponent cycle direction.
     */
    private Game.Direction mDirectionOpponent;
    
    /**
     * Direction the user wants us to travel in.
     */
    private Game.Direction mWantsToGo;
    
    /**
     * Opponent cycle color.
     */
    private final Paint mOpponentForeground;
    
    /**
     * Player cycle color.
     */
    private final Paint mPlayerForeground;
    
    /**
     * Precalculated wall rectangles for drawing
     */
    private final List<RectF> mWalls;
    
    
    
    /**
     * Create a new game.
     */
    public Game() {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> Game()");
    	}

        //Create Paints
    	this.mWallsForeground = new Paint(Paint.ANTI_ALIAS_FLAG);
    	this.mWallsForeground.setStyle(Paint.Style.STROKE);
        this.mBackgroundPaint = new Paint();
        this.mOpponentForeground = new Paint();
        this.mPlayerForeground = new Paint();
        
        this.mPlayer = new LinkedList<Point>();
        this.mOpponent = new LinkedList<Point>();
        this.mWalls = new LinkedList<RectF>();
        
        //Load all preferences or their defaults
        Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
        this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< Game()");
    	}
    }

    
    
    /**
     * Handle the changing of a preference.
     */
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> onSharedPreferenceChanged()");
    	}
    	
		final boolean all = (key == null);
		final Resources resources = Wallpaper.CONTEXT.getResources();
		
		boolean hasLayoutChanged = false;
		boolean hasGraphicsChanged = false;
		
		final String showWalls = resources.getString(R.string.settings_display_showwalls_key);
		if (all || key.equals(showWalls)) {
			this.mIsDisplayingWalls = preferences.getBoolean(showWalls, resources.getBoolean(R.bool.display_showwalls_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Is Displaying Walls: " + this.mIsDisplayingWalls);
			}
		}
		
		final String widgetLocations = resources.getString(R.string.settings_display_widgetlocations_key);
		if (all || key.equals(widgetLocations)) {
			this.mWidgetLocations = WidgetLocationsPreference.convertStringToWidgetList(preferences.getString(widgetLocations, resources.getString(R.string.display_widgetlocations_default)));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Widget Locations: " + (this.mWidgetLocations.size() / 4));
			}
		}
		
		
		// COLORS //
        
		final String gameBackground = resources.getString(R.string.settings_color_background_key);
		if (all || key.equals(gameBackground)) {
			this.mGameBackground = preferences.getInt(gameBackground, resources.getInteger(R.integer.color_background_default));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Background: #" + Integer.toHexString(this.mGameBackground));
			}
		}
		
		final String wallsForeground = resources.getString(R.string.settings_color_walls_key);
		if (all || key.equals(wallsForeground)) {
			this.mWallsForeground.setColor(preferences.getInt(wallsForeground, resources.getInteger(R.integer.color_walls_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Walls Foreground: #" + Integer.toHexString(this.mWallsForeground.getColor()));
			}
		}
		
		final String backgroundImage = resources.getString(R.string.settings_color_bgimage_key);
		if (all || key.equals(backgroundImage)) {
			this.mBackgroundPath = preferences.getString(backgroundImage, null);
			
			if (this.mBackgroundPath != null) {			
				if (Wallpaper.LOG_DEBUG) {
					Log.d(Game.TAG, "Background Image: " + this.mBackgroundPath);
				}
				
				//Trigger performResize
				hasGraphicsChanged = true;
			} else {
				this.mBackground = null;
			}
		}
		
		final String backgroundOpacity = resources.getString(R.string.settings_color_bgopacity_key);
		if (all || key.equals(backgroundOpacity)) {
			this.mBackgroundPaint.setAlpha(preferences.getInt(backgroundOpacity, resources.getInteger(R.integer.color_bgopacity_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Background Image Opacity: " + this.mBackgroundPaint.getAlpha());
			}
		}
		
		final String lightCycle = resources.getString(R.string.settings_color_player_key);
		if (all || key.equals(lightCycle)) {
			this.mPlayerForeground.setColor(preferences.getInt(lightCycle, resources.getInteger(R.integer.color_player_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Player Foreground: #" + Integer.toHexString(this.mPlayerForeground.getColor()));
			}
		}
		
		final String opponent = resources.getString(R.string.settings_color_opponent_key);
		if (all || key.equals(opponent)) {
			this.mOpponentForeground.setColor(preferences.getInt(opponent, resources.getInteger(R.integer.color_opponent_default)));
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Opponent Foreground: #" + Integer.toHexString(this.mOpponentForeground.getColor()));
			}
		}
    	
        
		// GRID //
		
		final String dotGridPaddingLeft = resources.getString(R.string.settings_display_padding_left_key);
		if (all || key.equals(dotGridPaddingLeft)) {
			this.mDotGridPaddingLeft = preferences.getInt(dotGridPaddingLeft, resources.getInteger(R.integer.display_padding_left_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Left: " + this.mDotGridPaddingLeft);
			}
		}

		final String dotGridPaddingRight = resources.getString(R.string.settings_display_padding_right_key);
		if (all || key.equals(dotGridPaddingRight)) {
			this.mDotGridPaddingRight = preferences.getInt(dotGridPaddingRight, resources.getInteger(R.integer.display_padding_right_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Right: " + this.mDotGridPaddingRight);
			}
		}

		final String dotGridPaddingTop = resources.getString(R.string.settings_display_padding_top_key);
		if (all || key.equals(dotGridPaddingTop)) {
			this.mDotGridPaddingTop = preferences.getInt(dotGridPaddingTop, resources.getInteger(R.integer.display_padding_top_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Top: " + this.mDotGridPaddingTop);
			}
		}

		final String dotGridPaddingBottom = resources.getString(R.string.settings_display_padding_bottom_key);
		if (all || key.equals(dotGridPaddingBottom)) {
			this.mDotGridPaddingBottom = preferences.getInt(dotGridPaddingBottom, resources.getInteger(R.integer.display_padding_bottom_default));
			hasGraphicsChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Dot Grid Padding Bottom: " + this.mDotGridPaddingBottom);
			}
		}
		
		
		// CELLS //
		
		final String iconRows = resources.getString(R.string.settings_display_iconrows_key);
		if (all || key.equals(iconRows)) {
			this.mIconRows = preferences.getInt(iconRows, resources.getInteger(R.integer.display_iconrows_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Icon Rows: " + this.mIconRows);
			}
		}
		
		final String iconCols = resources.getString(R.string.settings_display_iconcols_key);
		if (all || key.equals(iconCols)) {
			this.mIconCols = preferences.getInt(iconCols, resources.getInteger(R.integer.display_iconcols_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Icon Cols: " + this.mIconCols);
			}
		}
		
		final String cellSpacingRow = resources.getString(R.string.settings_display_rowspacing_key);
		if (all || key.equals(cellSpacingRow)) {
			this.mCellRowSpacing = preferences.getInt(cellSpacingRow, resources.getInteger(R.integer.display_rowspacing_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
		    	Log.d(Game.TAG, "Cell Row Spacing: " + this.mCellRowSpacing);
			}
		}
		
		final String cellSpacingCol = resources.getString(R.string.settings_display_colspacing_key);
		if (all || key.equals(cellSpacingCol)) {
			this.mCellColumnSpacing = preferences.getInt(cellSpacingCol, resources.getInteger(R.integer.display_colspacing_default));
			hasLayoutChanged = true;
			
			if (Wallpaper.LOG_DEBUG) {
		    	Log.d(Game.TAG, "Cell Column Spacing: " + this.mCellColumnSpacing);
			}
		}
		
		if (hasLayoutChanged) {
	    	this.mCellsWide = (this.mIconCols * (this.mCellColumnSpacing + Game.CELLS_BETWEEN_COLUMN)) + Game.CELLS_BETWEEN_COLUMN;
	    	this.mCellsTall = (this.mIconRows * (this.mCellRowSpacing + Game.CELLS_BETWEEN_ROW)) + Game.CELLS_BETWEEN_ROW;
	    	
	    	if (Wallpaper.LOG_DEBUG) {
	    		Log.d(Game.TAG, "Cells Wide: " + this.mCellsWide);
	    		Log.d(Game.TAG, "Cells Tall: " + this.mCellsTall);
	    	}
	    	
	    	//Create playing board
	        this.mBoard = new boolean[this.mCellsTall][this.mCellsWide];
		}
		if (hasLayoutChanged || hasGraphicsChanged) {
			if ((this.mScreenWidth > 0) && (this.mScreenHeight > 0)) {
				//Resize everything to fit
				this.performResize(this.mScreenWidth, this.mScreenHeight);
			}

	    	this.newBoard();
		}

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< onSharedPreferenceChanged()");
    	}
	}

    /**
     * Test if a Point is a valid coordinate on the game board.
     * 
     * @param position Point representing coordinate.
     * @return Boolean indicating whether or not the position is valid.
     */
	private boolean isValidPosition(final Point position) {
		return ((position.x >= 0) && (position.x < this.mCellsWide)
				&& (position.y >= 0) && (position.y < this.mCellsTall)
				&& (this.mBoard[position.y][position.x] == Game.CELL_BLANK));
	}
    
    /**
     * Reset the game state to that of first initialization.
     */
    public void newBoard() {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> newBoard()");
    	}

    	//Initialize board
    	final int iconCellsWidth = this.mCellColumnSpacing + Game.CELLS_BETWEEN_COLUMN;
    	final int iconCellsHeight = this.mCellRowSpacing + Game.CELLS_BETWEEN_ROW;
    	for (int y = 0; y < this.mCellsTall; y++) {
    		for (int x = 0; x < this.mCellsWide; x++) {
    			this.mBoard[y][x] = (((x % iconCellsWidth) < Game.CELLS_BETWEEN_COLUMN) || ((y % iconCellsHeight) < Game.CELLS_BETWEEN_ROW)) ? Game.CELL_BLANK : Game.CELL_WALL;
    		}
    	}
    	
    	//Remove board under widgets
    	for (final Rect widget : this.mWidgetLocations) {
    		if (Wallpaper.LOG_DEBUG) {
    			Log.d(Game.TAG, "Widget: L=" + widget.left + ", T=" + widget.top + ", R=" + widget.right + ", B=" + widget.bottom);
    		}
    		
    		final int left = (widget.left * iconCellsWidth) + Game.CELLS_BETWEEN_COLUMN;
    		final int top = (widget.top * iconCellsHeight) + Game.CELLS_BETWEEN_ROW;
    		final int right = (widget.right * iconCellsWidth) + iconCellsWidth - 1;
    		final int bottom = (widget.bottom * iconCellsHeight) + iconCellsHeight - 1;
    		for (int y = top; y <= bottom; y++) {
    			for (int x = left; x <= right; x++) {
    				this.mBoard[y][x] = Game.CELL_WALL;
    			}
    		}
    	}
    	
    	//Initialize game
    	this.newGame();
    	
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< newBoard()");
    	}
    }
    
    /**
     * Reset players.
     */
    private void newGame() {
    	//Create player and opponent
    	this.mPlayer.clear();
    	this.mOpponent.clear();
    	
    	//Get starting position for players
    	this.mPlayer.add(this.getRandomValidPosition());
    	this.mOpponent.add(this.getRandomValidPosition());
    	
    	//No user direction
    	this.mWantsToGo = null;
    }
    
    /**
     * Get a random valid position on the board.
     * 
     * @return Point.
     */
    private Point getRandomValidPosition() {
    	final Point position = new Point();
    	while (true) {
    		position.x = Game.RANDOM.nextInt(this.mCellsWide);
    		position.y = Game.RANDOM.nextInt(this.mCellsTall);
    		if (!this.isCollision(position)) {
    			return position;
    		}
    	}
    }
    
    /**
     * Set the user desired direction.
     * 
     * @param direction
     */
    public void setWantsToGo(final Game.Direction direction) {
    	this.mWantsToGo = direction;
    	
    	if (Wallpaper.LOG_DEBUG) {
    		Log.d(Game.TAG, "Wants To Go: " + direction.toString());
    	}
    }
    
    /**
     * Iterate player and opponent.
     */
    public void tick() {
    	this.determineNextPlayerDirection();
    	final Point playerNewPoint = Game.move(this.mPlayer.getLast(), this.mDirectionPlayer);
    	if (this.isCollision(playerNewPoint)) {
    		this.newGame();
    		return;
    	}
    	this.mPlayer.add(playerNewPoint);
    	
    	this.determineNextOpponentDirection();
    	final Point opponentNewPoint = Game.move(this.mOpponent.getLast(), this.mDirectionOpponent);
    	if (this.isCollision(opponentNewPoint)) {
    		this.newGame();
    		return;
    	}
    	this.mOpponent.add(opponentNewPoint);
    }
    
    /**
     * Test if a point collides with the player or opponent.
     * 
     * @param testPoint Point to test.
     * @return Whether or not the point collides.
     */
    private boolean isCollision(final Point testPoint) {
    	//Test for walls
    	if (!this.isValidPosition(testPoint)) {
    		return true;
    	}
    	//Test opponent first
    	for (final Point point : this.mOpponent) {
    		if (Game.pointEquals(point, testPoint)) {
    			return true;
    		}
    	}
    	//Test player
    	for (final Point point : this.mPlayer) {
    		if (Game.pointEquals(point, testPoint)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Try to go straight, otherwise make a valid turn and, on occasion, randomize!
     */
    private void determineNextPlayerDirection() {
		final Point head = this.mPlayer.getLast();
		
		//Try the user direction first
		final Point newPoint = Game.move(head, this.mWantsToGo);
		if ((this.mWantsToGo != null) && !this.isCollision(newPoint)) {
			//Follow user direction and GTFO
			this.mDirectionPlayer = this.mWantsToGo;
			return;
		}
		
    	//TODO: real AI
		Game.Direction nextDirection = null;
    	//favor current direction most of the time
    	if (!this.isCollision(Game.move(head, this.mDirectionPlayer)) && (Game.RANDOM.nextInt(Game.RANDOM_TURN_MULTIPLIER) != 0)) {
    		nextDirection = this.mDirectionPlayer;
    	} else {
	    	final List<Game.Direction> directions = new LinkedList<Game.Direction>();
	    	for (final Game.Direction direction : Game.Direction.values()) {
	    		if (!this.isCollision(Game.move(head, direction))) {
	    			directions.add(direction);
	    		}
	    	}
	    	if (directions.size() == 0) {
	    		nextDirection = Game.Direction.NORTH;
	    	} else {
	    		nextDirection = directions.get(Game.RANDOM.nextInt(directions.size()));
	    	}
    	}
		
		//If the wants-to-go direction exists and the AI forced us to change direction then wants-to-go direction
		//is impossible and should be cleared
		if ((this.mWantsToGo != null) && (this.mDirectionPlayer != nextDirection)) {
			this.mWantsToGo = null;
			
			if (Wallpaper.LOG_DEBUG) {
				Log.d(Game.TAG, "Clearing wants-to-go direction via AI.");
			}
		}
		
		this.mDirectionPlayer = nextDirection;
    }
    
    /**
     * Try to go straight, otherwise make a valid turn and, on occasion, randomize!
     */
    private void determineNextOpponentDirection() {
    	//TODO: real AI
    	final Point position = this.mOpponent.getLast();
    	//favor current direction most of the time
    	if (!this.isCollision(Game.move(position, this.mDirectionOpponent)) && (Game.RANDOM.nextInt(Game.RANDOM_TURN_MULTIPLIER) != 0)) {
    		return;
    	}
    	final List<Game.Direction> directions = new LinkedList<Game.Direction>();
    	for (final Game.Direction direction : Game.Direction.values()) {
    		if (!this.isCollision(Game.move(position, direction))) {
    			directions.add(direction);
    		}
    	}
    	if (directions.size() == 0) {
    		this.mDirectionOpponent = Game.Direction.NORTH;
    	} else {
    		this.mDirectionOpponent = directions.get(Game.RANDOM.nextInt(directions.size()));
    	}
    }

    /**
     * Resize the game board and all entities according to a new width and height.
     * 
     * @param screenWidth New width.
     * @param screenHeight New height.
     */
    public void performResize(int screenWidth, int screenHeight) {
    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "> performResize(width = " + screenWidth + ", height = " + screenHeight + ")");
    	}
    	
    	//Background image
    	if (this.mBackgroundPath != null) {
			try {
				final Bitmap temp = BitmapFactory.decodeStream(Wallpaper.CONTEXT.getContentResolver().openInputStream(Uri.parse(this.mBackgroundPath)));
				final float pictureAR = temp.getWidth() / (temp.getHeight() * 1.0f);
				final float screenAR = screenWidth / (screenHeight * 1.0f);
				int newWidth;
				int newHeight;
				int x;
				int y;
				
				if (pictureAR > screenAR) {
					//wider than tall related to the screen AR
					newHeight = screenHeight;
					newWidth = (int)(temp.getWidth() * (screenHeight / (temp.getHeight() * 1.0f)));
					x = (newWidth - screenWidth) / 2;
					y = 0;
				} else {
					//taller than wide related to the screen AR
					newWidth = screenWidth;
					newHeight = (int)(temp.getHeight() * (screenWidth / (temp.getWidth() * 1.0f)));
					x = 0;
					y = (newHeight - screenHeight) / 2;
				}
				
	    		final Bitmap scaled = Bitmap.createScaledBitmap(temp, newWidth, newHeight, false);
	    		this.mBackground = Bitmap.createBitmap(scaled, x, y, screenWidth, screenHeight);
			} catch (final Exception e) {
				e.printStackTrace();
				Log.w(Game.TAG, "Unable to load background bitmap.");
				Toast.makeText(Wallpaper.CONTEXT, "Unable to load background bitmap.", Toast.LENGTH_SHORT).show();
				this.mBackground = null;
			}
    	}
    	
    	this.mIsLandscape = (screenWidth > screenHeight);
    	this.mScreenWidth = screenWidth;
    	this.mScreenHeight = screenHeight;
    	
    	if (this.mIsLandscape) {
    		this.mScaleX = (screenWidth - (this.mDotGridPaddingLeft + this.mDotGridPaddingRight + this.mDotGridPaddingBottom)) / (this.mCellsWide * 1.0f);
    		this.mScaleY = (screenHeight - this.mDotGridPaddingTop) / (this.mCellsTall * 1.0f);
    	} else {
    		this.mScaleX = (screenWidth - (this.mDotGridPaddingLeft + this.mDotGridPaddingRight)) / (this.mCellsWide * 1.0f);
    		this.mScaleY = (screenHeight - (this.mDotGridPaddingTop + this.mDotGridPaddingBottom)) / (this.mCellsTall * 1.0f);
    	}
    	
    	//Calculate walls
    	this.mWalls.clear();
		
    	//Widget walls
    	for (final Rect widget : this.mWidgetLocations) {
			final float left = (widget.left * (this.mCellColumnSpacing + Game.CELLS_BETWEEN_COLUMN)) + Game.CELLS_BETWEEN_COLUMN;
			final float top = (widget.top * (this.mCellRowSpacing + Game.CELLS_BETWEEN_ROW)) + Game.CELLS_BETWEEN_ROW;
    		final float right = ((widget.right * (this.mCellColumnSpacing + Game.CELLS_BETWEEN_COLUMN)) + this.mCellColumnSpacing + Game.CELLS_BETWEEN_COLUMN);
    		final float bottom = ((widget.bottom * (this.mCellRowSpacing + Game.CELLS_BETWEEN_ROW)) + this.mCellRowSpacing + Game.CELLS_BETWEEN_ROW);
			
			this.mWalls.add(new RectF(left, top, right, bottom));
    	}
		
    	//Icon walls
    	for (int y = 0; y < this.mIconRows; y++) {
    		for (int x = 0; x < this.mIconCols; x++) {
    			boolean contained = false;
    			for (final Rect widget : this.mWidgetLocations) {
    				if (x >= widget.left && x <= widget.right && y >= widget.top && y <= widget.bottom) {
    					contained = true;
    					break;
    				}
    			}
    			if (contained) {
    				continue;
    			}
    			
    			final float left = (x * (this.mCellColumnSpacing + Game.CELLS_BETWEEN_COLUMN)) + Game.CELLS_BETWEEN_COLUMN;
    			final float top = (y * (this.mCellRowSpacing + Game.CELLS_BETWEEN_ROW)) + Game.CELLS_BETWEEN_ROW;
    			final float right = left + this.mCellColumnSpacing;
    			final float bottom = top + this.mCellRowSpacing;

    			this.mWalls.add(new RectF(left, top, right, bottom));
    		}
    	}
    	
    	if (Wallpaper.LOG_DEBUG) {
    		Log.d(Game.TAG, "Is Landscape: " + this.mIsLandscape);
    		Log.d(Game.TAG, "Screen Width: " + screenWidth);
    		Log.d(Game.TAG, "Screen Height: " + screenHeight);
    		Log.d(Game.TAG, "Scale X: " + this.mScaleX);
    		Log.d(Game.TAG, "Scale Y: " + this.mScaleY);
    	}

    	if (Wallpaper.LOG_VERBOSE) {
    		Log.v(Game.TAG, "< performResize()");
    	}
    }
    
    /**
     * Render the board and all entities on a Canvas.
     * 
     * @param c Canvas to draw on.
     */
    public void draw(final Canvas c) {
    	c.save();
    	
    	//Clear the screen in case of transparency in the image
		c.drawColor(this.mGameBackground);
    	if (this.mBackground != null) {
    		//Bitmap should already be sized to the screen so draw it at the origin
    		c.drawBitmap(this.mBackground, 0, 0, this.mBackgroundPaint);
    	}
        
        c.translate(this.mDotGridPaddingLeft, this.mDotGridPaddingTop);
        c.scale(this.mScaleX, this.mScaleY);
        
        //Draw dots and walls
        this.drawGameBoard(c);
        
        c.restore();
    }

    /**
     * Render the dots and walls.
     * 
     * @param c Canvas to draw on.
     */
    private void drawGameBoard(final Canvas c) {
    	//draw light cycle
    	for (final Point position : this.mPlayer) {
    		c.drawRect(position.x, position.y, position.x + 1, position.y + 1, this.mPlayerForeground);
    	}
    	//draw opponent
    	for (final Point position : this.mOpponent) {
    		c.drawRect(position.x, position.y, position.x + 1, position.y + 1, this.mOpponentForeground);
    	}
    	
        //draw walls if enabled
        if (this.mIsDisplayingWalls) {
        	for (final RectF wall : this.mWalls) {
        		c.drawRect(wall, this.mWallsForeground);
        	}
        }
    }

    

	/**
	 * Update the point one step in the direction specified.
	 * 
	 * @param point Point of original coordinates.
	 * @param direction Direction in which to move the point.
	 * @return New point coordinates.
	 */
    private static Point move(final Point point, final Game.Direction direction) {
    	final Point newPoint = new Point(point);
    	if (direction != null) {
	    	switch (direction) {
	    		case NORTH:
	    			newPoint.y -= 1;
					break;
					
	    		case SOUTH:
	    			newPoint.y += 1;
					break;
					
	    		case WEST:
	    			newPoint.x -= 1;
					break;
					
	    		case EAST:
	    			newPoint.x += 1;
					break;
	    	}
    	}
    	return newPoint;
    }
    
    /**
     * Determine whether two points represent the same coordinate.
     * 
     * @param one Point one.
     * @param two Point two.
     * @return Boolean.
     */
    private static boolean pointEquals(final Point one, final Point two) {
    	return ((one.x == two.x) && (one.y == two.y));
    }
}
