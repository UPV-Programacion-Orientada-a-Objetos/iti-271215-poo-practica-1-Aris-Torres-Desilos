package edu.upvictoria.fpoo;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input;

        try {
            System.out.print("Ingrese un comando ('exit;' para terminar): ");
            while (true) {
                input = reader.readLine();

                while (true){
                    if (input.equals("exit;")) {
                        System.out.println("Saliendo del programa...");
                        break;
                    } else if (input.toUpperCase().startsWith("INSERT INTO")) {
                        INSERT(input);
                    }
                }

                // Aquí puedes realizar cualquier operación adicional con la entrada del usuario
                //System.out.println("Comando ingresado: " + input);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void INSERT(String input) {
        Pattern sql = Pattern.compile("INSERT INTO (\\w+) \\((.*?)\\) VALUES \\((.*?)\\);",Pattern.CASE_INSENSITIVE);
        Matcher matcher = sql.matcher(input);
    }
}
