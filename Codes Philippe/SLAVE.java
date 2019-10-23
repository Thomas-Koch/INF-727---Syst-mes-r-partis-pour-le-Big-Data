package slave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SLAVE {

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

	
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		boolean retourOk;
		String commande;
		PrintWriter writer;
		ProcessBuilder builder;
		int nbMachines = 3;  // nb de machines dans le cluster
		
		// récupération du nom de la machine
		String hostname = java.net.InetAddress.getLocalHost().getHostName();
		
		// chargement d'un dico cluster (numéro, nom machine)
		HashMap<String, String> cluster =new HashMap<>();
		List<String> d = Files.readAllLines(Paths.get("/tmp/benezeth/machines.txt"));
		
		int rank = 0; // rang dans le cluster
		String numMachine =""; // rang dans le cluster de la machine en local
		
		for (String machine : d) {
			cluster.put(Integer.toString(rank), machine);
			if (hostname.equals(machine)) {
				numMachine = Integer.toString(rank);
			}
			rank += 1;
		}
		
		switch (args[0]) {
		case "0" :
		// 0 veut dire Map
			
			// savoir sur quelle machine on est
			//numMachine = Character.toString(args[1].charAt(args[1].length()-5));
			
			// création du dir maps		
			commande = "mkdir -p /tmp/benezeth/maps";
			builder = new ProcessBuilder(commande.split(" "));
			builder.start();
			//Thread.sleep(2000);
			
			// lecture du fichier Sx.txt
			List<String> a = Files.readAllLines(Paths.get(args[1]));
			
			// ecriture du fichier UMx.txt
			writer = new PrintWriter("/tmp/benezeth/maps/UM" + numMachine + ".txt", "UTF-8");
			for (String line : a) {
				for (String mot : line.split(" ")) {
					writer.println(mot + " 1");
				}				
			}
			writer.close();
			break ;
		
		case "1" :
		// 1 veut dire calcul du hash et shuffle	
			System.out.println("num machine" + numMachine );
			
			
			// création du dir shuffles		
			commande = "mkdir -p /tmp/benezeth/shuffles";
			builder = new ProcessBuilder(commande.split(" "));
			builder.start();
			//Thread.sleep(2000);
					
			// Initialisation
			int hash = 0;
			String nomFileShuffles = "";  // constitué du hash et du nom de la machine locale avec dir complet
			String machineCible ="";  // machine vers laquelle  on envoie le fichier hash 
			String mot_encours ="";  // gestion du changement de mot
			boolean firstWord = true;
			
			// obligation de gérer le 1er writer (il faudrait le supprimer à la fin)
			writer = new PrintWriter("/tmp/benezeth/initwriter.txt", "UTF-8");
			writer.close();
			
			// lecture du fichier UMx.txt
			List<String> b = Files.readAllLines(Paths.get(args[1]));
			// Remarque : on considère les mots triés
			for (String line : b) {
				String mot = line.split(" ")[0]; // on considère le mot pas le nombre 1
				
				// 1er mot ou nouveau mot
				if (firstWord | !mot.equals(mot_encours)) {
					
					if (!firstWord) {
						// on ferme le fichier en cours
						writer.close();
						
						// trouver le nom de la machine cible
						machineCible = cluster.get(Integer.toString((hash % nbMachines)));
						
						// on envoie le fichier sur la machine calculée via hash que si autre que elle même
						if (!machineCible.equals(hostname)) {
							commande = "scp " + nomFileShuffles + " " + machineCible + ":/tmp/benezeth/shuffles/";
							retourOk = ProcessLauncher(commande);
							if (retourOk) {
								// timeout pas atteint
								System.out.println("Succes envoi shuffles");	
							} else {
								System.out.println("Echec envoi shuffles");
							}
						}	
					}
					// calcul du hash
					hash = mot.hashCode();
					mot_encours = mot;
					firstWord = false;
					
					// on cree un nouveau fichier
					nomFileShuffles = "/tmp/benezeth/shuffles/"  + hash + "-" + hostname + ".txt";
					
					writer = new PrintWriter(nomFileShuffles, "UTF-8");
					writer.println(mot + " 1");
					
				// même mot	
				} else {	
					// on ajoute une ligne dans le fichier
					writer.println(mot + " 1");
				}							
			}
			
			writer.close(); // on ferme pour le dernier mot
			
			// trouver le nom de la machine cible
			machineCible = cluster.get(Integer.toString((hash % nbMachines)));
			
			// on envoie le fichier sur la machine calculée via hash que si autre que elle même
			if (machineCible != hostname) {
				
				// on envoie le fichier 
				commande = "scp " + nomFileShuffles + " " + machineCible + ":/tmp/benezeth/shuffles/";
				retourOk = ProcessLauncher(commande);
				if (retourOk) {
					// timeout pas atteint
					System.out.println("Succes envoi shuffles");
				} else {
					System.out.println("Echec envoi shuffles");
				}
			}
			break ;
		}
	}
	
}