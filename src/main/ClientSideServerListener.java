package main;

public class ClientSideServerListener implements Runnable {
		private ClypeClient client;

		public ClientSideServerListener(ClypeClient client) {
			this.client = client;
		}

		/***
		 * Runs the listener.
		 */
		public void run() {
			while (!client.closed()) {
					client.printData();
			}
		}
}