package com.honeywell.mobility.lineprint.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import java.io.*;

/**
 * Implements a custom view for signing.
 */
public class SignatureView extends View {

	private static final float STROKE_WIDTH = 6f;
	private Paint paint = new Paint(Paint.DITHER_FLAG);
	private Path path = new Path();
	private Bitmap signBitmap;
	private Canvas signCanvas;

	public SignatureView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(STROKE_WIDTH);
	}

	/**
	 * Gets a black and white signature bitmap.
	 * @return a black and white signature bitmap.
	 */
	public Bitmap getBlackWhiteBitmap()
	{
		Bitmap bwBitmap = Bitmap.createBitmap(signBitmap.getWidth(), signBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas bwCanvas = new Canvas(bwBitmap);

		//*****
		//* The original bitmap, signBitmap, has a transparent background. The
		//* following call draws a white background so the returned bitmap
		//* contains only black (signature) and white (background) colors.
		//*****
		bwCanvas.drawColor(Color.WHITE);
		bwCanvas.drawBitmap(signBitmap, 0, 0, paint);

		return bwBitmap;
	}
	/**
	 * Gets the signature bitmap in PNG format encoded in a Base64 string.
	 * @return a Base64 encoded string containing the signature in PNG format.
	 */
	public String getBase64EncodedPNG()
	{
		String base64Png = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try
		{
			if (getBlackWhiteBitmap().compress(Bitmap.CompressFormat.PNG, 100, output))
			{
				byte[] imgData = output.toByteArray();
				base64Png = Base64.encodeToString(imgData, Base64.DEFAULT);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return base64Png;
	}

	/**
	 * Saves the signature bitmap to the specified file in PNG format.
	 * @param aOutputFile The signature output file.
	 */
	public void saveSignaturePNG(File aOutputFile)
	{
		FileOutputStream output = null;
		try
		{
			android.util.Log.d("LinePrinterSample", "Signature file: " + aOutputFile.getAbsolutePath());

			output = new FileOutputStream(aOutputFile);
			signBitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
			output.flush();
			output.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Clears the signature view.
	 */
	public void clear()
	{
		signCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		path.reset();
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		signBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		signCanvas = new Canvas (signBitmap);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawBitmap(signBitmap, 0, 0, paint);
		canvas.drawPath(path, paint);
	}

	/**
	 * Handles touch screen motion events.
	 * @param event The motion event.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float eventX = event.getX();
		float eventY = event.getY();

		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			// Sets the beginning of the next contour to the point (eventX, eventY).
			path.moveTo(eventX, eventY);
			break;
		case MotionEvent.ACTION_MOVE:
			// Add a line from the last point to the the point (eventX, eventY).
			path.lineTo(eventX, eventY);
			break;
		case MotionEvent.ACTION_UP:
			// Use the signature canvas to draw the path which also updates
			// the underlying signBitmap.
			signCanvas.drawPath(path, paint);
			path.reset();
			break;
		default:
			return false;
		}

		invalidate();
		return true;
	}

}
