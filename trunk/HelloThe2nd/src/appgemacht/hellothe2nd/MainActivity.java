package appgemacht.hellothe2nd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;

//
// http://developer.android.com/training/basics/actionbar/setting-up.html
//   Adding the action bar when running on versions older than Android 3.0 (down to Android 2.1)
//   requires that you include the Android Support Library in your application.
//
public class MainActivity extends ActionBarActivity {
	public final static String EXTRA_MESSAGE = "appgemacht.HelloThe2nd.MESSAGE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	/** Called when the user clicks the Send button
	 * NOTE: after adding the method definition below,
	 *       you just need to press Ctrl+Shift+O to add
	 *       the 'import' statement for the 'view.View' class ! */
	public void sendMessage(View view) {
	    // Do something in response to button

	    Intent intent = new Intent(this, DisplayMessageActivity.class);
	    EditText editText = (EditText) findViewById(R.id.edit_message);
	    String message = editText.getText().toString();
	    intent.putExtra(EXTRA_MESSAGE, message);
	    startActivity(intent);
	}
}
