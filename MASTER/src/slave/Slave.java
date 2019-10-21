package slave;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Slave {

	public static void main(String args[]) throws IOException, InterruptedException {

		int timeout = 3;  // Integer.parseInt(args[0]); // pour tester le programme, aller dans Run->Run configurations->Onglet Arguments->rentrer le nombre du timeout voulu
		switch (args[0]) {
		case "0" : // phase de MAP

			// création du dossier maps
			String commande = "mkdir -p /tmp/tkoch/maps";
			ProcessBuilder maps = new ProcessBuilder(commande.split(" "));
			maps.inheritIO();
			Process proc_maps = maps.start();
			boolean ret = proc_maps.waitFor(timeout, TimeUnit.SECONDS); // permet de renvoyer true ou false au bout de timeout pour savoir si proc est arrivé au bout
			if (ret) { // = la machine distante a répondu avant le timeout

				// lecture du fichier Sx.txt
				List<String> a = Files.readAllLines(Paths.get(args[1]));

				// écriture du fichier UMx.txt
				PrintWriter writer = new PrintWriter("/tmp/tkoch/maps/UM" + args[1].charAt(args[1].length()-5) + ".txt", "UTF-8");
				for (String line : a) {
					for (String mot : line.split(" ")) {
						writer.println(mot + " 1");
					}
				}
				writer.close();
				break;
			}
			else {
				System.out.println("timeout => erreur de lancement du jar./n");
				proc_maps.destroy();
			}

		}

	}
}
