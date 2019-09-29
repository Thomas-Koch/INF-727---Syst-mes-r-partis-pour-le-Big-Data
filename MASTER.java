import java.io.IOException;

public class Master {
	
	public static void main(String args[]) throws IOException {
	
	ProcessBuilder pb = new ProcessBuilder("java", "-jar", "/tmp/SLAVE.jar"); // ou dans le répertoire /home/p5hngk/Downloads/SLAVE.jar
	pb.inheritIO();
	pb.start();
	// pb.redirectError();
	// pb.redirectErrorStream(true);
	// pb.redirectOutput();
	
	}
}

