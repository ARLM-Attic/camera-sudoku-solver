package com.camerasudokusolver;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.camerasudokusolver.Utils.Constants;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dmitry.bushtets
 * Date: 1/20/12
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback  {
	Camera camera_;
	boolean finished_;
	SurfaceHolder holder_;
	YUVImageAnalyzer imageAnalyzer;

	CameraPreview(Context context, YUVImageAnalyzer hv ) {
		super(context);

		imageAnalyzer = hv;

		finished_ = false;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		holder_ = getHolder();
		holder_.addCallback(this);
		holder_.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = camera_.getParameters();
        int width = Constants.getInstance().screenWidth;
        int height = Constants.getInstance().screenHeight;
		parameters.setPreviewSize(width, height);
        //parameters.setPreviewFrameRate(25);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		camera_.setParameters(parameters);
		camera_.startPreview();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		camera_ = Camera.open();
		try {
			camera_.setPreviewDisplay(holder);
			camera_.setPreviewCallback(new Camera.PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera camera)
				{
					if ( finished_ )
						return;

                    // Initialize the draw-on-top companion
					if (YUVImageAnalyzer.readyForNextFrame) {
						imageAnalyzer.decodeYUV420SP(data);
						imageAnalyzer.invalidate();
					}
				}
			});

		}
		catch (IOException exception) {
			camera_.release();
			camera_ = null;
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		finished_ = true;
		camera_.setPreviewCallback(null);
		camera_.stopPreview();
		camera_.release();
		camera_ = null;
	}
}
