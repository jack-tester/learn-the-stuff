package appgemacht.hellothe2nd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Get the message from the intent
		// (Every Activity is invoked by an Intent and the data contained with
		//  it can be retrieved via getIntent() )
	    Intent intent = getIntent();
	    String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		
	    // Create the text view
	    TextView textView = new TextView(this);
	    textView.setTextSize(40);
	    textView.setText(message);

		// Set the text view as the activity layout
		setContentView(textView);//R.layout.activity_display_message);
		
		// Make sure we're running on Honeycomb or higher to use ActionBar APIs
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		//	if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
		//		// Show the Up button in the action bar.
		//		getActionBar().setDisplayHomeAsUpEnabled(true);
		//	}
		//}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_message, menu);
		return true;
	}

	// don't know yet, what to do with this ... let it in just ...
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
