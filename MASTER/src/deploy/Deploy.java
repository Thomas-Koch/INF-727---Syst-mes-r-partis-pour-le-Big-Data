package deploy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Deploy {

	public static void main(String[] args) throws IOException, InterruptedException { 

		int timeout = 3;  // Integer.parseInt(args[0]); // pour tester le programme, aller dans Run->Run configurations->Onglet Arguments->rentrer le nombre du timeout voulu

		List<String> a = Files.readAllLines(Paths.get("DEPLOY.txt"));

		for (String line : a) {
			ProcessBuilder pb = new ProcessBuilder("ssh", "tkoch@"+line, "hostname");
			pb.inheritIO();
			Process proc = pb.start();
			boolean ret = proc.waitFor(timeout, TimeUnit.SECONDS); // permet de renvoyer true ou false pour savoir si le timeout est arrivé au bout avant que le process ne soit fini ou pas

			if (ret) { // = la machine distante a répondu avant le timeout
				System.out.println("répond \n=> création du dossier mkdir");
				ProcessBuilder pb2 = new ProcessBuilder("ssh", "tkoch@"+line, "mkdir /tmp/tkoch"); 
				pb2.inheritIO();
				Process proc2 = pb2.start();
				boolean ret2 = proc2.waitFor(timeout, TimeUnit.SECONDS);

				if (ret2 ) { // = le dossier a correctement été créé
					System.out.println("dossier correctement crée \n=> copie du fichier jar");
					ProcessBuilder pb3 = new ProcessBuilder("scp", "/home/p5hngk/SLAVE.jar", "tkoch@"+line+":/tmp/tkoch");
					pb3.inheritIO();
					Process proc3 = pb3.start();
					boolean ret3 = proc3.waitFor(timeout, TimeUnit.SECONDS);

					if (ret3) { // = le fichier .jar a correctement été copié
						System.out.println("Copie SLAVE.jar terminée\n");
						ProcessBuilder pb4 = new ProcessBuilder("scp /home/p5hngk/eclipse-workspace/MASTER/DEPLOY.txt tkoch"+line+":/tmp/tkoch");
						pb4.inheritIO();
						Process proc4 = pb4.start();
						boolean ret4 = proc4.waitFor(timeout, TimeUnit.SECONDS);

						if (ret4) {
							System.out.println("Copie DEPLOY.txt terminée\n");
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
					System.out.println(line + " timeout\n");
					proc2.destroy();
				}
			}
			else { // = la machine distante n'a pas répondu avant le timeout
				System.out.println(line + " timeout\n");
				proc.destroy();
			}
		}

	}
}