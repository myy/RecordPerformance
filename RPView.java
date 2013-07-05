import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * viewクラス定義<br>
 * #6 終了ボタンを用意する
 * @author myy
 *
 */
public class RPView {
	private JFrame frame = new JFrame("record performance");
	private JButton rStartBtn = new JButton("録音開始");
	private JButton rStopBtn = new JButton("録音終了");
	
	// イベントリスナ
	private RecordOperationListener listener = null;
	
	/**
	 * コンストラクタ
	 */
	public RPView() {
		System.out.println("RPView constructor");
		// フレーム初期化
		setupFrame();
		
		// 録音開始ボタンのアクションリスナー設定
		this.rStartBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				recordStartReq();
			}
		});
		// 録音終了ボタンのアクションリスナー設定
		this.rStopBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				recordStopReq();
			}
		});
		
	}
	
	/**
	 * フレームを初期化する
	 */
	protected void setupFrame() {
		// look&feelの設定
		UIManager.LookAndFeelInfo infos[] = UIManager.getInstalledLookAndFeels();
		String laf = "";
		
		for(int i=0;i<infos.length;i++) {
			// win
			if(infos[i].getName().equals("Windows")) {
				laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
			}
			// mac
			if(infos[i].getName().equals("Mac OS X")) {
				laf = "com.apple.laf.AquaLookAndFeel";				
			}
		}
		
		try{
			UIManager.setLookAndFeel(laf);
			SwingUtilities.updateComponentTreeUI(frame);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Container cp = this.frame.getContentPane();
		cp.setLayout(new FlowLayout());
		cp.add(this.rStartBtn);
		cp.add(this.rStopBtn);
		
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true);
		this.frame.pack();
	}
	
	/** 
	 * ボタンの状態を表す
	 * @author myy
	 *
	 */
	public enum ButtonStatus {
		init, // 初期状態．録音できる状態
		recording // 録音中
		// TODO 録音したものの再生と停止の処理も必要．あとから追加で実装する
	}
	
	/**
	 * ボタンの状態を設定する
	 * @param status
	 */
	public void setButtonStatus(ButtonStatus status) {
		// 録音している状態のとき
		if(status == ButtonStatus.recording) {
			this.rStartBtn.setEnabled(false);
			this.rStopBtn.setEnabled(true);
		}
		// 録音できる状態のとき
		else {
			this.rStartBtn.setEnabled(true);
			this.rStopBtn.setEnabled(false);
		}
	}
	
	/**
	 * イベントリスナの登録
	 * @param l
	 * @throws TooManyListenersException
	 */
	public void addRecordOperationListener(RecordOperationListener l) throws TooManyListenersException {
		if(this.listener != null) {
			throw new TooManyListenersException();
		}
		this.listener = l;
	}
	
	/**
	 * イベントリスナの削除<br>
	 * 引数が登録中のリスナーと一致しないときはムシする
	 * @param l
	 */
	public void removeRecordOperationListener(RecordOperationListener l) {
		if(this.listener == l) {
			this.listener = null;
		}
	}
	
	// 録音開始イベントを発行
	protected void recordStartReq() {
		this.listener.rStartReq(new RecordOperationEvent(this));
	}
	
	// 録音終了イベントを発行
	protected void recordStopReq() {
		this.listener.rStopReq(new RecordOperationEvent(this));
	}
	
}
