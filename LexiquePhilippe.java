import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.*;


/* TP du 4 sept 2019 - Exercice 5 du pdf
 * Générer un lexique de traduction à partir de 2 ensembles de phrases appariées (fr - en) 
 */
public class Lexique {

 
	public static HashMap<String,Integer> countSentencesWithWord (String fileName) throws IOException   {
	/* retourne, pour chaque mot (fr) unique, le nombre de phrases d'occurence */
	 	 
		HashMap<String,Integer> comptePhrase = new HashMap<> (); // contiendra le mot unique en clé, le nombre de phrases en valeur
		List<String> fic = Files.readAllLines(Paths.get(fileName)); // lit en 1 coup toutes les lignes du fichier
   
		for (String line : fic) { // on boucle sur les phrases
			String[] tab=line.split(" ");
			HashSet<String> uniqWord = new HashSet<String>(Arrays.asList(tab)); 
			/* passe le contenu d'un tableau vers un HashSet pour ne garder qu'une fois chaque mot de la phrase */
	   
			for (String el : uniqWord) {
				if (comptePhrase.containsKey(el)){
					comptePhrase.put(el, comptePhrase.get(el)+1);
				} else {
					comptePhrase.put(el,1);
				}
			} 
		}  
		return comptePhrase;
	}
 
	
	public static HashMap<String,HashMap<String,Integer>> buildContTable (String fileName1,String fileName2) throws IOException   {
	/* retourne pour chaque mot (fr) unique, la liste des mots (en) de toutes les phrases d'occurence
	 *  avec le nombre de phrases pour chacun */
		
		HashMap<String,HashMap<String,Integer>> cooccurence = new HashMap<> ();// contiendra le mot unique en clé, les paires(mot, nb de phrases) en valeur
		List<String> fic1 = Files.readAllLines(Paths.get(fileName1)); // lit en 1 coup toutes les lignes du fichier fr
		List<String> fic2 = Files.readAllLines(Paths.get(fileName2)); // lit en 1 coup toutes les lignes du fichier en
		
		int numero = 0; // permet de garder l'index de lecture du fichier
		for (String line1 : fic1) {
			String line2 = fic2.get(numero); // accès à une List par son index
			numero++;
			String[] tab1=line1.split(" ");
			String[] tab2=line2.split(" ");
			HashSet<String> uniqWord1 = new HashSet<String>(Arrays.asList(tab1)); 
			HashSet<String> uniqWord2 = new HashSet<String>(Arrays.asList(tab2)); 
			/* passe le contenu d'un tableau vers un HashSet pour ne garder qu'une fois chaque mot de la phrase */
      
			for (String el1 : uniqWord1) { // boucle sur les mots (fr) d'une phrase
				if (cooccurence.containsKey(el1)){ // le mot (fr) existe déjà
					HashMap<String, Integer> mot2 = cooccurence.get(el1); // mot2 va contenir les paires(mot, nb de phrases)
					for (String el2 : uniqWord2) { // boucle sur les mots (en) de la phrase traduction (en)
						if (mot2.containsKey(el2)) {  // le mot (en) existe déjà
							mot2.put(el2, mot2.get(el2)+1);
						} else { // le mot (fr) n'existe pas encore
							mot2.put(el2, 1);
						}
					}
					cooccurence.put(el1,mot2);
				} else {     // le mot (fr) n'existe pas encore
					HashMap<String, Integer> mot2 = new HashMap<>(); // mot2 va contenir les paires(mot, nb de phrases)
					for (String el2 : uniqWord2) {
						mot2.put(el2, 1);
					}
     				cooccurence.put(el1,mot2);
				}
			} 
		}
    	return cooccurence;
    }
 
  
 
	public static void printSortedCoocTable (HashMap<String,HashMap<String,Integer>> cooc) {
	/* tri en ordre décroissant du nombre de phrases des paires (mot, nb de phrases) 
	 *  cooc est du format de cooccurence */ 
		
		for (String el1 : cooc.keySet()) { // on boucle sur les mots (fr)
			HashMap<String, Integer> mot2 = cooc.get(el1); // mot2 va contenir les paires(mot, nb de phrases)
			HashMap<String, Integer> mottri = new HashMap<>(); // mot2 va contenir les paires(mot, nb de phrases) triées
   
			mottri = mot2 // algo de tri sur valeur copié sur Google...
					.entrySet()
					.stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
					.collect(
							toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
									LinkedHashMap::new));
     
			cooc.put(el1,  mottri); 
		}
		
		/* on limite l'impression à n lignes */
		int maxlignes = 0;
		for (Entry<String,HashMap<String,Integer>> el : cooc.entrySet()) {
			System.out.println(el);
			maxlignes++;
			   if (maxlignes==5) break;
		}
	}
 
	public static double likelihoodRatio(int n, int na, int nb, int nab) {
	/* calcul du ratio magique qui indique la vraisemblance de la traduction */
		double ratio;
		ratio = 2*(
				nab*Math.log(1.0*nab*n/(na*nb))+ // a et b
				(na-nab)*Math.log(1.0*(na-nab)*n/(na*(n-nb)))+ // a et -b
				(nb-nab)*Math.log(1.0*(nb-nab)*n/((n-na)*nb))+ // -a et b
				(n-na-nb+nab)*Math.log(1.0*(n-na-nb+nab)*n/((n-na)*(n-nb))) // -a et -b)
				);
		return ratio;
	}
 
	public static HashMap<String, Double> ratio (HashMap<String,HashMap<String,Integer>> cooc, int n)throws IOException {
	/* création du HashMap restri (mot fr, likelihood ratio) par ratio décroissant */
		
		HashMap<String, Double> res = new HashMap<>();
		HashMap<String, Double> restri = new HashMap<>();
		
		// n nombre total de phrases 
		System.out.println("Il y a " + n + " phrases dans chaque corpus");
  
		HashMap<String,Integer> nbfr =countSentencesWithWord ("corpusfr.txt");
		HashMap<String,Integer> nben =countSentencesWithWord ("corpusen.txt");
		/* retourne, pour chaque mot (fr) et (en) , le nombre de phrases d'occurence */
		System.out.println("Il y a " + nbfr.size() + " mots différents dans le corpus français");
		System.out.println("Il y a " + nben.size() + " mots différents dans le corpus anglais");
  
		for (Entry<String,HashMap<String,Integer>> el : cooc.entrySet()) { 
		/* on boucle sur cooc qui contient le mot (fr) en clé, les paires(mot, nb de phrases) en valeur */
			String motfr = el.getKey(); // mot (fr)
			/* si le mot est un sigle de ponctuation on shunte */
			if (motfr.equals("?") || motfr.equals("!") || motfr.equals(".") || motfr.equals(",") || motfr.equals("-")) continue;
			
			int na=nbfr.get(motfr); // nombre de phrases avec le mot fr
     
			for (Entry<String,Integer> motfreq2 : el.getValue().entrySet()){
			/* on boucle sur les mots (en) en clé, le nb de phrases d'apparition en valeur */	
				
				String moten = motfreq2.getKey(); // mot (en)
				/* si le mot est un sigle de ponctuation on shunte */
				if (moten.equals("?") || moten.equals("!") || moten.equals(".") || moten.equals(",") || moten.equals("-")) continue;
								
				int nb=nben.get(moten); // nombre de phrases avec le mot en
				int nab=motfreq2.getValue(); // nombre de phrases avec le mot fr et en
				
				/* on aligne les mots (fr) et (en) */
				StringBuilder motBlanc1 = new StringBuilder("               ");
				motBlanc1=motBlanc1.delete(0, motfr.length());
				motBlanc1=motBlanc1.insert(0,motfr);
				StringBuilder motBlanc2 = new StringBuilder("               ");
				motBlanc2=motBlanc2.delete(0, moten.length());
				motBlanc2=motBlanc2.insert(0,moten);
								
				String motconcat= motBlanc1 +	" - " + motBlanc2; // on crée la concaténation mot (fr) et mot (en)
				    
				Double r=likelihoodRatio(n,na,nb,nab); // calcul du ratio et passage en Double pour la comparaison à Nan et l'écriture dans res
				if (!r.equals(Double.NaN)) res.put(motconcat, r); // on ne crée pas si ratio infini (non défini) 
			}
		}
  
    
		restri = res // copié sur Google...
				.entrySet()
				.stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(
						toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
								LinkedHashMap::new));
  
		/* on limite l'impression à n lignes */
		int maxlignes = 0;
		for (Entry<String, Double> el : restri.entrySet()) {
			System.out.println(el);
			maxlignes++;
			if (maxlignes==50) break;
		}  
		return res;
	}
 
	public static void main(String[] args) throws IOException{
		String fileName1 = "corpusfr.txt";
		String fileName2= "corpusen.txt";
  
		System.out.println(countSentencesWithWord(fileName1));
		System.out.println();
  
		printSortedCoocTable(buildContTable(fileName1, fileName2));
		System.out.println();
		
		List<String> fic = Files.readAllLines(Paths.get(fileName1)); // lit en 1 coup toutes les lignes du fichier
		int n = fic.size(); // le nombre de phrases du corpus
		
		ratio(buildContTable(fileName1, fileName2),n);
	
	}

}