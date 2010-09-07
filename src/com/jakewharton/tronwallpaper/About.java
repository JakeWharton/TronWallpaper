package com.jakewharton.tronwallpaper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Activity which displays a web view with the contents loaded from an asset.
 * 
 * @author Jake Wharton
 */
public class About extends Activity {
	/**
	 * Filename of the asset to load.
	 */
	public static final String EXTRA_FILENAME = "filename";
	
	/**
	 * Title of the activity.
	 */
	public static final String EXTRA_TITLE = "title";
	
	/**
	 * Newline character to use between asset lines.
	 */
	private static final char NEWLINE = '\n';
	
	/**
	 * Error message displayed when the asset fails to load.
	 */
	private static final String ERROR = "Failed to load the file from assets.";
	
	/**
	 * Encoding of the assets.
	 */
	private static final String MIME_TYPE = "text/html";
	
	/**
	 * Character set of the assets.
	 */
	private static final String ENCODING = "utf-8";
	
	
	
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final StringBuffer content = new StringBuffer();
        
        try {
        	//Load entire about plain text from asset
			final BufferedReader about = new BufferedReader(new InputStreamReader(this.getAssets().open(this.getIntent().getStringExtra(About.EXTRA_FILENAME))));
			String data;
			while ((data = about.readLine()) != null) {
				content.append(data);
				content.append(About.NEWLINE);
			}
		} catch (IOException e) {
			e.printStackTrace();
			content.append(About.ERROR);
		}
		
		this.setTitle(this.getIntent().getStringExtra(About.EXTRA_TITLE));
		
		//Put text into layout
        final WebView view = new WebView(this);
		view.loadData(Uri.encode(content.toString()), About.MIME_TYPE, About.ENCODING);
		
		this.setContentView(view);
    }
}
