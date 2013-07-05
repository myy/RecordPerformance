import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * view�N���X��`<br>
 * #6 �I���{�^����p�ӂ���
 * @author myy
 *
 */
public class RPView {
	private JFrame frame = new JFrame("record performance");
	private JButton rStartBtn = new JButton("�^���J�n");
	private JButton rStopBtn = new JButton("�^���I��");
	
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
		cp.setLayout(new FlowLayout());
		cp.add(this.rStartBtn);
		cp.add(this.rStopBtn);
		
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
		init, // ������ԁD�^���ł�����
		recording // �^����
		// TODO �^���������̂̍Đ��ƒ�~�̏������K�v�D���Ƃ���ǉ��Ŏ�������
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
		}
		// �^���ł����Ԃ̂Ƃ�
		else {
			this.rStartBtn.setEnabled(true);
			this.rStopBtn.setEnabled(false);
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
	
}
