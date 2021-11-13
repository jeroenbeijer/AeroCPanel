package subscriber;

import gui.MainPanel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import datatypes.Aircraft;
import decoder.AudioDecoder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.table.DefaultTableModel;

public class SubscriberThread implements Runnable {

	int instance = 1;

	SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

	FileOutputStream fos = null;
	File audioOut = null;
	String currentHex = "";
	boolean isMil = false;
	boolean muted = false;
	ZContext context = null;
	SourceDataLine dataline = null;

	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		this.muted = muted;
	}

	public void flush() {

		if (dataline != null) {

			dataline.flush();
		}
	}

	public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}

	public void run() {

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {

				try {

					if (fos != null)
						fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				System.out.println("Closing.....");

			}
		}));

		context = new ZContext();
		ZMQ.Socket socket = context.createSocket(ZMQ.SUB);

		socket.setTCPKeepAlive(1);

		boolean result = socket.connect(MainPanel.address[instance]);

		System.out.println(
				"Subscribing " + "JAERO-" + instance + " on address " + MainPanel.address[instance] + " " + result);

		// hard coded for now
		socket.subscribe("JAERO");

		AudioDecoder oCodec = new AudioDecoder();

		oCodec.init();

		short[] audio = new short[25 * 160];

		try {
			AudioFormat af = new AudioFormat(8000, 16, 1, true, false);
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);

			dataline = (SourceDataLine) AudioSystem.getLine(info);

			dataline.open(af, 160 * 25);
			dataline.start();

		} catch (Exception e) {

			System.err.println("Caught exception initializing audio system " + e.getMessage());
		}

		while (true) {

			byte[] data = socket.recv();

			try {

				// audio
				if (data.length == 300) {

					int errors = oCodec.processData(data, audio);

					DefaultTableModel model = MainPanel.getModel();

					if (model != null) {
						MainPanel.getModel().setValueAt(new Integer(errors).toString(), instance - 1, 4);
					} else {

						int b = 100;
					}

					data = ShortToByte_Twiddle_Method(audio);

					if (!muted && (MainPanel.onlyMil == false || isMil)) {
						dataline.write(data, 0, data.length);
					}

					try {
						if (MainPanel.onlyMil == false || isMil) {

							if (fos != null)
								fos.write(data);

							if (isMil) {
								MainPanel.lastMilcall = System.currentTimeMillis();

							}
						}
					} catch (Exception e) {

						System.err.println("Caught Exception writing audio to file " + audioOut.getAbsolutePath());
					}

				} else if (data.length == 6) {

					// hex code

					Date now = new Date();

					MainPanel.getModel().setValueAt(format.format(now), instance - 1, 1);

					MainPanel.getModel().setValueAt(new Long(now.getTime()), instance - 1, 6);

					String hex = new String(data);

					Aircraft craft = MainPanel.getAircraft(hex);

					if (craft != null && !hex.equals("000000")) {

						MainPanel.getModel().setValueAt(craft.getHex() + " " + craft.getType() + " " + craft.getReg(),
								instance - 1, 3);

						if (!currentHex.equals(hex)) {

							// switch audio out file
							switchAudioFile(hex, craft.getType(), craft.getReg());

							currentHex = hex;

						}

					} else if (craft == null && !hex.equals("000000")) {

						MainPanel.getModel().setValueAt(hex, instance - 1, 3);

						if (!currentHex.equals(hex)) {

							// switch audio out file
							switchAudioFile(hex, "", "");

							currentHex = hex;

						}

					}
				}

			} catch (Exception e) {

				System.err.println("Caught exception in subscriber " + e.getMessage());
			}

		}

	}

	byte[] ShortToByte_Twiddle_Method(short[] input) {
		int short_index, byte_index;
		int iterations = input.length;

		byte[] buffer = new byte[input.length * 2];

		short_index = byte_index = 0;

		for (/* NOP */; short_index != iterations; /* NOP */) {
			buffer[byte_index] = (byte) (input[short_index] & 0x00FF);
			buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);

			++short_index;
			byte_index += 2;
		}

		return buffer;
	}

	public void stop() {

		if (context != null) {
			context.close();
		}

		System.out.println("Stopping thread");

	}

	public void switchAudioFile(String hex, String type, String serial) {

		isMil = MainPanel.isMil(hex);

		if (isMil) {
			MainPanel.lastMilcall = System.currentTimeMillis();
		}

		if (MainPanel.baseOutPath.equals("NO_PCM")) {
			return;
		}

		if (!MainPanel.onlyMil || isMil) {

			MainPanel.blink();

			SimpleDateFormat format = new SimpleDateFormat("YYYYMMdd");

			String dateString = format.format(new Date());

			String currentDatePath = MainPanel.baseOutPath + "\\" + dateString;

			File directory = new File(currentDatePath);
			if (!directory.exists()) {
				directory.mkdir();
			}

			try {

				if (fos != null) {
					fos.close();
				}

				audioOut = new File(MainPanel.baseOutPath + "\\" + dateString + "\\audio_" + type + "_" + serial + "_"
						+ hex + ".pcm");

				fos = new FileOutputStream(audioOut, true);

			} catch (Exception e) {

				System.err.println("Caught exception while switching audio file " + e.getMessage());
			}
		}

	}

}
