/**
 * view���s�C�x���g���󂯂Ƃ郊�X�i���p������C���^�t�F�[�X�̒�`
 * @author myy
 *
 */
public interface RecordOperationListener {
	// �^���J�n�v���ʒm�n���h��
	public void rStartReq(RecordOperationEvent e);
	// �^���I���v���ʒm�n���h��
	public void rStopReq(RecordOperationEvent e);
	// �Đ��v���ʒm�n���h��
	public void mPlayReq(RecordOperationEvent e);
	// ��~�v���ʒm�n���h��
	public void mStopReq(RecordOperationEvent e);
	// �v���O�����I���v���ʒm�n���h��
	public void eReq(RecordOperationEvent e);
}
