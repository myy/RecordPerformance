/**
 * model発行イベントを受けとるリスナが継承するインタフェースの定義
 * @author myy
 *
 */
public interface RecordListener {
	// 録音終了通知ハンドラ
	public void finish(RecordEvent e);
}
