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
}
