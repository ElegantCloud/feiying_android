package com.ivyinfo.feiying.utity;

import android.graphics.Bitmap;
import android.widget.ImageButton;

public class ImageCallback {
	
	ImageButton button;
	
	public ImageCallback(ImageButton btn){
		this.button = btn;
	}
	
	public void imageLoaded(Bitmap bitmap){
		button.setImageBitmap(bitmap);
	}
}
