package com.honeywell.mobility.lineprint.sample;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.*;

/**
 * Implements the UI for the user to sign and save the signature.
 */
public class CaptureSignatureActivity extends Activity {
	public static final String BASE64_SIGNATURE_KEY = "com.honeywell.mobility.lineprint.sample.Base64Signature";
	private SignatureView signView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture_signature);
		signView = (SignatureView) findViewById(R.id.viewSignature);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.capture_signature, menu);
		return true;
	}

	/**
	 * Called when the Save button is clicked. This method saves the current
	 * signature in PNG format encoded in Base64. It delivers the result
	 * containing the Base64 string via an Intent which is received by
	 * the PrintActivity.onActivityResult method.
	 * @param view The view where the click event occurs.
	 */
	public void onSaveButtonClicked (View view)
	{
		String base64Png = signView.getBase64EncodedPNG();

		Intent intent = new Intent();
		if (base64Png != null)
		{
			intent.putExtra(BASE64_SIGNATURE_KEY, base64Png);
		}

		setResult(RESULT_OK,intent);
		finish();
	}

	public void onClearButtonClicked (View view)
	{
		signView.clear();
	}

	public void onCancelButtonClicked (View view)
	{
		Intent intent = new Intent();
		setResult(RESULT_CANCELED,intent);
		finish();
	}

}
