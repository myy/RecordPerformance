import java.util.*;

/**
 * model�N���X��`
 * @author myy
 *
 */
public class RPModel {
	
	private RecordListener listener = null;
	
	/**
	 * �R���X�g���N�^
	 */
	public RPModel() {
		
	}
	
	// �^���J�n
	public void startRecording() {
		// TODO
		// MIDI�f�o�C�X�̐ڑ�
		// ���V�[�o�쐬
		// ���̓f�[�^�̋L�^
	}
	
	// �^���I��
	public void stopRecording() {
		// TODO
		// MIDI�t�@�C���̍쐬
		// finishGeneratedMid���\�b�h���Ăԁi���Ԃ�j
	}
	
	/**
	 * �C�x���g���X�i�̓o�^
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
	 * �C�x���g���X�i�̍폜
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
