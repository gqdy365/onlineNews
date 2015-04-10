package jerome.news.lazy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import jerome.news.R;

public class ImageLoader {

	private MemoryCache memoryCache = new MemoryCache();
	private FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	private List<String> urlList = new ArrayList<String>();
	private String TAG = "ImageLoader";
	private static ImageLoader mImageLoader= null;

	public static ImageLoader getInstance(Context context) {
		if (null == mImageLoader) {
			mImageLoader = new ImageLoader(context);
		}
		return mImageLoader;
	};

	private ImageLoader(Context context) {
		// Make the background thead low priority. This way it will not affect
		// the UI performance
		photoLoaderThread.setPriority(Thread.MAX_PRIORITY - 1);
		fileCache = new FileCache(context);
	}

	final int stub_id = R.drawable.icon_image_default;

	public void DisplayImage(String url, Activity activity, ImageView imageView) {
		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		
		int[] scale = new int[]{0,0,0};
		int gScale = -100;
		//获取每个图片的显示大小，以便于缩放，最大限度节省内存;
		if (null!=imageView.getTag()) {
			String result = (String) imageView.getTag();
			String[] args = result.split("/");
			scale[0] = Integer.valueOf(args[0]);
			scale[1] = Integer.valueOf(args[1]);
			if (args.length > 2) {
				//同一个界面有可能出现一样的图，但有些地方需要大图，有些地方需要小图，
				//如果小图先加载的话，就已经存在在内存中，这样大图就不从sdcard加载，造成失真；
				//-99作为标记
				gScale = Integer.valueOf(args[2]);
				scale[2] = Integer.valueOf(args[2]);
			}
		}

		if (bitmap != null) {
			int gWidth = bitmap.getWidth();
			int gHeight = bitmap.getHeight();
			if((gScale==-99) && (scale[0]>gWidth+100)&& (scale[1]>gHeight+100)){
				queuePhoto(url, imageView,scale);
				imageView.setImageResource(stub_id);
			} else {
				imageView.setImageBitmap(bitmap);
			}
		} else {
			try {
				queuePhoto(url, imageView, scale);
			} catch (Exception e) {
				e.printStackTrace();
			}
			imageView.setImageResource(stub_id);
		}
	}
	
	public void getLargeCache(String url, ImageView imageView) {
		if ("".equals(url.trim())) {
			return;
		}
		if (null != imageView.getTag()) {
			int[] scale = new int[] { 0, 0, 0 };
			String result = (String) imageView.getTag();
			String[] args = result.split("/");
			scale[0] = Integer.valueOf(args[0]);
			scale[1] = Integer.valueOf(args[1]);
			if (args.length > 2) {
				scale[2] = Integer.valueOf(args[2]);
			}
			Bitmap bmp = getBitmap(url, scale);
			synchronized (memoryCache) {
				memoryCache.put(url, bmp);
			}
			urlList.add(url);
		}
	}

	public Bitmap getCacheBitmap(String url) {
		return memoryCache.get(url);
	}

	private void queuePhoto(String url, ImageView imageView,int[] scale) {
		// This ImageView may be used for other images before. So there may be
		// some old tasks in the queue. We need to discard them.
		photosQueue.Clean(imageView);
		PhotoToLoad p = new PhotoToLoad(url, imageView,scale);
		synchronized (photosQueue.photosToLoad) {
			photosQueue.photosToLoad.push(p);
			photosQueue.photosToLoad.notifyAll();
		}

		// start thread if it's not started yet
		if (photoLoaderThread.getState() == Thread.State.NEW)
			photoLoaderThread.start();
	}

	private Bitmap getBitmap(String url,int[] s) {
		File f = fileCache.getFile(url);
		
		// from SD cache
		Bitmap b = decodeFile(f,s);
		if (b != null)
			return b;

		// from web
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f, s);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取图片的宽度和高度
	 * @param url
	 * @return
	 */
	public int[] getWidthHeight(String url) {
		int[] result = new int[] { 0, 0 };
		File f = fileCache.getFile(url);
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);
			result[0] = o.outWidth;
			result[1] = o.outHeight;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 从sdcard加载已经存在的图片
	 * @param context
	 * @param url
	 * @param ws
	 * @param hs
	 * @return
	 */
	public Bitmap getBitmapFromSdCard(Context context,String url , int ws,int hs) {
		Bitmap bitmap= null;
		File f = fileCache.getFile(url);
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			o.inPreferredConfig = Bitmap.Config.RGB_565;   
			o.inPurgeable = true;  
			o.inInputShareable = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			int width_tmp = o.outWidth;
			int height_tmp = o.outHeight;
			int scale = 1;

			while (true) {
				if (width_tmp / 2 < ws || height_tmp / 2 < hs)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(!urlList.contains(url)){
			urlList.add(url);
		}
		
		return bitmap;
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f,int[] s) {
		int ws = 640;
		int hs = 640;
		if (s[0] == 0 && s[1] == 0) {

		} else if (s[0] == -1 && s[1] == -1) {

		} else {
			ws = s[0];
			hs = s[1];
		}

		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			o.inPreferredConfig = Bitmap.Config.RGB_565;   
			o.inPurgeable = true;  
			o.inInputShareable = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			// final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth;
			int height_tmp = o.outHeight;
			int scale = 1;

			while (true) {
				if (width_tmp / 2 < ws || height_tmp / 2 < hs)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}
			
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public int[] mScale;

		public PhotoToLoad(String u, ImageView i, int[] scale) {
			url = u;
			imageView = i;
			mScale = scale;
		}
	}

	PhotosQueue photosQueue = new PhotosQueue();

	public void stopThread() {
		photoLoaderThread.interrupt();
	}

	// stores list of photos to download
	class PhotosQueue {
		private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();

		// removes all instances of this ImageView
		public void Clean(ImageView image) {
			for (int j = 0; j < photosToLoad.size();) {
				if (photosToLoad.get(j).imageView == image)
					photosToLoad.remove(j);
				else
					++j;
			}
		}
	}

	class PhotosLoader extends Thread {
		public void run() {
			try {
				while (true) {
					// thread waits until there are any images to load in the
					// queue
					if (photosQueue.photosToLoad.size() == 0)
						synchronized (photosQueue.photosToLoad) {
							photosQueue.photosToLoad.wait();
						}
					if (photosQueue.photosToLoad.size() != 0) {
						PhotoToLoad photoToLoad;
						synchronized (photosQueue.photosToLoad) {
							photoToLoad = photosQueue.photosToLoad.pop();
						}
						Bitmap bmp = getBitmap(photoToLoad.url,photoToLoad.mScale);
						synchronized (memoryCache) {
							memoryCache.put(photoToLoad.url, bmp);
						}
						urlList.add(photoToLoad.url);
						String tag = imageViews.get(photoToLoad.imageView);
						if (tag != null && tag.equals(photoToLoad.url)) {
							BitmapDisplayer bd = new BitmapDisplayer(bmp,photoToLoad.imageView);
							Activity a = (Activity) photoToLoad.imageView.getContext();
							a.runOnUiThread(bd);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	PhotosLoader photoLoaderThread = new PhotosLoader();

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;

		public BitmapDisplayer(Bitmap b, ImageView i) {
			bitmap = b;
			imageView = i;
		}

		public void run() {
			if (bitmap != null){
				if (null != imageView.getTag()) {
					imageView.setImageBitmap(bitmap);
					// imageView.setImageBitmap(Common.resizeImage(bitmap,
					// Common.SCALE_SIZE));
				} else {
					imageView.setImageBitmap(bitmap);
				}
			} else {
				imageView.setImageResource(stub_id);
			}
		}
	}

	public void clearCache() {
		synchronized (memoryCache) {
		memoryCache.clear();
		}
		fileCache.clear();
	}
	
	/**
	 * 回收用完的图片，防止内存溢出
	 */
	public void clearBitmap() {
		synchronized (memoryCache) {
			for (int i = 0; i < urlList.size(); i++) {
				memoryCache.clearBitmap(urlList.get(i));
			}
			memoryCache.clear();
		}

		float total = Runtime.getRuntime().totalMemory();
		float free = Runtime.getRuntime().freeMemory();
		float max = Runtime.getRuntime().maxMemory();
		Log.i("zwq", "********totalMemory total*********" + total
				/ 1000.f / 1000.0f + "__free___" + free / 1000.f
				/ 1000.0f + "__max___" + max / 1000.f / 1000.0f);
	}
	
	public void clearBitmapByUrl(String url) {
		synchronized (memoryCache) {
			memoryCache.clearBitmap(url);
		}
	}

	public void downLoadPic(String url) {
		File file = fileCache.getFile(url);
		if (!file.exists()) {
			// from web
			try {
				URL imageUrl = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) imageUrl
						.openConnection();
				conn.setConnectTimeout(30000);
				conn.setReadTimeout(30000);
				InputStream is = conn.getInputStream();
				OutputStream os = new FileOutputStream(file);
				Utils.CopyStream(is, os);
				os.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
}
