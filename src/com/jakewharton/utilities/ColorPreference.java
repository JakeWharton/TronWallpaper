package com.jakewharton.utilities;

import com.jakewharton.tronwallpaper.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * Preference for picking a color which is persisted as an integer.
 * 
 * @author Jake Wharton
 */
public class ColorPreference extends DialogPreference {
	/**
	 * Since we do not allow changing the alpha value, always use the maximum value of 255.
	 */
	private static final int ALPHA = 0xff;
	
	
	
	/**
	 * Color preview at top of dialog.
	 */
	private SurfaceView mPreview;
	
	/**
	 * Seek bar for the red color part.
	 */
	private SeekBar mR;
	
	/**
	 * Seek bar for the green color part.
	 */
	private SeekBar mG;
	
	/**
	 * Seek bar for the blue color part.
	 */
	private SeekBar mB;
	
	/**
	 * Value of the red seek bar.
	 */
	private TextView mRValue;
	
	/**
	 * Value of the green seek bar.
	 */
	private TextView mGValue;
	
	/**
	 * Value of the blue seek bar.
	 */
	private TextView mBValue;
	
	/**
	 * The color.
	 */
	private int mColor;
	
	/**
	 * Temporary color storage used for callback.
	 */
	private Integer mTempColor;
	
	/**
	 * Listener for any of the seek bar value changes.
	 */
	private final OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		public void onStopTrackingTouch(SeekBar seekBar) {}
		public void onStartTrackingTouch(SeekBar seekBar) {}
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			final int red = ColorPreference.this.mR.getProgress();
			final int green = ColorPreference.this.mG.getProgress();
			final int blue = ColorPreference.this.mB.getProgress();
			final int color = Color.argb(ColorPreference.ALPHA, red, green, blue);
			
			ColorPreference.this.mRValue.setText(Integer.toString(red));
			ColorPreference.this.mGValue.setText(Integer.toString(green));
			ColorPreference.this.mBValue.setText(Integer.toString(blue));
			
			ColorPreference.this.setValue(color);
		}
	};

	
	
	/**
	 * Create a new instance of the ColorPreference.
	 * 
	 * @param context Context.
	 * @param attrs Attributes.
	 */
	public ColorPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		
		this.setPersistent(true);
		this.setDialogLayoutResource(R.layout.color_preference);
	}
	
	
	
	@Override
	protected void onBindDialogView(final View view) {
		super.onBindDialogView(view);
		
		this.mPreview = (SurfaceView)view.findViewById(R.id.preview);
		this.mPreview.setBackgroundColor(this.mColor);
		
		this.mR = (SeekBar)view.findViewById(R.id.red);
		this.mR.setProgress(Color.red(this.mColor));
		this.mR.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
		this.mG = (SeekBar)view.findViewById(R.id.green);
		this.mG.setProgress(Color.green(this.mColor));
		this.mG.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
		this.mB = (SeekBar)view.findViewById(R.id.blue);
		this.mB.setProgress(Color.blue(this.mColor));
		this.mB.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
		
		this.mRValue = (TextView)view.findViewById(R.id.red_value);
		this.mRValue.setText(Integer.toString(Color.red(this.mColor)));
		this.mGValue = (TextView)view.findViewById(R.id.green_value);
		this.mGValue.setText(Integer.toString(Color.green(this.mColor)));
		this.mBValue = (TextView)view.findViewById(R.id.blue_value);
		this.mBValue.setText(Integer.toString(Color.blue(this.mColor)));
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(final boolean restore, final Object defaultValue) {
		final int color = this.getPersistedInt(defaultValue == null ? 0 : (Integer)defaultValue);
		this.mColor = color;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			this.mTempColor = this.mColor;
			if (this.callChangeListener(this.mTempColor)) {
				this.saveValue(this.mTempColor);
			}
		}
	}

	/**
	 * Set the value of the color and update the preview.
	 * 
	 * @param color Color value.
	 */
	public void setValue(final int color) {
		this.mColor = color;
		this.mPreview.setBackgroundColor(color);
	}
	
	/**
	 * Set and persist the value of the color.
	 * 
	 * @param color Color value.
	 */
	public void saveValue(final int color) {
		this.setValue(color);
		this.persistInt(color);
	}
}