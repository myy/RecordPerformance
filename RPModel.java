import java.util.*;
import java.io.*;
import java.text.*;
import javax.sound.midi.*;
import java.net.*;

/**
 * model�N���X��`
 * @author myy
 *
 */
public class RPModel {
	
	private RecordListener listener = null;
	
	// MIDI�f�o�C�X�œ��͂ɑΉ�����@��Əo�͂ɑΉ�����@��̔ԍ�
	// ���܂̊��ł�PCR-M1��UX16�Őڑ����Ă��āC�f�o�C�X�Ƃ��Ă�UX16���F�������
	static final int DEVICE_IN = 0;
	static final int DEVICE_OUT = 3;
	
	MidiDevice device_input = null;
	MidiDevice device_output = null;
	MyReceiver myrecv = null;

	Sequencer sequencer = null;

	/**
	 * �R���X�g���N�^
	 */
	public RPModel() {
		System.out.println("RPModel constructor");
		
		ArrayList<MidiDevice> devices = getDevices();
		device_input = devices.get(DEVICE_IN);
		device_output = devices.get(DEVICE_OUT);
		
		if(!(device_output instanceof Synthesizer)) {
			throw new IllegalArgumentException("not a Synthesizer!");
		}
	}
	
	/**
	 * �ڑ�����Ă���MIDI�f�o�C�X���擾����
	 * @return
	 */
	public static ArrayList<MidiDevice> getDevices() {
		ArrayList<MidiDevice> devices = new ArrayList<MidiDevice>();
		
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		for(int i=0;i<infos.length;i++) {
			MidiDevice.Info info = infos[i];
			MidiDevice dev = null;
			try {
				dev = MidiSystem.getMidiDevice(info);
				devices.add(dev);
			} catch (SecurityException e) {
				System.err.println(e.getMessage());
			} catch (MidiUnavailableException e) {
				System.err.println(e.getMessage());
			}
		}
		return devices;
	}
	
	/**
	 * �^���J�n
	 */
	public void startRecording() {
				
		try { // MIDI�f�o�C�X�̃g�����X�~�b�^�ƁC�\�t�g�E�F�A���̃V���Z�T�C�U�̐ڑ�
			Transmitter trans = device_input.getTransmitter();
			
			if(!device_input.isOpen()) {
				device_input.open();
			}
			if(!device_output.isOpen()) {
				device_output.open();
			}
			
			// ���V�[�o�[�����삷��
			myrecv = new MyReceiver(device_output);
			trans.setReceiver(myrecv);
		} catch (MidiUnavailableException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
		
	}
	
	/**
	 * �^���I��
	 */
	public void stopRecording() {
		
		// �f�o�C�X�����
		device_input.close();
		
		// MIDI�L�[�{�[�h�œ��͂��ꂽ�����m�F����
		printInputData(MyReceiver.inputData);
		
		// MIDI�t�@�C���̍쐬
		Calendar cal = Calendar.getInstance();
		DateFormat format = new SimpleDateFormat("MMddHHmmss");
		String date = format.format(cal.getTime());
		
		File outputFile = new File(date + ".mid");
		
		Sequence sequence = null;
		
		MetaMessage mmes = null;
		int l = 0; // 4������1���̕b���i�}�C�N���b�j
		
		try {
			sequence = new Sequence(Sequence.PPQ, 480); // 4�������ЂƂ�480tick
			
			// �e���|�ݒ�
			mmes = new MetaMessage();
			int tempo = 80; // TODO �����͎��R�ɕς�����悤�ɂ���K�v������
			l = 60*1000000/tempo;
			mmes.setMessage(0x51, new byte[]{(byte)(l/65536), (byte)(l%65536/256), (byte)(l%256)}, 3);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// ��̃g���b�N����������
		Track track = sequence.createTrack();
		
		// �e���|�ݒ�̃��^���b�Z�[�W���g���b�N�ɒǉ�����
		track.add(new MidiEvent(mmes, 0));
		
		// MIDI�L�[�{�[�h����擾�����f�[�^���g���b�N�ɒǉ����Ă���
		System.out.println("debug");
		long tick = 0;
		long duration = 0; // �ꉹ���Ƃ̒���
		
		for(int i=0;i<MyReceiver.inputData.size();i++) {
			if(i == 0) {
				// debug
				System.out.println("pitch: " + MyReceiver.inputData.get(i).pitch
						+ "  velocity : " + MyReceiver.inputData.get(i).velocity
						+ "  tick: 0");
				
				track.add(createNoteOnEvent(MyReceiver.inputData.get(i).pitch,
						MyReceiver.inputData.get(i).velocity,
						0));
			} else {
				// �}�C�N���b����tick�֕ϊ�����
				// duration = (timeStamp(t) - timeStamp(t-1)) * 480 / l
				duration = (MyReceiver.inputData.get(i).timeStamp - MyReceiver.inputData.get(i-1).timeStamp) * 480 / l;
				
				// debug
				System.out.println("pitch: " + MyReceiver.inputData.get(i).pitch
						+ "  velocity : " + MyReceiver.inputData.get(i).velocity
						+ "  tick: " + (duration + tick));

				track.add(createNoteOnEvent(MyReceiver.inputData.get(i).pitch,
						MyReceiver.inputData.get(i).velocity,
						(duration + tick)));				

				// tick�����Z���Ă���
				tick = tick + duration;
				System.out.println("tick: " + tick);
			}
		}
		// mid�t�@�C�������
		try {
			System.out.println("output to " + date + ".mid");
			MidiSystem.write(sequence, 0, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// finish���\�b�h���Ăԁi���Ԃ�j
		this.listener.finish(new RecordEvent(this));
		
	}
	
	// mid�t�@�C���Đ����\�b�h
	public void midiPlay() {
		// MIDI�܂��̐ݒ�
		try {
			File smf = new File("hoge.mid"); // TODO �t�@�C�����͒��O�ɘ^���������̂��w�肷��
			sequencer = MidiSystem.getSequencer();
			Sequence seq = MidiSystem.getSequence(smf);
			
			sequencer.open(); // �V�[�P���T���J��
			sequencer.setSequence(seq); // �V�[�P���X���Z�b�g����
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(InvalidMidiDataException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(MidiUnavailableException e) {
			e.printStackTrace();
		}
		
		// �Đ�����
		sequencer.start();
		
		// TODO �Đ����ł��邱�Ƃ�RPController�ɒʒm����H
		
	}
	
	// mid�t�@�C����~���\�b�h
	public void midiStop() {
		sequencer.stop();
		sequencer.setTickPosition(0);
		
		// TODO ��~�������Ƃ�RPController�ɒʒm����H
	}
	
	/**
	 * �v���O�����I��
	 */
	public void exitSystem() {
		System.exit(0);
		// TODO ���O�c������̍�Ƃ������ł���K�v���肩��
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
	
	/**
	 * 
	 */
//	public void finishGenerateMid() {
//		this.listener.finish(new RecordEvent(this));
//	}

	/**
	 * MIDI�L�[�{�[�h����󂯎�����f�[�^���ꗗ�\������
	 * @param inputData
	 */
	public static void printInputData(ArrayList<MIDIData> inputData) {
		System.out.println("inputted data");
		for(int i=0;i<inputData.size();i++) {
			System.out.println("pitch: " + inputData.get(i).pitch
							   + "  velocity: " + inputData.get(i).velocity
							   + "  timeStamp: " + inputData.get(i).timeStamp);
		}
	}

	/**
	 * 
	 * @param nKey
	 * @param nVelocity
	 * @param lTick
	 * @return
	 */
	private static MidiEvent createNoteOnEvent(int nKey, int nVelocity, long lTick) {
		// �����̓m�[�g�I�����b�Z�[�W�C�����C�x���V�e�B�C����
		return createNoteEvent(ShortMessage.NOTE_ON, nKey, nVelocity, lTick);
	}
	
	/**
	 * 
	 * @param nCommand
	 * @param nKey
	 * @param nVelocity
	 * @param lTick
	 * @return
	 */
	private static MidiEvent createNoteEvent(int nCommand, int nKey, int nVelocity, long lTick) {
		ShortMessage message = new ShortMessage();
		try {
			message.setMessage(nCommand, 0, nKey, nVelocity);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			System.exit(1);
		}
		MidiEvent event = new MidiEvent(message, lTick);
		
		return event;
	}
	
	
}

/**
 * MIDI�f�o�C�X����̓��͂��󂯂Ƃ鎩�샌�V�[�o�[
 * @author myy
 *
 */
class MyReceiver implements Receiver {
	private Synthesizer synth;
	private MidiChannel defaultChannel;
	int debug = 0; // for debug
	public static ArrayList<MIDIData> inputData = new ArrayList<MIDIData>(); // �����C�x���V�e�B�C�^�C���X�^���v��ێ�����
	
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
				// �擾���������C�x���V�e�B�C�^�C���X�^���v��add����
				MIDIData midiData_ = new MIDIData(sm.getData1(), sm.getData2(), timeStamp);
				inputData.add(midiData_); // clone�����ق����悢�̂��ȁH
				break;
			case ShortMessage.NOTE_OFF: // �����I�ɏ����̃��b�Z�[�W������ꂽ�ꍇ�͂�����̏����ɓ���
				this.defaultChannel.noteOff(sm.getData1(), sm.getData2()); // �\�t�g�E�F�A�����ŏ���
				System.out.println("NOTE OFF: pitch " + sm.getData1() + " : velocity " + sm.getData2() + " : timeStamp " + timeStamp);
				break;
			}
			
		}
	}
	
	/**
	 * close���\�b�h
	 */
	public void close() {
		
	}
}

/**
 * MIDI�L�[�{�[�h�œ��͂��ꂽ�����C�x���V�e�B�C�^�C���X�^���v�������N���X
 * @author myy
 *
 */
class MIDIData {
	int pitch;
	int velocity;
	long timeStamp;
	
	/**
	 * �R���X�g���N�^
	 */
	public MIDIData() {
		
	}
	
	public MIDIData(int pitch, int velocity, long timeStamp) {
		this.pitch = pitch;
		this.velocity = velocity;
		this.timeStamp = timeStamp;
	}
}
