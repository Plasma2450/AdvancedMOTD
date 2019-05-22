package ProtocolWrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedServerPing;

public class WrapperStatusServerServerInfo extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Status.Server.SERVER_INFO;

	public WrapperStatusServerServerInfo() {
		super(new PacketContainer(TYPE), TYPE);
		handle.getModifier().writeDefaults();
	}

	public WrapperStatusServerServerInfo(PacketContainer packet) {
		super(packet, TYPE);
	}

	/**
	 * Retrieve JSON Response.
	 * <p>
	 * Notes: https://gist.github.com/thinkofdeath/6927216
	 * 
	 * @return The current JSON Response
	 */
	public WrappedServerPing getJsonResponse() {
		return handle.getServerPings().read(0);
	}

	/**
	 * Set JSON Response.
	 * 
	 * @param value - new value.
	 */
	public void setJsonResponse(WrappedServerPing value) {
		handle.getServerPings().write(0, value);
	}

}