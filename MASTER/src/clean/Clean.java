package clean;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Clean {

	public static void main(String[] args) throws IOException, InterruptedException { 

		int timeout = 3;  // Integer.parseInt(args[0]); // pour tester le programme, aller dans Run->Run configurations->Onglet Arguments->rentrer le nombre du timeout voulu

		List<String> a = Files.readAllLines(Paths.get("DEPLOY.txt"));

		for (String line : a) {
			ProcessBuilder pb1 = new ProcessBuilder("ssh", "tkoch@"+line, "hostname");
			pb1.inheritIO();
			Process proc1 = pb1.start();
			boolean ret1 = proc1.waitFor(timeout, TimeUnit.SECONDS); // permet de renvoyer true ou false pour savoir si le timeout est arrivé au bout avant que le process ne soit fini ou pas
			if (ret1) { // = la machine distante a répondu avant le timeout
				System.out.println("répond \n=> supression du dossier tkoch");
				ProcessBuilder pb2 = new ProcessBuilder("ssh", "tkoch@"+line, "rm -rf /tmp/tkoch/"); 
				pb2.inheritIO();
				Process proc2 = pb2.start();
				Thread.sleep(5000); // attente pour assurer la bonne supression du fichier
				boolean ret2 = proc2.waitFor(timeout, TimeUnit.SECONDS);
				if (ret2) { // = le dossier a correctement été créé
					System.out.println("dossier correctement supprimé \n");
				}
				else {
					System.out.println("erreur de suppression !");
					proc2.destroy();
				}	
			}
			else { // = la machine distante n'a pas répondu avant le timeout
				System.out.println(line + "\n timeout\n");
				proc1.destroy();
			}
		}

	}
}

