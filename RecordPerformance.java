import java.io.*;
import java.util.ArrayList;
import javax.sound.midi.*;

/**
 * MIDI�L�[�{�[�h����̓��͂��L�^���Cmid�t�@�C���ɏo�͂���
 * @author myy
 *
 */
class RecordPerformance {
	// MIDI�f�o�C�X�œ��͂ɑΉ�����@��Əo�͂ɑΉ�����@��̔ԍ�
	// ���܂̊��ł�PCR-M1��UX16�Őڑ����Ă��āC�f�o�C�X�Ƃ��Ă�UX16���F�������
	static final int DEVICE_IN = 0;
	static final int DEVICE_OUT = 3;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<MidiDevice> devices = getDevices();
		MidiDevice device_input = devices.get(DEVICE_IN);
		MidiDevice device_output = devices.get(DEVICE_OUT);
		if(!(device_output instanceof Synthesizer)) {
			throw new IllegalArgumentException("not a Synthesizer!");
		}
		
		try{ // UX16�̃g�����X�~�b�^�ƃ\�t�g�E�F�A�̃V���Z�T�C�U���Ȃ�
			Transmitter trans = device_input.getTransmitter();
			
			if(!device_input.isOpen()) {
				device_input.open();
			}
			if(!device_output.isOpen()) {
				device_output.open();
			}
			
			// PCR-M1����̓��͂��󂯎���āC���낢���肽���̂Ń��V�[�o�[�����삷��
			Receiver myrecv = new MyReceiver(device_output);
			trans.setReceiver(myrecv);
		} catch(MidiUnavailableException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}

	}
	
	/**
	 * �ڑ�����Ă���MIDI�f�o�C�X���擾����
	 * @return �ڑ�����Ă���MIDI�f�o�C�X���
	 */
	public static ArrayList<MidiDevice> getDevices() {
		ArrayList<MidiDevice> devices = new ArrayList<MidiDevice>();
		
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for(int i=0;i<infos.length;i++) {
			MidiDevice.Info info = infos[i];
			MidiDevice dev = null;
			try{
				dev = MidiSystem.getMidiDevice(info);
				devices.add(dev);
			} catch(SecurityException e) {
				System.err.println(e.getMessage());
			} catch(MidiUnavailableException e) {
				System.err.println(e.getMessage());
			}
		}
		return devices;
	}

}

/**
 * MIDI�f�o�C�X����̓��͂��󂯂Ƃ鎩�샌�V�[�o�[
 * @author suzukimio
 *
 */
class MyReceiver implements javax.sound.midi.Receiver {
	private Synthesizer synth;
	private MidiChannel defaultChannel;
	
	/**
	 * �R���X�g���N�^
	 * @param device_out MIDI�f�o�C�X�̏o�͑�
	 */
	public MyReceiver(MidiDevice device_out) {
		if(!(device_out instanceof Synthesizer)) {
			throw new IllegalArgumentException ("device is not a Synthesizer");
		}
		this.synth = (Synthesizer)device_out;
		
		// get first channel of the device
		MidiChannel[] channels = this.synth.getChannels();
		if(0 == channels.length) {
			throw new IllegalStateException("no channels available");
		}
		this.defaultChannel = channels[0]; // �ŏ��̃`�����l�����f�t�H���g�œǂ�
	}
	
	/**
	 * send���\�b�h
	 */
	public void send(MidiMessage message, long timeStamp) {
		// ������MIDI�L�[�{�[�h����̃��b�Z�[�W�̉�͂��s��
	}
	
	/**
	 * close���\�b�h
	 */
	public void close() {
		
	}
}
