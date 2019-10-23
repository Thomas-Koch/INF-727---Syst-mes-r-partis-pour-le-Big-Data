package clean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CLEAN {

	// Lancement en séquentiel du process launcher
	public static boolean ProcessLauncher(String command) throws IOException {

		ProcessBuilder builder = new ProcessBuilder(command.split(" "));
		builder.redirectErrorStream(true);
		Process process = null;
		try {
			process = builder.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		boolean running = true;
		boolean tooLong = false;

		while (running) {
			try {
				// Wait For retourne vrai si le programme est arrete
				boolean stillRunning = !process.waitFor(5, TimeUnit.SECONDS);

				// On lit la sortie standard. Si on a eu quelque chose, on continue
				if (reader.ready()) {
					// On a du monde dans le buffer. On les recupere.
					// Si on ne veut pas les récuperer, on peut faire un "reset"
					// reader.reset();
					while (reader.ready()) {
						int c = reader.read();
						System.out.print((char) c);
					}
				} else if(process.isAlive() && stillRunning) {
					// Le process n'a rien écris pendant les 5 secondes. On le tue
					tooLong = true;
					process.destroy();
				}

				running = stillRunning && !tooLong;
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return !tooLong && process.exitValue()==0;
	}	


	public static void main(String[] args) throws IOException, InterruptedException {

		boolean retourOk;
		String commande;

		// on lit le fichier des machines
		List<String> a = Files.readAllLines(Paths.get("DEPLOY.txt"));
		for (String line : a) {

			// on teste la connexion ssh par un hostname
			commande = "ssh benezeth@" + line + " hostname";
			retourOk = ProcessLauncher(commande);
			if (retourOk) {
				// timeout pas atteint
				System.out.println(line + "Succes hostname");

				// on supprime le dir benezeth
				commande = "ssh benezeth@" + line + " rm -rf /tmp/benezeth";
				retourOk = ProcessLauncher(commande);
				if (retourOk) {
					// timeout pas atteint
					System.out.println(line + "Succes suppression benezeth");

				} else {
					System.out.println(line + "Echec suppression benezeth");
				}
			} else {
				System.out.println(line + "Echec hostname");
			}

		}
	}
}
