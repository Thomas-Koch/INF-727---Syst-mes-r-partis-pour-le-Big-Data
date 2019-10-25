package master;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Master {

	public static void main(String[] args) throws IOException, InterruptedException { 

		int timeout = 3;  // Integer.parseInt(args[0]); // pour tester le programme, aller dans Run->Run configurations->Onglet Arguments->rentrer le nombre du timeout voulu
		int num_machine = 0; // décompte pour implémenter les numéros de fichiers à copier
		List<String> a = Files.readAllLines(Paths.get("DEPLOY.txt"));

		for (String line : a) {
			ProcessBuilder pb1 = new ProcessBuilder("ssh", "tkoch@"+line, "hostname");
			pb1.inheritIO();
			Process proc1 = pb1.start();
			boolean ret1 = proc1.waitFor(timeout, TimeUnit.SECONDS); // permet de renvoyer true ou false au bout de timeout pour savoir si proc est arrivé au bout

			if (ret1) { // = la machine distante a répondu avant le timeout
				System.out.println("répond \n=> création du dossier avec mkdir");
				ProcessBuilder pb2 = new ProcessBuilder("ssh", "tkoch@"+line, "mkdir -p /tmp/tkoch/splits"); 
				pb2.inheritIO();
				Process proc2 = pb2.start();
				boolean ret2 = proc2.waitFor(timeout, TimeUnit.SECONDS);

				if (ret2 ) { // = le dossier a correctement été créé
					System.out.println("dossier correctement crée \n=> copie du fichier");
					ProcessBuilder pb3 = new ProcessBuilder("scp", "/home/p5hngk/Downloads/tmp/S"+num_machine+".txt", "tkoch@"+line+":/tmp/tkoch/splits");
					pb3.inheritIO();
					Process proc3 = pb3.start();
					boolean ret3 = proc3.waitFor(timeout, TimeUnit.SECONDS);

					if (ret3) { // = le fichier .txt a correctement été copié
						System.out.println(line + " : S"+num_machine+".txt correctement copié\n=> lancement du jar");
						ProcessBuilder pb4 = new ProcessBuilder("ssh", "tkoch@"+line, "java -jar /tmp/tkoch/SLAVE.jar 0 /tmp/tkoch/splits/S"+num_machine+".txt");
						pb4.inheritIO();
						Process proc4 = pb4.start();
						boolean ret4 = proc4.waitFor(timeout, TimeUnit.SECONDS);

						if (ret4) { // = le jar a bien été lancé et a terminé
							System.out.println(".jar correctement lancé => map effectué pour S"+num_machine);
						}
						else {
							System.out.println(line + " timeout/n");
							proc4.destroy();
						}
					}
					else {
						System.out.println(line + " timeout/n");
						proc3.destroy();
					}
				}
				else {
					System.out.println(line + " timeout/n");
					proc2.destroy();	
				}
			}
			else { // = la machine distante n'a pas répondu avant le timeout
				System.out.println(line + "\n timeout\n");
				proc1.destroy();
			}
			System.out.println("\n MAP FINISHED \n");
			num_machine = num_machine + 1 ;

		}
	}
}
