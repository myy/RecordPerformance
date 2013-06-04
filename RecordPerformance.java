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
			Receiver myrecv = new MyReceiver(device_output);
			trans.setReceiver(myrecv);
		} catch(MidiUnavailableException e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}

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

}

/**
 * MIDIデバイスからの入力を受けとる自作レシーバー
 * @author suzukimio
 *
 */
class MyReceiver implements javax.sound.midi.Receiver {
	private Synthesizer synth;
	private MidiChannel defaultChannel;
	
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
		// ここでMIDIキーボードからのメッセージの解析を行う
	}
	
	/**
	 * closeメソッド
	 */
	public void close() {
		
	}
}
