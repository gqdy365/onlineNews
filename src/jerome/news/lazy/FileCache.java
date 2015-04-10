package jerome.news.lazy;

import java.io.File;

import android.content.Context;

public class FileCache {
    
    private File cacheDir;
    
    public FileCache(Context context){
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"carnetnews");
        }else{
            cacheDir=context.getCacheDir();
        }
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }
    
	public File getFile(String url) {
		// 用图片路径的hashcode值作为文件名称
		String filename = String.valueOf(url.hashCode());
		return new File(cacheDir, filename);
	}
    
	public void clear() {
		File[] files = cacheDir.listFiles();
		for (File f : files) {
			f.delete();
		}
	}

}