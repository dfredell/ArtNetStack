/**
 * 
 */
package fr.azelart.artnetstack.runners;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import fr.azelart.artnetstack.domain.artpoll.ArtPoll;
import fr.azelart.artnetstack.domain.arttimecode.ArtTimeCodeType;
import fr.azelart.artnetstack.domain.controller.Controller;
import fr.azelart.artnetstack.domain.controller.ControllerController;
import fr.azelart.artnetstack.domain.controller.ControllerGoodInput;
import fr.azelart.artnetstack.domain.controller.ControllerGoodOutput;
import fr.azelart.artnetstack.domain.controller.ControllerPortType;
import fr.azelart.artnetstack.domain.enums.PortInputOutputEnum;
import fr.azelart.artnetstack.domain.enums.PortTypeEnum;
import fr.azelart.artnetstack.listeners.ArtNetPacketListener;
import fr.azelart.artnetstack.listeners.ServerListener;
import fr.azelart.artnetstack.server.ArtNetServer;
import fr.azelart.artnetstack.tools.timecode.TimeCodeTask;
import fr.azelart.artnetstack.utils.ArtNetPacketEncoder;

/**
 * Server Runner.
 * @author Corentin Azelart
 *
 */
public class ServerStart {
	
	private static Controller thisControler;
	
	private static void createControler() {
		thisControler = new ControllerController();
		
		// Create one port
		final Map<Integer,ControllerPortType> vPorts = new HashMap<Integer, ControllerPortType>();
		ControllerPortType vPort1 = new ControllerPortType();
		vPort1.setInputArtNet( true );
		vPort1.setOutputArtNet( true );
		vPort1.setPort( 0 );
		vPort1.setType( PortTypeEnum.DMX512 );
		vPort1.setDirection( PortInputOutputEnum.BOTH );
		vPorts.put(0, vPort1);
		
		// Set status port1 input
		final Map<Integer,ControllerGoodInput> vGoodInputsMap = new HashMap<Integer, ControllerGoodInput>();
		ControllerGoodInput vGoodInput1 = new ControllerGoodInput();
		vGoodInput1.setDisabled( false );
		vGoodInput1.setDataReceived( true );
		vGoodInput1.setIncludeDMXSIPsPackets( true );
		vGoodInput1.setIncludeDMXTestPackets( true );
		vGoodInput1.setIncludeDMXTextPackets( true );
		vGoodInput1.setReceivedDataError( false );
		vGoodInputsMap.put(0, vGoodInput1);
		
		// Set status port1 output
		final Map<Integer,ControllerGoodOutput> vGoodOutputsMap = new HashMap<Integer, ControllerGoodOutput>();
		ControllerGoodOutput vGoodOutput1 = new ControllerGoodOutput();
		vGoodOutput1.setDataTransmited( true );
		vGoodOutput1.setOutputPowerOn( true );
		vGoodOutput1.setOutputMergeArtNet( false );
		vGoodOutput1.setMergeLTP( false );
		vGoodOutput1.setIncludeDMXTextPackets( false );
		vGoodOutput1.setIncludeDMXTestPackets( false );
		vGoodOutput1.setIncludeDMXSIPsPackets( false );
		vGoodOutputsMap.put(0, vGoodOutput1);
		
		// Display
		thisControler.setScreen( false );
		
		thisControler.setGoodOutputMapping( vGoodOutputsMap );
		thisControler.setGoodInputMapping(vGoodInputsMap);
		thisControler.setPortTypeMap(vPorts);
	}
	
	/**
	 * Start program method.
	 * @param args
	 */
	public static void main(String[] args) {
		final ArtNetServer artNetServer;
		createControler();
		
		
		try {
			artNetServer = new ArtNetServer();
			
			// Server listener
			artNetServer.addListenerServer( new ServerListener() {
				
				public void onConnect() {
					System.out.println("Connected");
					
				}
			} );
			
			// Packet Listener
			artNetServer.addListenerPacket( new ArtNetPacketListener() {
				


				public void onArtPoll(ArtPoll artPoll) {
					try {
						artNetServer.sendPacket( ArtNetPacketEncoder.encodeArtPollReplyPacket( thisControler ) );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				
			} );
			
			
			
			artNetServer.start();
			
			
			/**
			 * Configure a time code for example.
			 */
			final Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimeCodeTask(artNetServer, ArtTimeCodeType.SMPTE), 0, 1000);
			
			
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		
	}

}
