package com.camerasudokusolver;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import com.camerasudokusolver.Utils.Constants;

//----------------------------------------------------------------------

public class StartActivity extends Activity {
	private CameraPreview mPreview;
	private YUVImageAnalyzer imageAnalyzer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.main);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Constants.getInstance().screenWidth = metrics.widthPixels;
        Constants.getInstance().screenHeight = metrics.heightPixels;

        imageAnalyzer = new YUVImageAnalyzer(this);
		mPreview = new CameraPreview(this, imageAnalyzer);
		setContentView(mPreview);
		addContentView(imageAnalyzer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
	}
}