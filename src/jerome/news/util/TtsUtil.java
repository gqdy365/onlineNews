package jerome.news.util;

import android.content.Context;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;

public class TtsUtil {

	private static SpeechSynthesizer mTts;
	private static String voicer = "";
	private static TtsUtil single = null;

	public synchronized static TtsUtil getInstance(Context context) {
		if (single == null) {
			single = new TtsUtil();

			// 初始化合成对象
			mTts = SpeechSynthesizer.createSynthesizer(context, null);
			setParam();
		}
		return single;
	}

	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	private static void setParam() {
		// 设置合成
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);

		// 设置发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);

		// 设置语速
		mTts.setParameter(SpeechConstant.SPEED, "50");

		// 设置音调
		mTts.setParameter(SpeechConstant.PITCH, "50");

		// 设置音量
		mTts.setParameter(SpeechConstant.VOLUME, "50");

		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
	}

	/**
	 * 开始播放
	 * @param text
	 * @param mTtsListener
	 * @return
	 */
	public int startSpeak(String text,
			com.iflytek.cloud.SynthesizerListener mTtsListener) {
		if (mTts.isSpeaking()) {
			stopSpeak();
		}
		return mTts.startSpeaking(text, mTtsListener);
	}
	
	/**
	 * 停止播放
	 */
	public void stopSpeak() {
		if (null != mTts) {
			try {
				mTts.stopSpeaking();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
