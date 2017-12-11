package main;

public class ClientSideServerListener implements Runnable {
		private ClypeClient client;

		public ClientSideServerListener(ClypeClient client) {
			this.client = client;
		}

		/***
		 * Runs the listener.
		 */
		@Override
		public void run() {
			while (!client.closed()) {
				client.receiveData();
				client.printData();
			}
			return;
		}
}