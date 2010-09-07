/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jakewharton.utilities;

import com.jakewharton.tronwallpaper.R;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * A simple Preference which displays an icon next to the text.
 * 
 * @author Jake Wharton
 */
public class IconPreference extends Preference {
	/**
	 * The icon.
	 */
    private Drawable mIcon;

    
    
    /**
     * Create a new instance of the IconPreference.
     * 
     * @param context Context.
     * @param attrs Attributes.
     */
    public IconPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Create a new instance of the IconPreference.
     * 
     * @param context Context.
     * @param attrs Attributes.
     * @param defStyle Style.
     */
    public IconPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        
        this.setLayoutResource(R.layout.icon_preference);
        
        this.mIcon = context.obtainStyledAttributes(attrs, R.styleable.IconPreference, defStyle, 0).getDrawable(R.styleable.IconPreference_icon);
    }

    
    
    @Override
    public void onBindView(final View view) {
        super.onBindView(view);
        
        final ImageView imageView = (ImageView)view.findViewById(R.id.icon);
        if ((imageView != null) && (this.mIcon != null)) {
            imageView.setImageDrawable(this.mIcon);
        }
    }

    /**
     * Sets the icon for this Preference with a Drawable.
     *
     * @param icon The icon for this Preference
     */
    public void setIcon(final Drawable icon) {
        if (((icon == null) && (this.mIcon != null)) || ((icon != null) && (!icon.equals(this.mIcon)))) {
            this.mIcon = icon;
            this.notifyChanged();
        }
    }

    /**
     * Returns the icon of this Preference.
     *
     * @return The icon.
     * @see #setIcon(Drawable)
     */
    public Drawable getIcon() {
        return this.mIcon;
    }
}