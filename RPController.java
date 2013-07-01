import java.util.*;

/**
 * controller�N���X��`
 * @author myy
 *
 */
public class RPController implements RecordOperationListener, RecordListener {
	
	// view�̃C���X�^���X
	private RPView view;
	// model�̃C���X�^���X
	private RPModel model;
	
	/**
	 * �R���X�g���N�^
	 */
	public RPController() {
		try {
			// view�Cmodel�̃C���X�^���X�̐���
			this.view = new RPView();
			this.model = new RPModel();
			// �\���̏�����
			this.view.setButtonStatus(RPView.ButtonStatus.stop);
			// int t = this.model.getRemain();
			// this.view.setRemain(t);
			this.view.addRecordOperationListener(this);
//			this.model.addRecordListener(this);
		} catch (TooManyListenersException ex) {
			
		}
	}
	
	// view����̒ʒm����
	// �^���X�^�[�g�ʒm�n���h��
	@Override public void rStartReq(RecordOperationEvent e) {
		this.model.startRecording();
		this.view.setButtonStatus(RPView.ButtonStatus.start);
	}
	// �^���X�g�b�v�ʒm�n���h��
	@Override public void rStopReq(RecordOperationEvent e) {
		this.model.stopRecording();
	}
	
	// model����̒ʒm����
	// �^���I���ʒm�n���h��
	@Override public void finish(RecordEvent e) {
		this.view.setButtonStatus(RPView.ButtonStatus.stop);
	}
	
}