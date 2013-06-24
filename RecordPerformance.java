import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.sound.midi.*;

/**
 * MIDIキーボードからの入力を記録し，midファイルに出力する
 * @author myy
 *
 */
class RecordPerformance {
	// MIDIデバイスで入力に対応する機器と出力に対応する機器の番号
	// いまの環境ではPCR-M1をUX16で接続していて，デバイスとしてはUX16が認識される
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
		
		try{ // UX16のトランスミッタとソフトウェアのシンセサイザをつなぐ
			Transmitter trans = device_input.getTransmitter();
			
			if(!device_input.isOpen()) {
				device_input.open();
			}
			if(!device_output.isOpen()) {
				device_output.open();
			}
			
			// PCR-M1からの入力を受け取って，いろいろやりたいのでレシーバーを自作する
			myrecv = new MyReceiver(device_output);
			trans.setReceiver(myrecv);
		} catch(MidiUnavailableException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
		
		// ENTERキーが押されるたら，デバイスを閉じる
		try {
			System.in.read();
			System.out.println("Pressed ENTER key");
		} catch (IOException e) {
			
		}
		device_input.close();
		
		// MIDIキーボードで入力された情報を確認する
		printInputData(MyReceiver.inputData);
				
		// MIDIキーボードでの演奏からmidファイルを生成する
		// ファイル名は日時と時刻にする
		Calendar cal = Calendar.getInstance();
		DateFormat format = new SimpleDateFormat("MMddHHmmss");
		String date = format.format(cal.getTime());
		
		// ファイル名の設定
		File outputFile = new File(date + ".mid");
		
		// Sequence型変数の準備
		Sequence sequence = null;
		
		// MetaMessage型変数の準備
		MetaMessage mmes = null;
		int l = 0; // ４分音符1つぶんの秒数（マイクロ秒）
		
		try {
			sequence = new Sequence(Sequence.PPQ, 480); // ４分音符一つ＝480tick
			
			// テンポを設定する
			mmes = new MetaMessage();
			int tempo = 80; // ここは録音前に自由に変えられるようにしなければいけない
			l = 60*1000000/tempo;
			mmes.setMessage(0x51, new byte[]{(byte)(l/65536), (byte)(l%65536/256), (byte)(l%256)}, 3);

			// 曲によっては，メタメッセージで拍子の設定も必要になってくる
			
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// 空のトラックを準備する
		Track track = sequence.createTrack();
		
		// テンポ設定のメタメッセージをトラックにaddする
		track.add(new MidiEvent(mmes, 0));
		
		// MIDIキーボードから取得したデータをトラックにaddしていく
		System.out.println("debug");
		long tick = 0; // tick累積用
		long duration = 0; // 一音ごとの長さ(tick)用
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
				// マイクロ秒からtickへ変換する
				// duration = (timeStamp(t) - timeStamp(t-1)) * 480 / l
				duration = (MyReceiver.inputData.get(i).timeStamp - MyReceiver.inputData.get(i-1).timeStamp) * 480 / l;
				
				// debug
				System.out.println("pitch: " + MyReceiver.inputData.get(i).pitch
						+ "  velocity : " + MyReceiver.inputData.get(i).velocity
						+ "  tick: " + (duration + tick));

				track.add(createNoteOnEvent(MyReceiver.inputData.get(i).pitch,
						MyReceiver.inputData.get(i).velocity,
						(duration + tick)));				

				// tickを加算していく
				tick = tick + duration;
				System.out.println("tick: " + tick);
			}
		}
		// midファイルを作る
		try {
			System.out.println("output to " + date + ".mid");
			MidiSystem.write(sequence, 0, outputFile);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("exit");

	}
	
	/**
	 * 接続されているMIDIデバイスを取得する
	 * @return 接続されているMIDIデバイス情報
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
	 * MIDIキーボードから受け取ったデータを一覧表示する
	 */
	public static void printInputData(ArrayList<MIDIData> inputData) {
		System.out.println("inputted data");
		for(int i=0;i<inputData.size();i++) {
			System.out.println("pitch: " + inputData.get(i).pitch
					   + "  velocity: " + inputData.get(i).velocity
					   + "  timeStamp: " + inputData.get(i).timeStamp);			
		}
	}
	
	// ノートオンイベントを作成するメソッド
	private static MidiEvent createNoteOnEvent(int nKey, int nVelocity, long lTick) {
		// 引数はノートオンメッセージ，音程，ベロシティ，長さ
		return createNoteEvent(ShortMessage.NOTE_ON, nKey, nVelocity, lTick);
	}
	
	// ノートイベントを作成するメソッド
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
 * MIDIデバイスからの入力を受けとる自作レシーバー
 * @author myy
 *
 */
class MyReceiver implements Receiver {
	private Synthesizer synth;
	private MidiChannel defaultChannel;
	int debug = 0; // for debug
	public static ArrayList<MIDIData> inputData = new ArrayList<MIDIData>(); // 音程，ベロシティ，タイムスタンプを保持する
	
	/**
	 * コンストラクタ
	 * @param device_out MIDIデバイスの出力側
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
		this.defaultChannel = channels[0]; // 最初のチャンネルをデフォルトで読む
	}
	
	/**
	 * sendメソッド
	 */
	public void send(MidiMessage message, long timeStamp) {
		// sendメソッドは周期的に呼び出されている
		
		if(message instanceof ShortMessage) {
			ShortMessage sm = ((ShortMessage)message);
			// getCommand()で取得したMIDIメッセージの種類によって挙動を変える
			switch(sm.getCommand()) {
			case ShortMessage.NOTE_ON: // 鍵盤を離した場合もこちらの処理に入る
				debug++;
				// getData1()で音程，getData2()でベロシティの取得
				this.defaultChannel.noteOn(sm.getData1(), sm.getData2()); // ソフトウェア音源で発音
				System.out.println("[" + debug + "] NOTE ON: pitch " + sm.getData1() + " : velocity " + sm.getData2() + " : timeStamp " + timeStamp);
				// 取得した音程，ベロシティ，タイムスタンプをaddする
				MIDIData midiData_ = new MIDIData(sm.getData1(), sm.getData2(), timeStamp);
				inputData.add(midiData_); // cloneしたほうがよいのかな？
				break;
			case ShortMessage.NOTE_OFF: // 明示的に消音のメッセージが送られた場合はこちらの処理に入る
				this.defaultChannel.noteOff(sm.getData1(), sm.getData2()); // ソフトウェア音源で消音
				System.out.println("NOTE OFF: pitch " + sm.getData1() + " : velocity " + sm.getData2() + " : timeStamp " + timeStamp);
				break;
			}
			
		}
	}
	
	/**
	 * closeメソッド
	 */
	public void close() {
		
	}
	
}

/**
 * MIDIキーボードで入力された音程，ベロシティ，タイムスタンプを扱うクラス
 * @author myy
 *
 */
class MIDIData {
	int pitch;
	int velocity;
	long timeStamp;
	
	public MIDIData() {
		
	}
	
	public MIDIData(int pitch, int velocity, long timeStamp) {
		this.pitch = pitch;
		this.velocity = velocity;
		this.timeStamp = timeStamp;
	}
}
