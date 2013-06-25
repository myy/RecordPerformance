import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * view
 * @author myy
 *
 */
public class RPView {
	private JFrame frame = new JFrame("record performance");
	private JButton rStartBtn = new JButton("録音開始"); // 一度押すとテキストが「録音終了」に変更される
	private JButton rStopBtn = new JButton("録音終了");
//	private JButton playBtn = new JButton("再生"); // 一度押すとテキストが「停止」に変更される
	
	// イベントリスナ
	private RecordOperationListener listener = null;
	
	/**
	 * コンストラクタ
	 */
	public RPView() {
		
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
//		cp.add(this.playBtn);
		
		this.frame.setVisible(true);
		this.frame.pack();
	}
	
	/** 
	 * ボタンの状態を表す
	 * @author myy
	 *
	 */
	public enum ButtonStatus {
		start, // これから録音する
		stop // 録音し終わった
		// 録音したものの再生と停止の処理も必要なのでは？
	}
	
	/**
	 * ボタンの状態を設定する
	 * @param status
	 */
	public void setButtonStatus(ButtonStatus status) {
		// 録音した状態のとき
		if(status == ButtonStatus.stop) {
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
		// TODO
	}
	
	// 録音終了イベントを発行
	protected void recordStopReq() {
		// TODO
	}
	
}
