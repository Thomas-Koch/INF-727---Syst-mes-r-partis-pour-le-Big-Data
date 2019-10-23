package master;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MASTER {

	// Lancement d'un processus
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
		int num_ordi = 0;
		
		// on lit le fichier des machines
		List<String> a = Files.readAllLines(Paths.get("DEPLOY.txt"));
		for (String line : a) {
			
			// on teste la connexion ssh par un hostname
			commande = "ssh benezeth@" + line + " hostname";
			retourOk = ProcessLauncher(commande);
			if (retourOk) {
				// timeout pas atteint
				System.out.println("Succes hostname");
			
				// on crée le dir splits
				commande = "ssh benezeth@" + line + " mkdir -p /tmp/benezeth/splits";
				retourOk = ProcessLauncher(commande);
				if (retourOk) {
					// timeout pas atteint
					System.out.println("Succes creation splits");
					
					// on envoie les fichiers Sx dans splits
					commande = "scp splits/S"+ num_ordi +".txt benezeth@" + line + ":/tmp/benezeth/splits/";
					retourOk = ProcessLauncher(commande);
					if (retourOk) {
						// timeout pas atteint
						System.out.println("Succes copie Sx");
						
						// on lance SLAVE phase 0 : map
						commande = "ssh benezeth@" + line + " java -jar /tmp/benezeth/SLAVE.jar 0 /tmp/benezeth/splits/S"+ num_ordi +".txt";
						retourOk = ProcessLauncher(commande);
						if (retourOk) {
							// timeout pas atteint
							System.out.println("Succes map UM");
							
						} else {
							System.out.println("Echec map UM");
						}
					} else {
						System.out.println("Echec copie Sx");
					}
				} else {
					System.out.println("Echec creation splits");
				}
			} else {
				System.out.println("Echec hostname");
			}
			num_ordi = num_ordi + 1;
		}

		// fin de la phase 0 : map
		System.out.println("MAP FINISHED");
		


		// Phase 1 : préparation du Shuffle
		num_ordi = 0;

		for (String line : a) {

			// on lance SLAVE phase 1 : prepa du shuffle
			commande = "ssh benezeth@" + line + " java -jar /tmp/benezeth/SLAVE.jar 1 /tmp/benezeth/maps/UM"+ num_ordi +".txt";
			retourOk = ProcessLauncher(commande);
			if (retourOk) {
				// timeout pas atteint
				System.out.println(line + "Succes Slave 1");

			} else {
				System.out.println(line + "Echec Slave 1");
			}
			num_ordi = num_ordi + 1;
		}
	}
}
