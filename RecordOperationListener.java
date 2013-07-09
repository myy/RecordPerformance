/**
 * view発行イベントを受けとるリスナが継承するインタフェースの定義
 * @author myy
 *
 */
public interface RecordOperationListener {
	// 録音開始要求通知ハンドラ
	public void rStartReq(RecordOperationEvent e);
	// 録音終了要求通知ハンドラ
	public void rStopReq(RecordOperationEvent e);
	// 再生要求通知ハンドラ
	public void mPlayReq(RecordOperationEvent e);
	// 停止要求通知ハンドラ
	public void mStopReq(RecordOperationEvent e);
	// プログラム終了要求通知ハンドラ
	public void eReq(RecordOperationEvent e);
}
