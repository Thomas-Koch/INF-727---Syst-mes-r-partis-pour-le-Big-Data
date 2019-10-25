package master;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

public class Master {

	public static void main(String[] args) throws IOException, InterruptedException { 

		int timeout = 3;  // Integer.parseInt(args[0]); // pour tester le programme, aller dans Run->Run configurations->Onglet Arguments->rentrer le nombre du timeout voulu
		int num_machine = 0; // décompte pour implémenter les numéros de fichiers à copier
		int index = 0; // index pour ajouter des valeurs au tableau
		List<String> a = Files.readAllLines(Paths.get("DEPLOY.txt"));
		boolean[] tableau1 = new boolean[3]; // initialisation d'un tableau 1 qui contiendra des booléen des premières commandes
		boolean tableau1[] ;

		for (String line : a) {
			ProcessBuilder pb1 = new ProcessBuilder("ssh", "tkoch@"+line, "hostname");
			pb1.inheritIO();
			Process proc1 = pb1.start();
			boolean ret1 = proc1.waitFor(timeout, TimeUnit.SECONDS);	 // permet de renvoyer true ou false au bout de timeout pour savoir si proc est arrivé au bout
			//Arrays.fill (tableau1, ret1);
			tableau[index] = ret1 ; // ajout de ret dans le tableau 1
			index++;
		} // fin de boucle for
		int index = 0 ; // réinitilaisation de l'index
		
		// tests des valeurs du tableau 1
		boolean tableau2[]; // initialisation d'un tableau 2 qui contiendra les booléens des deuxièmes commandes
		// int i = 0 ;
		for (int i = 0 ; String line : a ; i++) {
			if (tableau1[i] = false) { // = la machine distante n'a pas répondu
				a.remove(line); // suppression de la machine de la liste a (FONCTION A TROUVER)
				System.out.println(line + "\n timeout\n");
			}
			else { // = la machine distante a bien répondu
				// on peut lancer une nouvelle commande
				System.out.println(line + "répond \n=> création du dossier avec mkdir");
				ProcessBuilder pb2 = new ProcessBuilder("ssh", "tkoch@"+line, "mkdir -p /tmp/tkoch/splits"); 
				pb2.inheritIO();
				Process proc2 = pb2.start();
				boolean ret2 = proc2waitFor(timeout, TimeUnit.SECONDS);
				tableau2[index] = ret2 ; // ajout de ret2 dans le tableau 2
				index++; 
			}
			// i = i+1 ;	
		} // fin de boucle for
		int index = 0 ; // réinitialisation de l'index
		
		// tests des valeurs du tableau 2
		boolean tableau3[] ; // initialisation d'un tableau 3 qui contiendra les booléens des troisièmes commandes lancées
		for (int i = 0 ; String line : a ; i++) {
			if(tableau2[i] = false) {
				a.remove(line); // COMMANDE A TROUVER
				System.out.prinln(line + "\n timeout\n");
			}
			else { // = la machine a bien effectuée le proc2
				// on peut lancer une nouvelle commande
				System.out.println("dossier correctement crée \n=> copie du fichier");
				ProcessBuilder pb3 = new ProcessBuilder("scp", "/home/p5hngk/Downloads/tmp/S"+num_machine+".txt", "tkoch@"+line+":/tmp/tkoch/splits");
				pb3.inheritIO();
				Process proc3 = pb3.start();
				boolean ret3 = proc3.waitFor(timeout, TimeUnit.SECONDS);
				tableau3[index] = ret3 ;// ajout de ret3  dans le tableau 3
				index++ ;
			}
		} // fin de boucle for
		int index = 0 ;
		
		// tests des valeurs du tableau 3
		boolean tableau4[] ;// initialisation d'un tableau 4 qui contiendra les booléens des quatrièmes commandes lancées
		for (int i = 0 ; String line : a ; i++) {
			if(tableau3[i] = false) {
				a.remove(line); // COMMANDE A TROUVER
				System.out.prinln(line + "\n timeout\n");
			}
			else { // = la machine a bien effectuée le proc3
				// on peut lancer une nouvelle commande
				System.out.println(line + " : S"+num_machine+".txt correctement copié\n=> lancement du jar");
				ProcessBuilder pb4 = new ProcessBuilder("ssh", "tkoch@"+line, "java -jar /tmp/tkoch/SLAVE.jar 0 /tmp/tkoch/splits/S"+num_machine+".txt");
				pb4.inheritIO();
				Process proc4 = pb4.start();
				boolean ret4 = proc4.waitFor(timeout, TimeUnit.SECONDS);
				tableau4[index = ret4 ;]// ajout de ret4  dans le tableau 4
				index++ ;
			}
		} // fin de boucle for
		int index = 0;
		
		// tests des vlaeurs du tableau 4
		int num_machine = 0; // décompte pour implémenter les numéros de fichiers à copier
		for (String line : a) {
			if(el tableau 4 == false) {
				a.remove(line) ; // COMMANDE A TROUVER
				System.out.prinln(line + "\n timeout\n");
			}
			else { // = la machine a bien effectuée le proc3
				// on peut afficher l bon fonctionnement du programme
				System.out.println(".jar correctement lancé => map effectué pour S"+num_machine);
				num_machine = num_machine + 1 ;
			}
		} // fin de boucle for
		
		System.out.println("\n MAP FINISHED \n");
		

	} 
}
