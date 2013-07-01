import java.util.*;

/**
 * modelクラス定義
 * @author myy
 *
 */
public class RPModel {
	
	private RecordListener listener = null;
	
	/**
	 * コンストラクタ
	 */
	public RPModel() {
		
	}
	
	// 録音開始
	public void startRecording() {
		// TODO
		// MIDIデバイスの接続
		// レシーバ作成
		// 入力データの記録
	}
	
	// 録音終了
	public void stopRecording() {
		// TODO
		// MIDIファイルの作成
		// finishGeneratedMidメソッドを呼ぶ（たぶん）
	}
	
	/**
	 * イベントリスナの登録
	 * @param l
	 * @throws TooManyListenersException
	 */
	public void addRecordListener(RecordListener l) throws TooManyListenersException {
		if(this.listener != null) {
			throw new TooManyListenersException();
		}
		this.listener = l;
	}
	
	/**
	 * イベントリスナの削除
	 * @param l
	 */
	public void removeRecordListener(RecordListener l) {
		if(this.listener == l) {
			this.listener = null;
		}
	}
	
	public void finishGenerateMid() {
		this.listener.finish(new RecordEvent(this));
	}

}
