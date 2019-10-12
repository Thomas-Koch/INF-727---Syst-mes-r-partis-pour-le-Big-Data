
	// Lancement en séquentiel du process launcher
	public static boolean sequentialProcessLauncher(String command) throws IOException {

		ProcessBuilder builder = new ProcessBuilder(command.split(" "));
		builder.redirectErrorStream(true);
		Process process = builder.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		boolean running = true;
		boolean tooLong = false;
		while (running) {
			try {
				// Wait For retourne vrai si le programme est arrete
				// On regarde toutes les deux secondes pour savoir si le processus est toujours en cours ou non
				boolean stillRunning = !process.waitFor(2, TimeUnit.SECONDS); 

				// On lit la sortie standard. Si on a eu quelque chose, on continue
				String line = reader.readLine();
				if (line != null) {
					// On peut ecrire sur sysout
					while (line != null) {
						System.out.println(line);
						line = reader.readLine();
					}
				} else {
					// Le process n'a rien écris pendant les 2 secondes. On le tue
					tooLong = true;
					process.destroy();
				}
				running = stillRunning && !tooLong;
			} catch (IOException| InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return !tooLong;

	}

	public static void main(String[] args) throws IOException {
		boolean value = sequentialProcessLauncher("sleep 10");
	}
}