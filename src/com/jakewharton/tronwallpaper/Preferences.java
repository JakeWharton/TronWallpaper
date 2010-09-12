package com.jakewharton.tronwallpaper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.jakewharton.tronwallpaper.R;
import com.jakewharton.utilities.WidgetLocationsPreference;

/**
 * Settings activity for SnakeWallpaper
 * 
 * @author Jake Wharton
 */
public class Preferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	/**
	 * SharedPreference name.
	 */
	/*package*/static final String SHARED_NAME = "TronWallpaper";
	
	/**
	 * Filename of the change log.
	 */
	private static final String FILENAME_CHANGE_LOG = "changelog.html";
	
	/**
	 * Filename of the credits.
	 */
	private static final String FILENAME_CREDITS = "credits.html";
	
	/**
	 * Filename of the instructions.
	 */
	private static final String FILENAME_INSTRUCTIONS = "instructions.html";
	
	/**
	 * Filename of the to do list.
	 */
	private static final String FILENAME_TODO = "todo.html";
	
	/**
	 * Select background activity callback ID.
	 */
	private static final int SELECT_BACKGROUND = 1;
	
	
	
    @Override
    protected void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        
        final PreferenceManager manager = this.getPreferenceManager();
        manager.setSharedPreferencesName(Preferences.SHARED_NAME);
        this.addPreferencesFromResource(R.xml.preferences);
        
        final SharedPreferences preferences = manager.getSharedPreferences();
        final Resources resources = this.getResources();
        
        //reset display and game
        this.findPreference(resources.getString(R.string.settings_display_reset_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				(new AlertDialog.Builder(Preferences.this))
					.setMessage(resources.getString(R.string.reset_display))
					.setCancelable(false)
					.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, final int which) {
							Preferences.this.loadDisplayAndGameDefaults();
							
							Toast.makeText(Preferences.this, resources.getString(R.string.reset_display_toast), Toast.LENGTH_LONG).show();
						}
					})
					.setNegativeButton(resources.getString(R.string.no), null)
					.show();
				return true;
			}
		});
        
        //reset colors
        this.findPreference(resources.getString(R.string.settings_color_reset_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				(new AlertDialog.Builder(Preferences.this))
					.setMessage(resources.getString(R.string.reset_color))
					.setCancelable(false)
					.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, final int which) {
							Preferences.this.loadColorDefaults();
							
							Toast.makeText(Preferences.this, resources.getString(R.string.reset_color_toast), Toast.LENGTH_LONG).show();
						}
					})
					.setNegativeButton(resources.getString(R.string.no), null)
					.show();
				return true;
			}
		});
        
        //info email
        this.findPreference(resources.getString(R.string.information_contact_email_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.infoEmail();
				return true;
			}
		});
        
        //info twitter
        this.findPreference(resources.getString(R.string.information_contact_twitter_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.infoTwitter();
				return true;
			}
		});
        
        //info web
        this.findPreference(resources.getString(R.string.information_contact_website_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.infoWeb();
				return true;
			}
		});
        
        //info market
        this.findPreference(resources.getString(R.string.information_market_view_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.infoMarket();
				return true;
			}
		});
        
        //instructions
        this.findPreference(resources.getString(R.string.instructions_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.viewInstructions();
				return true;
			}
		});
        
        //change log
        this.findPreference(resources.getString(R.string.changelog_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.viewChangelog();
				return true;
			}
		});
        
        //credits
        this.findPreference(resources.getString(R.string.credits_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.viewCredits();
				return true;
			}
		});
        
        //todo
        this.findPreference(resources.getString(R.string.todo_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.viewTodo();
				return true;
			}
		});
        
        //github
        this.findPreference(resources.getString(R.string.github_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.viewGitHub();
				return true;
			}
		});
        
        //xda
        this.findPreference(resources.getString(R.string.xda_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Preferences.this.viewXda();
				return true;
			}
		});
        
        //background image
        this.findPreference(resources.getString(R.string.settings_color_bgimage_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), Preferences.SELECT_BACKGROUND);
				return true;
			}
		});
        
        //clear background image
        this.findPreference(resources.getString(R.string.settings_color_bgimageclear_key)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Preferences.this.getPreferenceManager().getSharedPreferences().edit().putString(resources.getString(R.string.settings_color_bgimage_key), null).commit();
				Toast.makeText(Preferences.this, R.string.settings_color_bgimageclear_toast, Toast.LENGTH_SHORT).show();
				return true;
			}
		});

        //Register as a preference change listener
        Wallpaper.PREFERENCES.registerOnSharedPreferenceChangeListener(this);
        this.onSharedPreferenceChanged(Wallpaper.PREFERENCES, null);
        
        //Check previously installed version
        final int thisVersion = resources.getInteger(R.integer.version_code);
        final int defaultVersion = resources.getInteger(R.integer.version_code_default);
        final int previousVersion = preferences.getInt(resources.getString(R.string.version_code_key), defaultVersion);
        if (previousVersion == defaultVersion) {
        	//First install
        	
        	//Store this version
        	this.getPreferenceManager().getSharedPreferences().edit().putInt(resources.getString(R.string.version_code_key), thisVersion).commit();
        	//Show hello
        	(new AlertDialog.Builder(this))
        		.setTitle(resources.getString(R.string.title))
        		.setMessage(resources.getString(R.string.welcome_firstrun))
        		.setCancelable(true)
        		.setPositiveButton(resources.getString(R.string.yes), new OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						Preferences.this.viewInstructions();
					}
				})
				.setNegativeButton(resources.getString(R.string.no), null)
				.show();
        } else if (previousVersion < thisVersion) {
        	//First run after upgrade
        	
        	//Store this version
        	this.getPreferenceManager().getSharedPreferences().edit().putInt(resources.getString(R.string.version_code_key), thisVersion).commit();
        	//Show hello
        	(new AlertDialog.Builder(this))
        		.setTitle(resources.getString(R.string.title))
        		.setMessage(resources.getString(R.string.welcome_upgrade))
        		.setCancelable(true)
        		.setPositiveButton(resources.getString(R.string.yes), new OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						Preferences.this.viewChangelog();
					}
				})
				.setNegativeButton(resources.getString(R.string.no), null)
				.show();
        }
    }
    
    /**
     * Open change log.
     */
    private void viewChangelog() {
		final Intent intent = new Intent(this, About.class);
		intent.putExtra(About.EXTRA_FILENAME, Preferences.FILENAME_CHANGE_LOG);
		intent.putExtra(About.EXTRA_TITLE, this.getResources().getString(R.string.changelog_title));
		this.startActivity(intent);
    }
    
    /**
     * Open instructions.
     */
    private void viewInstructions() {
		final Intent intent = new Intent(this, About.class);
		intent.putExtra(About.EXTRA_FILENAME, Preferences.FILENAME_INSTRUCTIONS);
		intent.putExtra(About.EXTRA_TITLE, this.getResources().getString(R.string.instructions_title));
		this.startActivity(intent);
    }
    
    /**
     * Open credits
     */
    private void viewCredits() {
		final Intent intent = new Intent(this, About.class);
		intent.putExtra(About.EXTRA_FILENAME, Preferences.FILENAME_CREDITS);
		intent.putExtra(About.EXTRA_TITLE, this.getResources().getString(R.string.credits_title));
		this.startActivity(intent);
    }
    
    /**
     * Open todo
     */
    private void viewTodo() {
		final Intent intent = new Intent(this, About.class);
		intent.putExtra(About.EXTRA_FILENAME, Preferences.FILENAME_TODO);
		intent.putExtra(About.EXTRA_TITLE, this.getResources().getString(R.string.todo_title));
		this.startActivity(intent);
    }
    
    /**
     * Open GitHub
     */
    private void viewGitHub() {
    	final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(this.getResources().getString(R.string.github_href)));
		
		this.startActivity(intent);
    }
    
    /**
     * Open XDA
     */
    private void viewXda() {
    	final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(this.getResources().getString(R.string.xda_href)));
		
		this.startActivity(intent);
    }

	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		final boolean all = (key == null);
		final Resources resources = this.getResources();
		
		//Only enable clear bg image when a bg image is set
		final String bgimage = resources.getString(R.string.settings_color_bgimage_key);
		if (all || key.equals(bgimage)) {
			final boolean enabled = preferences.getString(bgimage, null) != null;
			this.findPreference(resources.getString(R.string.settings_color_bgimageclear_key)).setEnabled(enabled);
			this.findPreference(resources.getString(R.string.settings_color_bgopacity_key)).setEnabled(enabled);
		}
		
		//If the icon rows or cols are explicitly changed then clear the widget locations
		final String iconRows = resources.getString(R.string.settings_display_iconrows_key);
		final String iconCols = resources.getString(R.string.settings_display_iconcols_key);
		if (all || key.equals(iconRows) || key.equals(iconCols)) {
			final int rows = preferences.getInt(iconRows, resources.getInteger(R.integer.display_iconrows_default));
			final int cols = preferences.getInt(iconCols, resources.getInteger(R.integer.display_iconcols_default));
			final String widgetLocations = resources.getString(R.string.settings_display_widgetlocations_key);
			
			if (!all) {
				//Clear any layouts
				preferences.edit().putString(widgetLocations, resources.getString(R.string.display_widgetlocations_default)).commit();
			}
			
			//Update with counts
			((WidgetLocationsPreference)this.findPreference(widgetLocations)).setIconCounts(rows, cols);
		}
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.getMenuInflater().inflate(R.menu.preferences, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Resources resources = this.getResources();
		
		switch (item.getItemId()) {
			case R.id.menu_reset:
				(new AlertDialog.Builder(this))
					.setMessage(resources.getString(R.string.reset_all))
					.setCancelable(false)
					.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Preferences.this.loadDisplayAndGameDefaults();
							Preferences.this.loadColorDefaults();
							
							Toast.makeText(Preferences.this, resources.getString(R.string.reset_all_toast), Toast.LENGTH_LONG).show();
						}
					})
					.setNegativeButton(resources.getString(R.string.no), null)
					.show();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			final Resources resources = this.getResources();
			
			switch (requestCode) {
				case Preferences.SELECT_BACKGROUND:
					//Store the string value of the background image
				    this.getPreferenceManager().getSharedPreferences().edit().putString(resources.getString(R.string.settings_color_bgimage_key), data.getDataString()).commit();
				    Toast.makeText(this, R.string.settings_color_bgimage_toast, Toast.LENGTH_SHORT).show();
					break;
					
				default:
					super.onActivityResult(requestCode, resultCode, data);
			}
		}
	}

	/**
	 * Launch an intent to send an email.
	 */
	private void infoEmail() {
        final Resources resources = this.getResources();
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("plain/text");
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] { resources.getString(R.string.information_contact_email_data) });
		intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.title));
		
		this.startActivity(intent);
    }
    
	/**
	 * Launch an intent to view twitter page.
	 */
    private void infoTwitter() {
        final Resources resources = this.getResources();
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(resources.getString(R.string.information_contact_twitter_data)));
		
		this.startActivity(intent);
    }
    
    /**
     * Launch an intent to view website.
     */
    private void infoWeb() {
        final Resources resources = this.getResources();
    	final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(resources.getString(R.string.information_contact_website_data)));
		
		this.startActivity(intent);
    }
    
    /**
     * Launch an intent to view other market applications.
     */
    private void infoMarket()
    {
        final Resources resources = this.getResources();
    	final Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(resources.getString(R.string.information_market_view_data)));
		
		this.startActivity(intent);
    }
    
    /**
     * Reset display preferences to their defaults.
     */
    private void loadDisplayAndGameDefaults() {
        final Resources resources = this.getResources();
	    final SharedPreferences.Editor editor = Preferences.this.getPreferenceManager().getSharedPreferences().edit();

		//user controllable
		editor.remove(resources.getString(R.string.settings_game_usercontrol_key));
		//fps
		editor.remove(resources.getString(R.string.settings_display_fps_key));
		//show walls
		editor.remove(resources.getString(R.string.settings_display_showwalls_key));
		//icon rows
		editor.remove(resources.getString(R.string.settings_display_iconrows_key));
		//icon cols
		editor.remove(resources.getString(R.string.settings_display_iconcols_key));
		//icon row spacing
		editor.remove(resources.getString(R.string.settings_display_rowspacing_key));
		//icon col spacing
		editor.remove(resources.getString(R.string.settings_display_colspacing_key));
		//widget locations
		editor.remove(resources.getString(R.string.settings_display_widgetlocations_key));
		//padding top
		editor.remove(resources.getString(R.string.settings_display_padding_top_key));
		//padding bottom
		editor.remove(resources.getString(R.string.settings_display_padding_bottom_key));
		//padding left
		editor.remove(resources.getString(R.string.settings_display_padding_left_key));
		//padding right
		editor.remove(resources.getString(R.string.settings_display_padding_right_key));
	
		editor.commit();
    }
    
    /**
     * Reset color preferences to their defaults.
     */
    private void loadColorDefaults() {
        final Resources resources = this.getResources();
		final SharedPreferences.Editor editor = Preferences.this.getPreferenceManager().getSharedPreferences().edit();

		//background
		editor.remove(resources.getString(R.string.settings_color_background_key));
		//walls
		editor.remove(resources.getString(R.string.settings_color_walls_key));
		//background image
		editor.remove(resources.getString(R.string.settings_color_bgimage_key));
		//background opacity
		editor.remove(resources.getString(R.string.settings_color_bgopacity_key));
		//player
		editor.remove(resources.getString(R.string.settings_color_player_key));
		//opponent
		editor.remove(resources.getString(R.string.settings_color_opponent_key));
		
		editor.commit();
    }
}
