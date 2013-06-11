import java.io.*;
import java.util.ArrayList;
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
		
		// MyReceiverクラスのinputDataの中身を確認する
		printInputData(myrecv.inputData);
				
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
 * MIDIデバイスからの入力を受けとる自作レシーバー
 * @author suzukimio
 *
 */
class MyReceiver implements Receiver {
	private Synthesizer synth;
	private MidiChannel defaultChannel;
	int debug = 0; // for debug
	public static ArrayList<Long[]> inputData = new ArrayList<Long[]>(); // 音程，ベロシティ，タイムスタンプを保持する配列
	
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
				// 音程，ベロシティ，タイムスタンプをLong型配列dataに入れて，data.clone()をaddする
				Long[] data = {new Long((long)sm.getData1()),
							   new Long((long)sm.getData2()),
							   new Long(timeStamp)};
				inputData.add(data.clone());
				break;
			case ShortMessage.NOTE_OFF: // 明示的に消音のメッセージが送られた場合はこちらの処理に入る
				this.defaultChannel.noteOff(sm.getData1(), sm.getData2()); // ソフトウェア音源で消音
				System.out.println("NOTE OFF: pitch " + sm.getData1() + " : velocity " + sm.getData2() + " : timeStamp " + timeStamp);
				break;
			}
			
			System.out.println("hoge"); // デバッグ用
			
		}
	}
	
	/**
	 * closeメソッド
	 */
	public void close() {
		
	}
	
}
