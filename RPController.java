import java.util.*;

/**
 * controllerクラス定義
 * @author myy
 *
 */
public class RPController implements RecordOperationListener, RecordListener {
	
	// viewのインスタンス
	private RPView view;
	// modelのインスタンス
	private RPModel model;
	
	/**
	 * コンストラクタ
	 */
	public RPController() {
		System.out.println("RPController constructor");

		try {
			// view，modelのインスタンスの生成
			this.view = new RPView();
			this.model = new RPModel();
			
			System.out.println("instances are generated.");
			// 表示の初期化
			this.view.setButtonStatus(RPView.ButtonStatus.init);
			this.view.addRecordOperationListener(this);
			this.model.addRecordListener(this);
		} catch (TooManyListenersException ex) {
			
		}
	}
	
	// viewからの通知処理
	// 録音スタート通知ハンドラ
	@Override public void rStartReq(RecordOperationEvent e) {
		this.model.startRecording();
		this.view.setButtonStatus(RPView.ButtonStatus.recording);
	}
	// 録音ストップ通知ハンドラ
	@Override public void rStopReq(RecordOperationEvent e) {
		this.model.stopRecording();
	}
	// プログラム終了通知ハンドラ
	@Override public void eReq(RecordOperationEvent e) {
		this.model.exitSystem();
	}
	
	// modelからの通知処理
	// 録音終了通知ハンドラ
	@Override public void finish(RecordEvent e) {
		this.view.setButtonStatus(RPView.ButtonStatus.init);
	}
	
}
