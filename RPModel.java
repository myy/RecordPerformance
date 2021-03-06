import java.util.*;
import java.io.*;
import java.text.*;
import javax.sound.midi.*;
import java.net.*;

/**
 * modelクラス定義
 * @author myy
 *
 */
public class RPModel {
	
	private RecordListener listener = null;
	
	// MIDIデバイスで入力に対応する機器と出力に対応する機器の番号
	// いまの環境ではPCR-M1をUX16で接続していて，デバイスとしてはUX16が認識される
	static final int DEVICE_IN = 0;
	static final int DEVICE_OUT = 3;
	
	MidiDevice device_input = null;
	MidiDevice device_output = null;
	MyReceiver myrecv = null;

	Sequencer sequencer = null;
	
	String date;

	/**
	 * コンストラクタ
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
	 * 接続されているMIDIデバイスを取得する
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
	 * 録音開始
	 */
	public void startRecording() {
				
		try { // MIDIデバイスのトランスミッタと，ソフトウェア側のシンセサイザの接続
			Transmitter trans = device_input.getTransmitter();
			
			if(!device_input.isOpen()) {
				device_input.open();
			}
			if(!device_output.isOpen()) {
				device_output.open();
			}
			
			// レシーバーを自作する
			myrecv = new MyReceiver(device_output);
			trans.setReceiver(myrecv);
		} catch (MidiUnavailableException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
		
	}
	
	/**
	 * 録音終了
	 */
	public void stopRecording() {
		
		// デバイスを閉じる
		device_input.close();
		
		// MIDIキーボードで入力された情報を確認する
		printInputData(MyReceiver.inputData);
		
		// MIDIファイルの作成
		Calendar cal = Calendar.getInstance();
		DateFormat format = new SimpleDateFormat("MMddHHmmss");
		date = format.format(cal.getTime());
		
		File outputFile = new File("smf/" + date + ".mid");
		
		Sequence sequence = null;
		
		MetaMessage mmes = null;
		int l = 0; // 4分音符1つ分の秒数（マイクロ秒）
		
		try {
			sequence = new Sequence(Sequence.PPQ, 480); // 4分音符ひとつ＝480tick
			
			// テンポ設定
			mmes = new MetaMessage();
			int tempo = 80; // TODO ここは自由に変えられるようにする必要がある
			l = 60*1000000/tempo;
			mmes.setMessage(0x51, new byte[]{(byte)(l/65536), (byte)(l%65536/256), (byte)(l%256)}, 3);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			System.exit(1);
		}
		// 空のトラックを準備する
		Track track = sequence.createTrack();
		
		// テンポ設定のメタメッセージをトラックに追加する
		track.add(new MidiEvent(mmes, 0));
		
		// MIDIキーボードから取得したデータをトラックに追加していく
		System.out.println("debug");
		long tick = 0;
		long duration = 0; // 一音ごとの長さ
		
		for(int i=0;i<MyReceiver.inputData.size();i++) {
			if(i == 0) { // 最初のノートイベント：鍵盤を押すのが最初になるはずなのでNOTE OFFは気にしなくてもよい（はず）
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

				// ベロシティの値でNOTE ONとNOTE OFFを振り分ける
				// CASIO LK-205に限らず，離鍵をNOTE OFFで扱うデバイスに対応できているはず
				if(MyReceiver.inputData.get(i).velocity != 0) {
					track.add(createNoteOnEvent(MyReceiver.inputData.get(i).pitch,
							MyReceiver.inputData.get(i).velocity,
							(duration + tick)));									
				} else {
					track.add(createNoteOffEvent(MyReceiver.inputData.get(i).pitch,
//							MyReceiver.inputData.get(i).velocity,
							(duration + tick)));														
					// createNoteOnEventでベロシティ値を0にしても同じことができるはず
				}

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
		
		// 処理が終わったので，viewに対して録音終了を知らせる
		this.listener.rFinish(new RecordEvent(this));
		
	}
	
	// midファイル再生メソッド
	public void midiPlay() {
		// MIDIまわりの設定
		try {
			File smf = new File("smf/" + date + ".mid");
			sequencer = MidiSystem.getSequencer();
			Sequence seq = MidiSystem.getSequence(smf);
			
			sequencer.open(); // シーケンサを開く
			sequencer.setSequence(seq); // シーケンスをセットする
		} catch(MalformedURLException e) {
			e.printStackTrace();
		} catch(InvalidMidiDataException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} catch(MidiUnavailableException e) {
			e.printStackTrace();
		}
		
		// mid再生
		sequencer.start();

		// TODO 再生が完了したら，viewへ再生完了を通知させたい．以下のコードだと通知できない．
//		if(!sequencer.isRecording()) {
//			this.listener.mFinish(new RecordEvent(this));
//		}
		
	}
	
	// midファイル停止メソッド
	public void midiStop() {
		sequencer.stop();
		sequencer.setTickPosition(0);
		
		// midファイル停止をviewへ通知する
		this.listener.mFinish(new RecordEvent(this));
	}
	
	/**
	 * プログラム終了
	 */
	public void exitSystem() {
		System.exit(0);
		// TODO ログ残したりの作業をここでする必要ありかも
	}
	
	/**
	 * イベントリスナの登録
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
	 * イベントリスナの削除
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
	 * MIDIキーボードから受け取ったデータを一覧表示する
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
		// 引数はノートオンメッセージ，音程，ベロシティ，長さ
		return createNoteEvent(ShortMessage.NOTE_ON, nKey, nVelocity, lTick);
	}
	
	/**
	 * 
	 * @param nKey
	 * @param nVelocity
	 * @param lTick
	 * @return
	 */
	private static MidiEvent createNoteOffEvent(int nKey, long lTick) {
		// 引数はノートオンメッセージ，音程，ベロシティ，長さ
		return createNoteEvent(ShortMessage.NOTE_OFF, nKey, 0, lTick);
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
		
		// 2回目以降の録音の場合には，inputDataの中身をクリアする
		if(inputData.size() > 0) {
			System.out.println("ArrayListの要素を削除します");
			inputData.clear();
		}
	}
	
	/**
	 * sendメソッド
	 */
	public void send(MidiMessage message, long timeStamp) {
		// sendメソッドは周期的に呼び出されている
//		MIDIData midiData_;
		
		if(message instanceof ShortMessage) {
			ShortMessage sm = ((ShortMessage)message);
			// getCommand()で取得したMIDIメッセージの種類によって挙動を変える
			switch(sm.getCommand()) {
			case ShortMessage.NOTE_ON: // 鍵盤を押したとき
				debug++;
				// getData1()で音程，getData2()でベロシティの取得
				this.defaultChannel.noteOn(sm.getData1(), sm.getData2()); // ソフトウェア音源で発音
				System.out.println("[" + debug + "] NOTE ON: pitch " + sm.getData1() + " : velocity " + sm.getData2() + " : timeStamp " + timeStamp);
				// 取得した音程，ベロシティ，タイムスタンプをaddする
//				midiData_ = new MIDIData(sm.getData1(), sm.getData2(), timeStamp);
				inputData.add(new MIDIData(sm.getData1(), sm.getData2(), timeStamp));
				break;
			case ShortMessage.NOTE_OFF: // 鍵盤を離したとき（NOTE OFFメッセージを送るデバイスの場合）
				debug++;
				this.defaultChannel.noteOff(sm.getData1(), sm.getData2()); // ソフトウェア音源で消音
				System.out.println("[" + debug + "] NOTE OFF: pitch " + sm.getData1() + " : velocity " + sm.getData2() + " : timeStamp " + timeStamp);
				// 取得した音程，ベロシティ（NOTE OFF なので 0），タイムスタンプをaddする
//				midiData_ = new MIDIData(sm.getData1(), 0, timeStamp);
				inputData.add(new MIDIData(sm.getData1(), 0, timeStamp));
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
	
	/**
	 * コンストラクタ
	 */
	public MIDIData() {
		
	}
	
	public MIDIData(int pitch, int velocity, long timeStamp) {
		this.pitch = pitch;
		this.velocity = velocity;
		this.timeStamp = timeStamp;
	}
}
