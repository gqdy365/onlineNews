package jerome.news.lazy;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.Bitmap;

public class MemoryCache {
	SoftReference<Bitmap> soft = new SoftReference<Bitmap>(null);
	private HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>(
			12);

	public Bitmap get(String id) {
		synchronized (cache) {
			if (!cache.containsKey(id))
				return null;
			SoftReference<Bitmap> ref = cache.get(id);
			return ref.get();
		}
	}

	public void put(String id, Bitmap bitmap) {
		synchronized (cache) {
			cache.put(id, new SoftReference<Bitmap>(bitmap));
		}
	}

	public void clear() {
		synchronized (cache) {
			cache.clear();
		}
		System.gc();
	}

	/**
	 * Çå³ýÍ¼Æ¬»º´æ£¬·ÀÖ¹ÄÚ´æÒç³ö
	 * 
	 * @param url
	 */
	public boolean clearBitmap(String url) {
		synchronized (cache) {
			if (!cache.containsKey(url))
				return false;
			SoftReference<Bitmap> ref = cache.get(url);
			if (null != ref.get()) {
				ref.get().recycle();
			}
			System.gc();
			return true;
		}
	}
}