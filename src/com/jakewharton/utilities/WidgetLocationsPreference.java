package com.jakewharton.utilities;

import java.util.LinkedList;
import java.util.List;
import com.jakewharton.tronwallpaper.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

/**
 * Dialog preference which allows for the selection of the locations of launcher widgets.
 * 
 * @author Jake Wharton
 */
public class WidgetLocationsPreference extends DialogPreference {
	/**
	 * Tag used for logging.
	 */
	private static final String LOG = "WidgetLocationsPreference";
	
	/**
	 * Number of numbers stored in a rectangle (L, R, T, B).
	 */
	private static final int RECTANGLE_LENGTH = 4;
	
	/**
	 * Offset from the sides of the dialog.
	 */
	private static final int PADDING = 10;
	
	
	
	/**
	 * Widget location view. 
	 */
	private WidgetLocatorView mView;
	
	/**
	 * The string representation of the locations.
	 */
	private String mValue;
	
	/**
	 * The string representation of the locations used for the save callback.
	 */
	private String mTempValue;
	
	/**
	 * Number of icon rows on the launcher.
	 */
	private int mIconRows;
	
	/**
	 * Number of icon columns on the launcher.
	 */
	private int mIconCols;

	
	
	/**
	 * Create a new instance of the WidgetLocationsPreference.
	 * 
	 * @param context Context.
	 * @param attrs Attributes.
	 */
	public WidgetLocationsPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		this.setPersistent(true);
	}

	
	
	@Override
	protected View onCreateDialogView() {
		final Context context = this.getContext();
		final LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(WidgetLocationsPreference.PADDING, WidgetLocationsPreference.PADDING, WidgetLocationsPreference.PADDING, WidgetLocationsPreference.PADDING);
		
		final TextView text = new TextView(context);
		text.setText(R.string.widgetlocations_howto);
		layout.addView(text, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		this.mView = new WidgetLocatorView(context, this.mIconRows, this.mIconCols, this.mValue);
		layout.addView(this.mView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		return layout;
	}
	
	/**
	 * Update the number of icon rows and columns on the launcher.
	 * 
	 * @param iconRows Number of rows.
	 * @param iconCols Number of columns.
	 */
	public void setIconCounts(final int iconRows, final int iconCols) {
		this.mIconRows = iconRows;
		this.mIconCols = iconCols;
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(final boolean restore, final Object defaultValue) {
		this.mValue = this.getPersistedString(defaultValue == null ? "" : (String)defaultValue);
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			this.mTempValue = this.mValue;
			if (this.callChangeListener(this.mTempValue)) {
				this.saveValue(this.mTempValue);
			}
		}
	}

	/**
	 * Set and persist the string representation of the widget locations.
	 * @param value
	 */
	private void saveValue(final String value) {
		this.setValue(value);
		this.persistString(value);
	}
	
	/**
	 * Set the string representation of the widget locations.
	 * @param value
	 */
	private void setValue(final String value) {
		this.mValue = value;
	}
	
	
	
	/**
	 * Convert a persisted string value to the actual widget locations.
	 * 
	 * @param string Persisted string.
	 * @return List of Rects where the widgets are located.
	 */
	public static List<Rect> convertStringToWidgetList(final String string) {
		final List<Rect> list = new LinkedList<Rect>();
		
		if ((string.length() % WidgetLocationsPreference.RECTANGLE_LENGTH) != 0) {
			throw new IllegalArgumentException("String length must be a multiple of four.");
		}
		
		int i = 0;
		while (i < string.length()) {
			try {
				final Rect r = new Rect();
				r.left = Integer.parseInt(String.valueOf(string.charAt(i)));
				r.top = Integer.parseInt(String.valueOf(string.charAt(i+1)));
				r.right = Integer.parseInt(String.valueOf(string.charAt(i+2)));
				r.bottom = Integer.parseInt(String.valueOf(string.charAt(i+3)));
				list.add(r);
			} catch (NumberFormatException e) {
				Log.w(WidgetLocationsPreference.LOG, "Invalid rectangle: " + string.substring(i, WidgetLocationsPreference.RECTANGLE_LENGTH));
			} finally {
				i += WidgetLocationsPreference.RECTANGLE_LENGTH;
			}
		}
		
		return list;
	}
	
	
	
	/**
	 * View which allows for the selecting of widget locations
	 * 
	 * @author Jake Wharton
	 */
	private class WidgetLocatorView extends View {
		/**
		 * Offset from the sides of the view.
		 */
		private static final float OFFSET = 5;
		
		
		
		/**
		 * Location at which the current widget location begins.
		 */
		private Point mTouchStart;
		
		/**
		 * Location at which the current widget location ends.
		 */
		private Point mTouchEnd;
		
		/**
		 * Number of icon rows to display.
		 */
		private final int mRows;
		
		/**
		 * Number of icon columns to display.
		 */
		private final int mCols;
		
		/**
		 * The width of a single icon.
		 */
		private float mIconWidth;
		
		/**
		 * The height of a single icon.
		 */
		private float mIconHeight;
		
		/**
		 * The width of the virtual screen on the view.
		 */
		private float mWidth;
		
		/**
		 * The width of the virtual screen on the view.
		 */
		private float mHeight;
		
		/**
		 * Paint used to draw the icon divider lines.
		 */
		private final Paint mLine;
		
		/**
		 * Paint used to draw the current widget.
		 */
		private final Paint mDrawing;
		
		/**
		 * Paint used to draw the existing widgets.
		 */
		private final Paint mWidget;
		
		/**
		 * List of current existing widget locations.
		 */
		private final List<Rect> mWidgets;
		
		/**
		 * Detect long-presses on the view.
		 */
		private final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
			public boolean onSingleTapUp(MotionEvent e) { return false; }
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
			public boolean onDown(MotionEvent e) { return false; }
			public void onShowPress(MotionEvent e) {}
			public void onLongPress(MotionEvent e) {
				WidgetLocatorView.this.delete();
			}
		});
		
		
		
		/**
		 * Create a new instance of the WidgetLocatorView.
		 * 
		 * @param context Context.
		 * @param rows Number of icon rows.
		 * @param cols Number of icon columns.
		 * @param value Persisted value of widget location representation.
		 */
		public WidgetLocatorView(final Context context, final int rows, final int cols, final String value) {
			super(context);
			
			this.mRows = rows;
			this.mCols = cols;
			
			this.mLine = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mLine.setColor(Color.GRAY);
			this.mLine.setStrokeWidth(2);
			
			this.mDrawing = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mDrawing.setColor(Color.RED);
			this.mDrawing.setStyle(Paint.Style.STROKE);
			
			this.mWidget = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.mWidget.setColor(Color.GREEN);
			this.mWidget.setStyle(Paint.Style.STROKE);
			
			this.mWidgets = WidgetLocationsPreference.convertStringToWidgetList(value);
		}

		
		
		@Override
		protected void onDraw(final Canvas c) {
			c.save();
			c.translate(WidgetLocatorView.OFFSET, WidgetLocatorView.OFFSET);
			
			//Draw lines
			for (int row = 0; row <= this.mRows; row++) {
				final float rowPosition = row * this.mIconHeight;
				c.drawLine(0, rowPosition, this.mWidth, rowPosition, this.mLine);
			}
			for (int col = 0; col <= this.mCols; col++) {
				final float colPosition = col * this.mIconWidth;
				c.drawLine(colPosition, 0, colPosition, this.mHeight, this.mLine);
			}
			
			final float iconWidthOverTwo = this.mIconWidth / 2.0f;
			final float iconHeightOverTwo = this.mIconHeight / 2.0f;
			final float offset = ((this.mIconHeight < this.mIconWidth) ? this.mIconHeight : this.mIconWidth) / 4.0f;
			
			//Saved widgets
			for (final Rect widget : this.mWidgets) {
				final float left = (widget.left * this.mIconWidth) + iconWidthOverTwo - offset;
				final float right = (widget.right * this.mIconWidth) + iconWidthOverTwo + offset;
				final float top = (widget.top * this.mIconHeight) + iconHeightOverTwo - offset;
				final float bottom = (widget.bottom * this.mIconHeight) + iconHeightOverTwo + offset;
				
				c.drawRect(left, top, right, bottom, this.mWidget);
				c.drawLine(left, top, right, bottom, this.mWidget);
				c.drawLine(left, bottom, right, top, this.mWidget);
			}
			
			//Currently drawing widget
			if (this.mTouchStart != null) {
				final Rect pointRect = this.toRectangle();
				final float left = (pointRect.left * this.mIconWidth) + iconWidthOverTwo - offset;
				final float right = (pointRect.right * this.mIconWidth) + iconWidthOverTwo + offset;
				final float top = (pointRect.top * this.mIconHeight) + iconHeightOverTwo - offset;
				final float bottom = (pointRect.bottom * this.mIconHeight) + iconHeightOverTwo + offset;
				
				c.drawRect(left, top, right, bottom, this.mDrawing);
			}
			
			c.restore();
		}

		@Override
		protected void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
			super.onSizeChanged(width, height, oldWidth, oldHeight);
			
			this.mWidth = width - (2 * WidgetLocatorView.OFFSET);
			this.mHeight = height - (2 * WidgetLocatorView.OFFSET);
			this.mIconWidth = this.mWidth / (1.0f * this.mCols);
			this.mIconHeight = this.mHeight / (1.0f * this.mRows);
		}

		@Override
		public boolean onTouchEvent(final MotionEvent event) {
			if (this.gestureDetector.onTouchEvent(event)) {
				return true;
			}
			
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					this.mTouchStart = this.mTouchEnd = this.getPoint(event.getX(), event.getY());
					
					this.invalidate();
					return true;
					
				case MotionEvent.ACTION_MOVE:
					this.mTouchEnd = this.getPoint(event.getX(), event.getY());
					
					this.invalidate();
					return true;
					
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					this.mTouchEnd = this.getPoint(event.getX(), event.getY());
					this.add();
					
					this.mTouchStart = null;
					this.mTouchEnd = null;
					
					this.invalidate();
					return true;
					
				default:
					return super.onTouchEvent(event);
			}
		}
		
		/**
		 * Add a new widget using the two touch point locations as corners.
		 */
		private void add() {
			final Rect newWidget = this.toRectangle();
			final Rect insetWidget = new Rect(newWidget);
			
			//This is so that intersect returns true if they are actually adjacent
			insetWidget.inset(-1, -1);
			
			for (final Rect widget : this.mWidgets) {
				if (Rect.intersects(widget, insetWidget)) {
					return;
				}
			}
			
			if ((newWidget.height() == 0) && (newWidget.width() == 0)) {
				return;
			}
			
			this.mWidgets.add(newWidget);
			this.save();
		}
		
		/**
		 * Delete a widget at the long-pressed poisiton (if it exists).
		 */
		private void delete() {
			for (final Rect widget : this.mWidgets) {
				if ((this.mTouchEnd.x >= widget.left) && (this.mTouchEnd.x <= widget.right) && (this.mTouchEnd.y >= widget.top) && (this.mTouchEnd.y <= widget.bottom)) {
					this.mWidgets.remove(widget);
					break;
				}
			}
			this.save();
			this.invalidate();
		}
		
		/**
		 * Save the value to the parent instance.
		 */
		private void save() {
			final StringBuilder builder = new StringBuilder();
			for (final Rect widget : this.mWidgets) {
				builder.append(Integer.toString(widget.left));
				builder.append(Integer.toString(widget.top));
				builder.append(Integer.toString(widget.right));
				builder.append(Integer.toString(widget.bottom));
			}
			WidgetLocationsPreference.this.setValue(builder.toString());
		}
		
		/**
		 * Get the icon location Point from the current pixel coordinates.
		 * 
		 * @param x X coordinate.
		 * @param y Y coordinate.
		 * @return Icon location Point.
		 */
		private Point getPoint(float x, float y) {
			x -= WidgetLocatorView.OFFSET;
			y -= WidgetLocatorView.OFFSET;
			int newX = (int)(x / this.mIconWidth);
			int newY = (int)(y / this.mIconHeight);
			
			if (newX < 0) {
				newX = 0;
			} else if (newX >= this.mCols) {
				newX = this.mCols - 1;
			}
			if (newY < 0) {
				newY = 0;
			} else if (newY >= this.mRows) {
				newY = this.mRows - 1;
			}
			
			return new Point(newX, newY);
		}
		
		/**
		 * Convert the two touch Points to a Rect.
		 * 
		 * @return Rect with corners at the two touch points.
		 */
		private Rect toRectangle() {
			final boolean isStartXSmaller = (this.mTouchStart.x < this.mTouchEnd.x);
			final boolean isStartYSmaller = (this.mTouchStart.y < this.mTouchEnd.y);
			
			final Rect r = new Rect();
			r.left = isStartXSmaller ? this.mTouchStart.x : this.mTouchEnd.x;
			r.right = isStartXSmaller ? this.mTouchEnd.x : this.mTouchStart.x;
			r.top = isStartYSmaller ? this.mTouchStart.y : this.mTouchEnd.y;
			r.bottom = isStartYSmaller ? this.mTouchEnd.y : this.mTouchStart.y;
			
			return r;
		}
	}
}
