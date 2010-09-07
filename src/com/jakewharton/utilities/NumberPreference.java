package com.jakewharton.utilities;

import com.jakewharton.tronwallpaper.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Preference which displays a seek bar for the selection of an integer.
 * 
 * @author Jake Wharton
 */
public class NumberPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
	/**
	 * The seek bar.
	 */
	private SeekBar mSeekBar;
	
	/**
	 * Representation of the value.
	 */
	private TextView mValueText;
	
	/**
	 * A suffix to append to the value for display.
	 */
	private String mSuffix;
	
	/**
	 * Maximum value.
	 */
	private int mMax;
	
	/**
	 * Minimum value.
	 */
	private int mMin;
	
	/**
	 * The current value.
	 */
	private int mValue = 0;

	
	
	/**
	 * Create a new instance of the NumberPreference.
	 * 
	 * @param context Context.
	 * @param attrs Attributes.
	 */
	public NumberPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		this.setPersistent(true);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPreference, 0, 0);
		this.mSuffix = a.getString(R.styleable.NumberPreference_suffix);
		this.mMin = a.getInt(R.styleable.NumberPreference_min, 0);
		this.mMax = a.getInt(R.styleable.NumberPreference_max, 100);

		this.setDialogLayoutResource(R.layout.number_preference);
	}

	
	
	@Override
	protected void onBindDialogView(final View view) {
		super.onBindDialogView(view);
		
		((TextView)view.findViewById(R.id.dialogMessage)).setText(this.getDialogMessage());

		this.mValueText = (TextView)view.findViewById(R.id.actualValue);

		this.mSeekBar = (SeekBar)view.findViewById(R.id.myBar);
		this.mSeekBar.setOnSeekBarChangeListener(this);
		this.mSeekBar.setMax(this.mMax - this.mMin);
		this.mSeekBar.setProgress(this.mValue - this.mMin);

		final String t = String.valueOf(this.mValue);
		this.mValueText.setText(this.mSuffix == null ? t : t.concat(this.mSuffix));
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(final boolean restore, final Object defaultValue) {
		this.mValue = this.getPersistedInt(defaultValue == null ? 0 : (Integer)defaultValue);
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			final int value = this.mSeekBar.getProgress() + this.mMin;
			if (this.callChangeListener(value)) {
				this.saveValue(value);
			}
		}
	}

	/**
	 * Set the value.
	 * 
	 * @param value Value.
	 */
	public void setValue(int value) {
		if (value > this.mMax) {
			value = this.mMax;
		} else if (value < this.mMin) {
			value = this.mMin;
		}
		this.mValue = value;
	}
	
	/**
	 * Set and persist the value.
	 * 
	 * @param value Value.
	 */
	private void saveValue(final int value) {
		this.setValue(value);
		this.persistInt(value);
	}

	/**
	 * Set the maximum possible value.
	 * 
	 * @param max Maximum value.
	 */
	public void setMax(final int max) {
		this.mMax = max;
		if (this.mValue > this.mMax) {
			this.setValue(this.mMax);
		}
	}

	/**
	 * Set the minimum possible value.
	 * 
	 * @param min Minimum value.
	 */
	public void setMin(final int min) {
		if (min < this.mMax) {
			this.mMin = min;
		}
	}

	/**
	 * Called when the seek bar value is changed.
	 */
	public void onProgressChanged(final SeekBar seek, final int value, final boolean fromTouch) {
		final String t = String.valueOf(value + this.mMin);
		this.mValueText.setText(this.mSuffix == null ? t : t.concat(this.mSuffix));
	}

	/**
	 * Called when the seek bar has started to changed. Not used.
	 */
	public void onStartTrackingTouch(final SeekBar seek) {}
	
	/**
	 * Called when the seek bar has stopped changing. Not used.
	 */
	public void onStopTrackingTouch(final SeekBar seek) {}
}