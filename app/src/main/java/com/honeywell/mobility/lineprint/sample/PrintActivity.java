package com.honeywell.mobility.lineprint.sample;

import com.honeywell.mobility.print.LinePrinter;
import com.honeywell.mobility.print.LinePrinterException;
import com.honeywell.mobility.print.PrintProgressEvent;
import com.honeywell.mobility.print.PrintProgressListener;
import com.honeywell.mobility.print.PrinterException;

import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import java.io.*;
import java.lang.*;
import java.util.Random;

import android.view.inputmethod.InputMethodManager;
import android.content.Context;


/**
 * This sample demonstrates printing on an Android computer using the LinePrinter
 * class. You may enter or scan a Honeywell mobile printer's MAC address and
 * click the Print button to print. The MAC Address text should have the format
 * of "nn:nn:nn:nn:nn:nn" or "nnnnnnnnnnnn" where each n is a hex digit.
 * <p>
 * You may also capture a signature to print by clicking the Sign button. It
 * will display another screen for you to sign and save the signature. After
 * you save the signature, you will see a preview of the signature graphic
 * next to the Sign button.
 * <p>
 * The printing progress will be displayed in the Progress and Status text box.
 */
public class PrintActivity extends Activity {
    private static final boolean enableShortPrints = true;

	private Button buttonPrint;
	private Button buttonSign;
	private Button buttonReset;
	private TextView textMsg;
	private EditText editPrinterID;
	private EditText editMacAddr;
	private EditText editUserText;
	private EditText numberOfCopies;
	private EditText numberOfAditionalLines;
	private EditText delayBetweenCopies;
	private ImageView imgSignature;
	private String base64SignaturePng = null;
	private int numtries = 0;
	private int maxretry = 2;
	private	int copycounter=1;
	private InputMethodManager inputManager;
	private int delay;
	private	LinePrinter lp;



	public static final int CAPTURE_SIGNATURE_ACTIVITY = 1;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_print);


		textMsg = (TextView) findViewById(R.id.textMsg);

		inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

		editPrinterID = (EditText) findViewById(R.id.editPrinterID);
		// Set a default Printer ID
		editPrinterID.setText("PR3");

		editMacAddr = (EditText) findViewById(R.id.editMacAddr);
		// Set a default Mac Address
		//editMacAddr.setText("00:07:80:AB:EA:5D"); //Juan
        editMacAddr.setText("0c:a6:94:3a:24:2d");   //Josef

		numberOfCopies=(EditText) findViewById(R.id.inputNumberOfCopies);
		// Set a default Number Of copies to Print
		numberOfCopies.setText("1");

		numberOfAditionalLines=(EditText) findViewById(R.id.inputNumberOfAditionalLines);
		//numberOfAditionalLines.setText("1");
		numberOfAditionalLines.setText("1");

		delayBetweenCopies=(EditText)findViewById(R.id.DelayBetweenCopies);
		//OfDelayBetweenCopies
		delayBetweenCopies.setText(("100"));

		textMsg.setMovementMethod(new ScrollingMovementMethod());

		editUserText = (EditText) findViewById(R.id.editUserText);
		imgSignature = (ImageView) findViewById(R.id.imgSignature);

		copyAssetFiles();


		//Broadcast


		IntentFilter filter = new IntentFilter(
				"android.bluetooth.device.action.PAIRING_REQUEST");

        /*
         * Registering a new BTBroadcast receiver from the Main Activity context
         * with pairing request event
         */
		registerReceiver(new PairingRequest(), filter);

		buttonPrint = (Button) findViewById(R.id.buttonPrint);
		buttonPrint.setOnClickListener(new OnClickListener() {
			public void onClick(View view)
			{
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);

				// Create a PrintTask to do printing on a separate thread.
				PrintTask task = new PrintTask();

				// Executes PrintTask with the specified parameter which is passed
				// to the PrintTask.doInBackground method.
                String sNumCopies = numberOfCopies.getText().toString().replaceAll("[\\D]","");
                String sNumAdditionalLines=numberOfAditionalLines.getText().toString().replaceAll("[\\D]","");
                String sDelayBetweenCopies = delayBetweenCopies.getText().toString().replaceAll("[\\D]","");
                String sUserText = editUserText.getText().toString();
				task.execute(editPrinterID.getText().toString(),
                        editMacAddr.getText().toString(),
                        sNumCopies,
                        sNumAdditionalLines,
                        sDelayBetweenCopies,
                        sUserText);
			}
		});

		buttonSign = (Button) findViewById(R.id.buttonsign);
		buttonSign.setOnClickListener(new OnClickListener() {
			public void onClick(View view)
			{
				Intent intent = new Intent(PrintActivity.this, CaptureSignatureActivity.class);
				startActivityForResult(intent, CAPTURE_SIGNATURE_ACTIVITY);
			}
		});



	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.print, menu);
		return true;
	}

	private void copyAssetFiles()
	{
		InputStream input = null;
		OutputStream output = null;
		// Copy the asset files we delivered with the application to a location
		// where the LinePrinter can access them.
		try
		{
			AssetManager assetManager = getAssets();
			String[] files = { "printer_profiles.JSON", "honeywell_logo.bmp" };

			for (String filename : files)
			{
				input = assetManager.open(filename);
				File outputFile = new File(getExternalFilesDir(null), filename);

				output = new FileOutputStream(outputFile);

				byte[] buf = new byte[1024];
				int len;
				while ((len = input.read(buf)) > 0)
				{
					output.write(buf, 0, len);
				}
				input.close();
				input = null;

				output.flush();
				output.close();
				output = null;
			}
		}
		catch (Exception ex)
		{
			textMsg.append("Error copying asset files.");
		}
		finally
		{
			try
			{
				if (input != null)
				{
					input.close();
					input = null;
				}

				if (output != null)
				{
					output.close();
					output = null;
				}
			}
			catch (IOException e){}
		}
	}

	/**
	 * This exception is thrown by the background thread to halt printing attempts and
	 * return an error to the UI when the printer status indicates conditions that would
	 * prevent successful printing such as "lid open" or "paper out".
	 */
	public class BadPrinterStateException extends Exception
	{
		static final long serialVersionUID = 1;

		public BadPrinterStateException(String message)
		{
			super(message);
		}
	}

	/**
	 * This class demonstrates printing in a background thread and updates
	 * the UI in the UI thread.
	 */
	public class PrintTask extends AsyncTask<String, Integer, String> {
		private static final String PROGRESS_CANCEL_MSG = "Printing cancelled\n";
		private static final String PROGRESS_COMPLETE_MSG = "Printing completed\n";
		private static final String PROGRESS_ENDDOC_MSG = "End of document\n";
		private static final String PROGRESS_FINISHED_MSG = "Printer connection closed\n";
		private static final String PROGRESS_NONE_MSG = "Unknown progress message\n";
		private static final String PROGRESS_STARTDOC_MSG = "Start printing document\n";


		/**
		 * Runs on the UI thread before doInBackground(Params...).
		 */
		@Override
		protected void onPreExecute()
		{
			// Clears the Progress and Status text box.
			textMsg.setText("");

			// Disables the Print button.
			buttonPrint.setEnabled(false);
			// Disables the Sign button.
			buttonSign.setEnabled(false);

			// Shows a progress icon on the title bar to indicate
			// it is working on something.
			setProgressBarIndeterminateVisibility(true);
		}

		/**
		 * This method runs on a background thread. The specified parameters
		 * are the parameters passed to the execute method by the caller of
		 * this task. This method can call publishProgress to publish updates
		 * on the UI thread.
		 */
		@Override
		protected String doInBackground(String... args)
		{
			LinePrinter lp = null;
			String sResult = null;

            String sPrinterID = args[0];
			String sMacAddr = args[1];
			String sNumCopies = args[2];
            String sNumAdditionalLines=args[3];
            String sDelayBetweenCopies=args[4];
            String sUserText=args[5];

            int iDocNumber = 1;
			int copies=1,numberOfLinesToAdd=1;


			// Check number of copies is a valid number between 1-10; if not, use 1 as copy.
			int s=Integer.parseInt(sNumCopies);// (numberOfCopies.getText().toString().replaceAll("[\\D]",""));
			if ((1<=s)&&(s<=80))
			    {copies=s;}

			// Check number of lines is a valid number between 1-100; if not, use 1 as number of added lines.
			s=Integer.parseInt(sNumAdditionalLines);// (numberOfAditionalLines.getText().toString().replaceAll("[\\D]",""));
			if ((1<=s)&&(s<=100))
			    {numberOfLinesToAdd=s;}

			// Check if Delay Between copies is 50-50000; if not use 100ms as default
			s=Integer.parseInt(sDelayBetweenCopies);// (delayBetweenCopies.getText().toString().replaceAll("[\\D]",""));
			if ((50<=s)&&(s<=50000))
			    {delay=s;}

//			textMsg.setText(textMsg.getText().toString()+"Starting Print");


			if (sMacAddr.contains(":") == false && sMacAddr.length() == 12)
			{
				// If the MAC address only contains hex digits without the
				// ":" delimiter, then add ":" to the MAC address string.
				char[] cAddr = new char[17];

				for (int i=0, j=0; i < 12; i += 2)
				{
					sMacAddr.getChars(i, i+2, cAddr, j);
					j += 2;
					if (j < 17)
					{
						cAddr[j++] = ':';
					}
				}

				sMacAddr = new String(cAddr);
			}

			String sPrinterURI = "bt://" + sMacAddr;
			//String sUserText = editUserText.getText().toString();

			LinePrinter.ExtraSettings exSettings = new LinePrinter.ExtraSettings();

			exSettings.setContext(PrintActivity.this);

			PrintProgressListener progressListener =
				new PrintProgressListener()
				{
					public void receivedStatus(PrintProgressEvent aEvent)
					{
						// Publishes updates on the UI thread.
						publishProgress(aEvent.getMessageType());
					}
				};

			/// Repeat print
			for (copycounter=1; copycounter<=copies;copycounter++)
			{
                String sDocNumber = String.format("%010d", iDocNumber);// "1234567890";

				// Update status textbox with the number of copies
				if (copies>1)
				{
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							textMsg.setText(textMsg.getText().toString() + "Print copy number: " + String.valueOf(copycounter) + "\n");
						}
					});

				}

				try {
					File profiles = new File(getExternalFilesDir(null), "printer_profiles.JSON");
					numtries=0;
					try {
						lp = new LinePrinter(
								profiles.getAbsolutePath(),
								sPrinterID,
								sPrinterURI,
								exSettings);
					}
					catch (final LinePrinterException ex)
					{

						runOnUiThread(new Runnable() {
							@Override

							public void run() {
								textMsg.setText(textMsg.getText().toString() + "lp.open:_"+ex.toString());
							}
						});
					}
					runOnUiThread(new Runnable() {
						@Override

						public void run() {
							textMsg.setText(textMsg.getText().toString() + "Start Printing\n");
						}
					});

					// Registers to listen for the print progress events.
					lp.addPrintProgressListener(progressListener);

					//A retry sequence in case the bluetooth socket is temporarily not ready
					//int numtries = 0;
					//final int maxretry = 2;
					while (numtries < maxretry) {
						try {

							runOnUiThread(new Runnable() {
								@Override

								public void run() {
									textMsg.setText(textMsg.getText().toString() + "BT connection, try: " + String.valueOf(numtries+1) + "\n");
								}
							});

							lp.connect();  // Connects to the printer

							break;
						} catch (LinePrinterException ex) {

							numtries++;
							Thread.sleep(2000);
						}
					}
					if (numtries == maxretry) //Final retry
					{
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								textMsg.setText(textMsg.getText().toString() + "Final BT conntection, try: " + String.valueOf(numtries+1) + "\n");
							}
						});
						try {
							lp.connect();
						}
						catch (final LinePrinterException ex)
						{

							runOnUiThread(new Runnable() {
								@Override

								public void run() {
									textMsg.setText(textMsg.getText().toString() + "last lp.open:_"+ex.toString());
                                    copycounter=0;
								}
							});
						}

					}


					// Check the state of the printer and abort printing if there are
					// any critical errors detected.
					int[] results = lp.getStatus();
					if (results != null) {
						for (int err = 0; err < results.length; err++) {
							if (results[err] == 223) {
								// Paper out.
								throw new BadPrinterStateException("Paper out");
							} else if (results[err] == 227) {
								// Lid open.
								throw new BadPrinterStateException("Printer lid open");
							}

						}
						if (results.length==0)
						{
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									textMsg.setText(textMsg.getText().toString() + "Printer status: no errors before printing\n");
								}
							});
						}

					}

                    if(!enableShortPrints) {
                        // Prints the Honeywell logo graphic on the receipt if the graphic
                        // file exists.
                        File graphicFile = new File(getExternalFilesDir(null), "honeywell_logo.bmp");
                        if (graphicFile.exists()) {
                            lp.writeGraphic(graphicFile.getAbsolutePath(),
                                    LinePrinter.GraphicRotationDegrees.DEGREE_0,
                                    72,  // Offset in printhead dots from the left of the page
                                    200, // Desired graphic width on paper in printhead dots
                                    40); // Desired graphic height on paper in printhead dots
                        }
                        lp.newLine(1);
                    }
					// Set font style to Bold + Double Wide + Double High.
					lp.setBold(true);
					lp.setDoubleWide(true);
					lp.setDoubleHigh(true);
					lp.write("SALES ORDER");
					lp.setDoubleWide(false);
					lp.setDoubleHigh(false);
					lp.newLine(2);

					// The following text shall be printed in Bold font style.
					lp.write("CUSTOMER: Casual Step");
					lp.setBold(false);  // Returns to normal font.
					lp.newLine(2);

					// Set font style to Compressed + Double High.
					lp.setDoubleHigh(true);
					lp.setCompress(false);
					lp.write("DOCUMENT#: " + sDocNumber);
					lp.setCompress(false);
					lp.setDoubleHigh(false);
					lp.newLine(2);

					// The following text shall be printed in Normal font style.
					lp.write(" PRD. DESCRIPT.   PRC.  QTY.    NET.");
					lp.newLine(2);

					lp.write(" 1501 Timer-Md1  13.15     1   13.15");
					lp.newLine(1);
                    if(!enableShortPrints) {
                        lp.write(" 1502 Timer-Md2  13.15     3   39.45");
                        lp.newLine(1);
                        lp.write(" 1503 Timer-Md3  13.15     2   26.30");
                        lp.newLine(1);
                        lp.write(" 1504 Timer-Md4  13.15     4   52.60");
                        lp.newLine(1);
                        lp.write(" 1505 Timer-Md5  13.15     5   65.75");
                        lp.newLine(1);
                        lp.write("                        ----  ------");
                        lp.newLine(1);
                        lp.write("              SUBTOTAL    15  197.25");
                        lp.newLine(2);

                        lp.write("          5% State Tax          9.86");
                        lp.newLine(2);

                        lp.write("                              ------");
                        lp.newLine(1);
                        lp.write("           BALANCE DUE        207.11");
                        lp.newLine(1);
                        lp.newLine(1);
                    }
					lp.write(" PAYMENT TYPE: CASH");
					lp.newLine(2);

					lp.setDoubleHigh(true);
					lp.write("       SIGNATURE / STORE STAMP");
					lp.setDoubleHigh(false);
					lp.newLine(2);

                    if(!enableShortPrints) {
                        // Prints the captured signature if it exists.
                        if (base64SignaturePng != null) {
                            lp.writeGraphicBase64(base64SignaturePng,
                                    LinePrinter.GraphicRotationDegrees.DEGREE_0,
                                    72,   // Offset in printhead dots from the left of the page
                                    220,  // Desired graphic width on paper in printhead dots
                                    100); // Desired graphic height on paper in printhead dots
                        }
                        lp.newLine(1);
                        lp.setBold(true);
                        if (sUserText.length() > 0) {
                            // Print the text entered by user in the Optional Text field.
                            lp.write(sUserText);
                            lp.newLine(2);
                        }
                    }

					lp.write("          ORIGINAL");
					lp.setBold(false);
					lp.newLine(2);

                    if(!enableShortPrints) {
                        // Print a Code 39 barcode containing the document number.
                        lp.writeBarcode(LinePrinter.BarcodeSymbologies.SYMBOLOGY_CODE39,
                                sDocNumber,   // Document# to encode in barcode
                                90,           // Desired height of the barcode in printhead dots
                                40);          // Offset in printhead dots from the left of the page

                        lp.newLine(4);


                        // Adding extralines
                        for (int counter = 1; counter <= numberOfLinesToAdd; counter++) {
                            lp.write("Extra line added, number " + String.valueOf(counter));
                            lp.newLine(1);

                        }
                        lp.newLine(5);
                    }

					results = lp.getStatus();

					//Thread.sleep(2000);

					if (results != null) {
						for (int err = 0; err < results.length; err++) {
							if (results[err] == 223) {
								// Paper out.
								throw new BadPrinterStateException("Paper out");
							} else if (results[err] == 227) {
								// Lid open.
								throw new BadPrinterStateException("Printer lid open");
							}

						}
						if (results.length==0)
						{
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									textMsg.setText(textMsg.getText().toString() + "End of print\n");
								}
							});
						}
					}


					//sResult = "Number of bytes sent to printer: " + lp.getBytesWritten();
                    sResult="Dummy text";
				} catch (BadPrinterStateException ex) {					// Stop listening for printer events.
					lp.removePrintProgressListener(progressListener);
					sResult = "Printer error detected: " + ex.getMessage() + ". Please correct the error and try again.";
				} catch (LinePrinterException ex) {
					sResult = "LinePrinterException: " + ex.getMessage();


				} catch (Exception ex) {
					if (ex.getMessage() != null)
						sResult = "Unexpected exception: " + ex.getMessage();
					else
						sResult = "Unexpected exception.";
				} finally {
				if (lp != null) {
						try {
                            runOnUiThread(new Runnable() {
                                @Override

                                public void run() {
                                    textMsg.setText(textMsg.getText().toString() + "BT disconnection start xxxxx\n");
                                }
                            });
							lp.disconnect();  // Disconnects from the printer
							Thread.sleep(2000);
							runOnUiThread(new Runnable() {
								@Override

								public void run() {
									textMsg.setText(textMsg.getText().toString() + "BT disconnection\n");
								}
							});


							lp.close();  // Releases resources
							Thread.sleep(2000);



							runOnUiThread(new Runnable() {
								@Override

								public void run() {
									textMsg.setText(textMsg.getText().toString() + "BT object closed\n");
									//textMsg.setMovementMethod(new ScrollingMovementMethod());
								}
							});

							if ((copies>1)&&(copycounter<copies))
							{
                                iDocNumber++;
								// add extra Delay
								if (delay==0)
								{
									Random rand = new Random();

									// nextInt is normally exclusive of the top value,
									// so add 1 to make it inclusive
									final int randomNum = rand.nextInt((25000 - 100) + 1) + 100;



									runOnUiThread(new Runnable() {
										@Override

										public void run() {
											textMsg.setText(textMsg.getText().toString() + "Adding extra delay:" + String.valueOf(randomNum) + " ms\n");
											//textMsg.setMovementMethod(new ScrollingMovementMethod());


										}
									});
									Thread.sleep(randomNum);

								}
								else
								{


								runOnUiThread(new Runnable() {
									@Override

									public void run() {
										textMsg.setText(textMsg.getText().toString() + "Adding extra delay:" + String.valueOf(delay) + " ms\n");
										//textMsg.setMovementMethod(new ScrollingMovementMethod());

									}
								});
									Thread.sleep(delay);
								}
							}

						}

							catch (final Exception ex)
							{

								runOnUiThread(new Runnable() {
									@Override

									public void run() {
										textMsg.setText(textMsg.getText().toString() + "last lp.close or disconnect:_"+ex.toString());
									}
								});
							}


				 }
				}


			}

			// The result string will be passed to the onPostExecute method
			// for display in the the Progress and Status text box.
			return sResult;
		}

		/**
		 * Runs on the UI thread after publishProgress is invoked. The
		 * specified values are the values passed to publishProgress.
		 */
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			// Access the values array.
			int progress = values[0];

			switch (progress)
			{
			case PrintProgressEvent.MessageTypes.CANCEL:
				//textMsg.append(PROGRESS_CANCEL_MSG);
				break;
			case PrintProgressEvent.MessageTypes.COMPLETE:
				//textMsg.append(PROGRESS_COMPLETE_MSG);
				break;
			case PrintProgressEvent.MessageTypes.ENDDOC:
				//textMsg.append(PROGRESS_ENDDOC_MSG);
				break;
			case PrintProgressEvent.MessageTypes.FINISHED:
				//textMsg.append(PROGRESS_FINISHED_MSG);
				break;
			case PrintProgressEvent.MessageTypes.STARTDOC:
				//textMsg.append(PROGRESS_STARTDOC_MSG);
				break;
			default:
				//textMsg.append(PROGRESS_NONE_MSG);
				break;
			}
		}

		/**
		 * Runs on the UI thread after doInBackground method. The specified
		 * result parameter is the value returned by doInBackground.
		 */
		@Override
		protected void onPostExecute(String result)
		{
			// Displays the result (number of bytes sent to the printer or
			// exception message) in the Progress and Status text box.
			if (result != null)
			{
				textMsg.append(result);
			}

			// Dismisses the progress icon on the title bar.
			setProgressBarIndeterminateVisibility(false);

			// Enables the Print button.
			buttonPrint.setEnabled(true);
			// Enables the Sign button.
			buttonSign.setEnabled(true);
		}
	} //endofclass PrintTask

	/**
	 * Called when an activity launched by this activity exits.
	 * @param requestCode The integer request code originally supplied to
	 * startActivityForResult(), allowing you to identify who this result came from.
	 * @param resultCode The integer result code returned by the child activity
	 * through its setResult().
	 * @param data An Intent, which can return result data to the caller
	 * (various data can be attached to Intent "extras").
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Bundle extras;

		switch (requestCode)
		{
		case CAPTURE_SIGNATURE_ACTIVITY:
			if (RESULT_OK == resultCode)
			{
				// Gets the Base64 encoded PNG signature graphic.
				extras = data.getExtras();
				base64SignaturePng = extras.getString(CaptureSignatureActivity.BASE64_SIGNATURE_KEY);
				displaySignature(base64SignaturePng);
			}
			break;
		}
	}

	/**
	 * Displays the specified graphic in the imange view next to the Sign button.
	 * @param base64Png A Base64 encoded PNG image.
	 */
	private void displaySignature(String base64Png)
	{
		if (base64Png != null)
		{
			byte[] signPngBytes = Base64.decode(base64Png, Base64.DEFAULT);
			Bitmap signBitmap = BitmapFactory.decodeByteArray(signPngBytes, 0, signPngBytes.length);
			if (signBitmap != null)
			{
				imgSignature.setImageBitmap(signBitmap);
			}
			else
			{
				// Clears the image view.
				imgSignature.setImageDrawable(null);
			}
		}
		else
		{
			// Clears the image view.
			imgSignature.setImageDrawable(null);
		}
	}
}


