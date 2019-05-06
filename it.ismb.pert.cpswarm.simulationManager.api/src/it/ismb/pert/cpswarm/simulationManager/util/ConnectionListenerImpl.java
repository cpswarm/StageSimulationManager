package it.ismb.pert.cpswarm.simulationManager.util;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import it.ismb.pert.cpswarm.simulationManager.api.SimulationManager;

/**
 *
 * Implementation of the listener of the connection status
 *
 */
public /*final*/ class ConnectionListenerImpl implements ConnectionListener {

	private final SimulationManager parent;

	/**
	 * Constructor of the listener of the connection
	 *
	 * @param parent
	 *            <code>XMPPClient</code> instance
	 *
	 *
	 * @throws AssertionError
	 *             if something is wrong
	 */
	public ConnectionListenerImpl(final SimulationManager parent) {
		assert parent != null;
		this.parent = parent;
		System.out.println("\n connection listener bound to MA =  "+ parent.getClientID());
	}

	@Override
	public void connectionClosed() {
		System.out.println(
				"XMPPClient The connection was closed normally.");
		// TODO
		// handle disconnection
	}

	@Override
	public void connectionClosedOnError(final Exception arg0) {
		System.out.println(
				"XMPPClient the connection was closed due to an exception.");
		// TODO
		// handle disconnection
	}


	@Override
	public void connected(final XMPPConnection arg0) {
		System.out.println(
				"The connection has connected successfully to the server.");
	}

	@Override
	public void authenticated(final XMPPConnection paramXMPPConnection,
			final boolean paramBoolean) {
		System.out.println(
				"The connection has authenticated successfully to the server."
						+ ((paramBoolean) ? " The connection has been resumed"
								: ""));
	}
}
