import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Master {

	public static void main(String[] args) throws IOException, InterruptedException { 

		int timeout = Integer.parseInt(args[0]); // pour tester le programme, aller dans Run->Run configurations->Onglet Arguments->rentrer le nombre du timeout voulu

		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/tkoch/SLAVE.jar"); // ou dans le répertoire /home/p5hngk/Downloads/SLAVE.jar
		pb.inheritIO();
		Process proc = pb.start();
		boolean ret = proc.waitFor(timeout, TimeUnit.SECONDS); // permet de renvoyer true ou false pour savoir si le timeout est arrivé au bout avant que le process ne soit fini ou pas
		if (ret) {
			System.out.println("Pas de timeout, le process est allé à son terme");
		}
		else {
			System.out.println("Timeout, le process a été stoppé avant son terme");
			proc.destroy();
		}
		// pb.redirectError();
		// pb.redirectErrorStream(true);
		// pb.redirectOutput();

	}
}


