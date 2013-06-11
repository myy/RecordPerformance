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
		MyReceiver myrecv = null;
		
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
			myrecv = new MyReceiver(device_output);
			trans.setReceiver(myrecv);
		} catch(MidiUnavailableException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
		
		// ENTER�L�[��������邽��C�f�o�C�X�����
		try {
			System.in.read();
			System.out.println("Pressed ENTER key");
		} catch (IOException e) {
			
		}
		device_input.close();
		
		// MyReceiver�N���X��inputData�̒��g���m�F����
		printInputData(myrecv.inputData);
				
		System.out.println("exit");

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

	/**
	 * MIDI�L�[�{�[�h����󂯎�����f�[�^���ꗗ�\������
	 */
	public static void printInputData(ArrayList<Long[]> inputData) {
		System.out.println("inputted data");
		for(int i=0;i<inputData.size();i++) {
			System.out.println("pitch: " + inputData.get(i)[0]
							   + "  velocity: " + inputData.get(i)[1]
							   + "  timeStamp: " + inputData.get(i)[2]);			
		}	
	}
	
}

/**
 * MIDI�f�o�C�X����̓��͂��󂯂Ƃ鎩�샌�V�[�o�[
 * @author suzukimio
 *
 */
class MyReceiver implements Receiver {
	private Synthesizer synth;
	private MidiChannel defaultChannel;
	int debug = 0; // for debug
	public static ArrayList<Long[]> inputData = new ArrayList<Long[]>(); // �����C�x���V�e�B�C�^�C���X�^���v��ێ�����z��
	
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
		// send���\�b�h�͎����I�ɌĂяo����Ă���
		
		if(message instanceof ShortMessage) {
			ShortMessage sm = ((ShortMessage)message);
			// getCommand()�Ŏ擾����MIDI���b�Z�[�W�̎�ނɂ���ċ�����ς���
			switch(sm.getCommand()) {
			case ShortMessage.NOTE_ON: // ���Ղ𗣂����ꍇ��������̏����ɓ���
				debug++;
				// getData1()�ŉ����CgetData2()�Ńx���V�e�B�̎擾
				this.defaultChannel.noteOn(sm.getData1(), sm.getData2()); // �\�t�g�E�F�A�����Ŕ���
				System.out.println("[" + debug + "] NOTE ON: pitch " + sm.getData1() + " : velocity " + sm.getData2() + " : timeStamp " + timeStamp);
				// �����C�x���V�e�B�C�^�C���X�^���v��Long�^�z��data�ɓ���āCdata.clone()��add����
				Long[] data = {new Long((long)sm.getData1()),
							   new Long((long)sm.getData2()),
							   new Long(timeStamp)};
				inputData.add(data.clone());
				break;
			case ShortMessage.NOTE_OFF: // �����I�ɏ����̃��b�Z�[�W������ꂽ�ꍇ�͂�����̏����ɓ���
				this.defaultChannel.noteOff(sm.getData1(), sm.getData2()); // �\�t�g�E�F�A�����ŏ���
				System.out.println("NOTE OFF: pitch " + sm.getData1() + " : velocity " + sm.getData2() + " : timeStamp " + timeStamp);
				break;
			}
			
			System.out.println("hoge"); // �f�o�b�O�p
			
		}
	}
	
	/**
	 * close���\�b�h
	 */
	public void close() {
		
	}
	
}
