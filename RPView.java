import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * view�N���X��`<br>
 * #5 mid�t�@�C���̍Đ��@�\������
 * @author myy
 *
 */
public class RPView {
	private JFrame frame = new JFrame("record performance");
	private JButton rStartBtn = new JButton("�^���J�n");
	private JButton rStopBtn = new JButton("�^���I��");
	private JButton exitBtn = new JButton("�v���O�����I��");
	private JButton mPlayBtn = new JButton("�Đ�");
	private JButton mStopBtn = new JButton("��~");
	
	// �C�x���g���X�i
	private RecordOperationListener listener = null;
	
	/**
	 * �R���X�g���N�^
	 */
	public RPView() {
		System.out.println("RPView constructor");
		// �t���[��������
		setupFrame();
		
		// �^���J�n�{�^���̃A�N�V�������X�i�[�ݒ�
		this.rStartBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				recordStartReq();
			}
		});
		// �^���I���{�^���̃A�N�V�������X�i�[�ݒ�
		this.rStopBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				recordStopReq();
			}
		});
		
		// �Đ��J�n�{�^���̃A�N�V�������X�i�[�ݒ�
		this.mPlayBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				midPlayReq();
			}
		});
		
		// �Đ���~�{�^���̃A�N�V�������X�i�[�ݒ�
		this.mStopBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				midStopReq();
			}
		});
		
		// �v���O�����I���{�^���̃A�N�V�������X�i�[�ݒ�
		this.exitBtn.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				exitReq();
			}
		});
		
	}
	
	/**
	 * �t���[��������������
	 */
	protected void setupFrame() {
		// look&feel�̐ݒ�
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
		cp.setLayout(new GridLayout(3, 2));
		cp.add(this.rStartBtn);
		cp.add(this.rStopBtn);
		cp.add(this.mPlayBtn);
		cp.add(this.mStopBtn);
		cp.add(this.exitBtn);
		
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true);
		this.frame.pack();
	}
	
	/** 
	 * �{�^���̏�Ԃ�\��
	 * @author myy
	 *
	 */
	public enum ButtonStatus {
		init, // �������
		recording, // �^����
		recorded, // �^����
		playing, // �Đ���
		exit // �v���O�����I��
	}
	
	/**
	 * �{�^���̏�Ԃ�ݒ肷��
	 * @param status
	 */
	public void setButtonStatus(ButtonStatus status) {
		// �^�����Ă����Ԃ̂Ƃ�
		if(status == ButtonStatus.recording) {
			this.rStartBtn.setEnabled(false);
			this.rStopBtn.setEnabled(true);
			this.mPlayBtn.setEnabled(false);
			this.mStopBtn.setEnabled(false);
			this.exitBtn.setEnabled(true);
		}
		// �^����
		else if(status == ButtonStatus.recorded) {
			this.rStartBtn.setEnabled(true);
			this.rStopBtn.setEnabled(false);
			this.mPlayBtn.setEnabled(true);
			this.mStopBtn.setEnabled(false);
			this.exitBtn.setEnabled(true);
		}
		// �Đ���
		else if(status == ButtonStatus.playing) {
			this.rStartBtn.setEnabled(false);
			this.rStopBtn.setEnabled(false);
			this.mPlayBtn.setEnabled(false);
			this.mStopBtn.setEnabled(true);
			this.exitBtn.setEnabled(true);
		}
		// ������ԁiButtonStatus == init�j
		else {
			this.rStartBtn.setEnabled(true);
			this.rStopBtn.setEnabled(false);
			this.mPlayBtn.setEnabled(false);
			this.mStopBtn.setEnabled(false);
			this.exitBtn.setEnabled(true);
		}
	}
	
	/**
	 * �C�x���g���X�i�̓o�^
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
	 * �C�x���g���X�i�̍폜<br>
	 * �������o�^���̃��X�i�[�ƈ�v���Ȃ��Ƃ��̓��V����
	 * @param l
	 */
	public void removeRecordOperationListener(RecordOperationListener l) {
		if(this.listener == l) {
			this.listener = null;
		}
	}
	
	// �^���J�n�C�x���g�𔭍s
	protected void recordStartReq() {
		this.listener.rStartReq(new RecordOperationEvent(this));
	}
	
	// �^���I���C�x���g�𔭍s
	protected void recordStopReq() {
		this.listener.rStopReq(new RecordOperationEvent(this));
	}
	
	// �Đ��C�x���g�𔭍s
	protected void midPlayReq() {
		this.listener.mPlayReq(new RecordOperationEvent(this));
	}
	
	// ��~�C�x���g�𔭍s
	protected void midStopReq() {
		this.listener.mStopReq(new RecordOperationEvent(this));
	}
	
	// �v���O�����I���C�x���g�𔭍s
	protected void exitReq() {
		this.listener.eReq(new RecordOperationEvent(this));
	}
}
