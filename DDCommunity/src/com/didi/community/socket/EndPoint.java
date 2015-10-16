
package com.didi.community.socket;

import java.io.IOException;


public interface EndPoint extends Runnable {
	public Serialization getSerialization ();

	/** If the listener already exists, it is not added again. */
	public void addListener (Listener listener);

	public void removeListener (Listener listener);

	/** Continually updates this end point until {@link #stop()} is called. */
	public void run ();

	/** Starts a new thread that calls {@link #run()}. */
	public void start ();

	/** Closes this end point and causes {@link #run()} to return. */
	public void stop ();

	/** @see Client
	 * @see Server */
	public void close ();

	/** @see Client#update(int)
	 * @see Server#update(int) */
	public void update (int timeout) throws IOException;

	/** Returns the last thread that called {@link #update(int)} for this end point. This can be useful to detect when long running
	 * code will be run on the update thread. */
	public Thread getUpdateThread ();

}
