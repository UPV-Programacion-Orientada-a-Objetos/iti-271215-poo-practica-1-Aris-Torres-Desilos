package edu.upvictoria.fpoo;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.join;

public class SQLCommand {
    public String[] rowData;
    public String line;
    public String[] columnNames;
    public String[] selectedColumns;
    public String currentDatabase = "";

    public SQLCommand() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder commandBuilder = new StringBuilder();
        boolean multiLineInput = false;

        System.out.print("-- ");

        try {
            while (true) {
                while (true) {
                    String line = reader.readLine();
                    commandBuilder.append(line).append(" ");

                    if (line.trim().endsWith(";")) {
                        String command = commandBuilder.toString().trim();

                        if (command.equalsIgnoreCase("exit;")) {
                            return;
                        }

                        if (command.toUpperCase().startsWith("USE ")) {
                            String newPath = command.substring(4).trim();
                            this.USE(newPath);
                        } else if (command.toUpperCase().startsWith("SHOW TABLES;")) {
                            System.out.println("En proceso el show tables");
                            SHOW();
                        } else if (command.toUpperCase().startsWith("CREATE")) {
                            System.out.println("En proceso el Create");
                            CREATE(command);
                        } else if (command.toUpperCase().startsWith("DROP")) {
                            System.out.println("En proceso el Drop");
                            String tableName = command.substring(10, command.length() - 1).trim();
                            DROP(tableName);
                        } else if (command.toUpperCase().startsWith("INSERT INTO")) {
                            System.out.println("En proceso el Insert");
                            INSERT(command);
                        } else if (command.toUpperCase().startsWith("SELECT")) {
                            System.out.println("En proceso el Select");
                            SELECT(command);
                        } else if (command.toUpperCase().startsWith("UPDATE")) {
                            System.out.println("En proceso el UPDATE");
                            UPDATE(command);
                        } else if (command.toUpperCase().startsWith("DELETE")) {
                            System.out.println("En proceso el Delete");
                            DELETE(command);
                        } else {
                            System.out.println("Comando no válido. " +
                                    "\n'exit' para salir, o 'USE $PATH$' para establecer la ruta de trabajo.");
                        }

                        commandBuilder.setLength(0);
                        multiLineInput = false;
                        System.out.print("-- ");
                    } else {
                        multiLineInput = true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fallo con -> " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new SQLCommand();
    }

    public void USE(String path) {
        path = path.trim();
        if (path.endsWith(";")) {
            path = path.substring(0, path.length() - 1).trim();
        }

        File folder = new File(path);

        if (!folder.exists()) {
            System.out.println("Error al entrar, la carpeta de trabajo " + path + " NO EXISTE.");
            return;
        }

        currentDatabase = path;
        System.out.println("Usando la base de datos en: " + currentDatabase);
    }

    public void SHOW() {
        if (!currentDatabase.isEmpty()) {
            File folder = new File(currentDatabase);

            if (!folder.exists() || !folder.isDirectory()) {
                System.out.println("Error: La carpeta de trabajo especificada no existe o no es una carpeta.");
                return;
            }

            File[] files = folder.listFiles();

            if (files != null && files.length > 0) {
                System.out.println("Tablas disponibles en la base de datos '" + currentDatabase + "':");

                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".csv")) {
                        System.out.println(file.getName().replace(".csv", ""));
                    }
                }
            } else {
                System.out.println("No hay tablas en la carpeta de trabajo.");
            }
        } else {
            System.out.println("Error: Ruta de trabajo no especificada.");
        }
    }

    public void CREATE(String query) {
        Pattern pattern = Pattern.compile("CREATE[\\s\\S]*?TABLE\\s+(\\w+)\\s*\\(([^;]+);\\)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            String tableName = matcher.group(1).trim();
            String columnsPart = matcher.group(2).trim();

            String[] columnDefinitions = columnsPart.split(",");
            List<String> columnNames = new ArrayList<>();

            for (String columnDefinition : columnDefinitions) {
                String[] columnInfo = columnDefinition.trim().split("\\s+");

                if (columnInfo.length < 2) {
                    System.out.println("Error: Sintaxis incorrecta para definición de columna.");
                    return;
                }

                String columnName = columnInfo[0];
                if (columnName.contains(" ") || columnName.contains(",")) {
                    System.out.println("Error: Nombre de columna no válido: " + columnName);
                    return;
                }

                columnNames.add(columnName);
            }

            String tableFilePath = currentDatabase + File.separator + tableName + ".csv";

            try {
                File tableFile = new File(tableFilePath);
                if (tableFile.createNewFile()) {
                    try (FileWriter fileWriter = new FileWriter(tableFile)) {
                        String header = join(",", columnNames);
                        fileWriter.write(header);
                    }
                    System.out.println("Tabla '" + tableName + "' creada.");
                } else {
                    System.out.println("La tabla '" + tableName + "' ya existe.");
                }
            } catch (IOException e) {
                System.out.println("Error al crear la tabla: " + e.getMessage());
            }
        } else {
            System.out.println("Error: Sintaxis incorrecta para CREATE TABLE.");
        }
    }

    private String join(String delimiter, List<String> elements) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < elements.size(); i++) {
            result.append(elements.get(i));
            if (i < elements.size() - 1) {
                result.append(delimiter);
            }
        }
        return result.toString();
    }

    public void DROP(String tableName) {
        String tableFilePath = currentDatabase + File.separator + tableName + ".csv";
        File tableFile = new File(tableFilePath);

        if (tableFile.exists()) {
            if (tableFile.delete()) {
                System.out.println("Tabla '" + tableName + "' eliminada.");
            } else {
                System.out.println("Error al eliminar la tabla '" + tableName + "'.");
            }
        } else {
            System.out.println("La tabla '" + tableName + "' no existe.");
        }
    }

    public void INSERT(String query) {
        Pattern pattern = Pattern.compile("INSERT INTO (\\w+) \\((.*?)\\) VALUES \\((.*?)\\);", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            String tableName = matcher.group(1).trim();
            String columnNamesPart = matcher.group(2).trim();
            String valuesPart = matcher.group(3).trim();

            String[] columnNames = columnNamesPart.split(",");
            String[] values = valuesPart.split(",");

            if (columnNames.length != values.length) {
                System.out.println("Error: La cantidad de columnas no coincide con la cantidad de valores.");
                return;
            }

            String tableFilePath = currentDatabase + File.separator + tableName + ".csv";

            try {
                File tableFile = new File(tableFilePath);
                FileWriter fileWriter = new FileWriter(tableFile, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                if (tableFile.exists() && tableFile.length() > 0) {
                    bufferedWriter.newLine();
                }

                StringBuilder dataRow = new StringBuilder();
                for (String value : values) {
                    dataRow.append(value.trim()).append(",");
                }
                // Remove the last comma
                dataRow.setLength(dataRow.length() - 1);

                bufferedWriter.write(dataRow.toString());
                bufferedWriter.close();

                System.out.println("Datos insertados en la tabla '" + tableName + "'.");
            } catch (IOException e) {
                System.out.println("Error al insertar datos en la tabla: " + e.getMessage());
            }
        } else {
            System.out.println("Error: Sintaxis incorrecta para INSERT INTO.");
        }
    }

    public void DELETE(String query) {
        Pattern pattern = Pattern.compile("DELETE FROM (\\w+)(?: WHERE (.+));", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            String tableName = matcher.group(1).trim();
            String whereCondition = matcher.group(2);

            String tableFilePath = currentDatabase + File.separator + tableName + ".csv";

            try {
                File tableFile = new File(tableFilePath);
                File tempFile = new File(currentDatabase + File.separator + tableName + "_temp.csv");

                if (!tableFile.exists()) {
                    System.out.println("La tabla '" + tableName + "' no existe.");
                    return;
                }

                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tableFile));
                     BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile))) {

                    String header = bufferedReader.readLine();
                    if (header == null) {
                        System.out.println("La tabla '" + tableName + "' está vacía.");
                        return;
                    }

                    bufferedWriter.write(header);
                    bufferedWriter.newLine();

                    String[] columnNames = header.split(",");

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] rowData = line.split(",");

                        boolean shouldDelete = false;

                        if (whereCondition != null && !whereCondition.isEmpty()) {
                            String[] conditionParts = whereCondition.split("=");

                            if (conditionParts.length == 2) {
                                String column = conditionParts[0].trim();
                                String value = conditionParts[1].trim().replaceAll("'", "");

                                int columnIndex = -1;
                                for (int i = 0; i < columnNames.length; i++) {
                                    if (columnNames[i].equalsIgnoreCase(column)) {
                                        columnIndex = i;
                                        break;
                                    }
                                }

                                if (columnIndex >= 0 && rowData[columnIndex].equals(value)) {
                                    shouldDelete = true;
                                }
                            } else {
                                System.out.println("Error: Sintaxis incorrecta en la condición WHERE.");
                                return;
                            }
                        }

                        if (!shouldDelete) {
                            bufferedWriter.write(line);
                            bufferedWriter.newLine();
                        }
                    }

                    bufferedReader.close();
                    bufferedWriter.close();

                    if (tableFile.delete()) {
                        tempFile.renameTo(tableFile);
                        System.out.println("Registros eliminados de la tabla '" + tableName + "'.");
                    } else {
                        System.out.println("Error al eliminar registros de la tabla '" + tableName + "'.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error al eliminar registros de la tabla: " + e.getMessage());
            }
        } else {
            System.out.println("Error: Sintaxis incorrecta para DELETE FROM.");
        }
    }

    public void SELECT(String query) {
        Pattern pattern = Pattern.compile("SELECT (.*?) FROM (\\w+)(?: WHERE (.*?))?;", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            String columnsPart = matcher.group(1).trim();
            String tableName = matcher.group(2).trim();
            String whereCondition = matcher.group(3);

            selectedColumns = columnsPart.split(",");
            for (int i = 0; i < selectedColumns.length; i++) {
                selectedColumns[i] = selectedColumns[i].trim();
            }

            String tableFilePath = currentDatabase + File.separator + tableName + ".csv";

            try {
                File tableFile = new File(tableFilePath);

                if (!tableFile.exists()) {
                    System.out.println("La tabla '" + tableName + "' no existe.");
                    return;
                }

                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tableFile))) {
                    String header = bufferedReader.readLine();
                    if (header == null) {
                        System.out.println("La tabla '" + tableName + "' está vacía.");
                        return;
                    }

                    String[] columnNames = header.split(",");

                    Map<String, Integer> columnIndexMap = new HashMap<>();
                    for (int i = 0; i < columnNames.length; i++) {
                        columnIndexMap.put(columnNames[i].trim(), i);
                    }

                    List<Integer> selectedColumnIndices = new ArrayList<>();
                    if (columnsPart.equals("*")) {
                        for (int i = 0; i < columnNames.length; i++) {
                            selectedColumnIndices.add(i);
                        }
                    } else {
                        for (String column : selectedColumns) {
                            Integer index = columnIndexMap.get(column);
                            if (index == null) {
                                System.out.println("Error: La columna '" + column + "' no existe.");
                                return;
                            }
                            selectedColumnIndices.add(index);
                        }
                    }

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] rowData = line.split(",");

                        boolean matchesCondition = true;
                        if (whereCondition != null && !whereCondition.isEmpty()) {
                            String[] conditionParts = whereCondition.split("=");
                            if (conditionParts.length == 2) {
                                String column = conditionParts[0].trim();
                                String value = conditionParts[1].trim().replaceAll("'", "");

                                Integer index = columnIndexMap.get(column);
                                if (index == null || !rowData[index].equals(value)) {
                                    matchesCondition = false;
                                }
                            } else {
                                System.out.println("Error: Sintaxis incorrecta en la condición WHERE.");
                                return;
                            }
                        }

                        if (matchesCondition) {
                            List<String> selectedData = new ArrayList<>();
                            for (int index : selectedColumnIndices) {
                                selectedData.add(rowData[index]);
                            }
                            System.out.println(String.join(", ", selectedData));
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error al seleccionar datos de la tabla: " + e.getMessage());
            }
        } else {
            System.out.println("Error: Sintaxis incorrecta para SELECT.");
        }
    }

    public void UPDATE(String query) {
        Pattern pattern = Pattern.compile("UPDATE (\\w+) SET (.+?)(?: WHERE (.*?))?;", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            String tableName = matcher.group(1).trim();
            String setPart = matcher.group(2).trim();
            String whereCondition = matcher.group(3);

            String tableFilePath = currentDatabase + File.separator + tableName + ".csv";

            try {
                File tableFile = new File(tableFilePath);
                File tempFile = new File(currentDatabase + File.separator + tableName + "_temp.csv");

                if (!tableFile.exists()) {
                    System.out.println("La tabla '" + tableName + "' no existe.");
                    return;
                }

                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(tableFile));
                     BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile))) {

                    String header = bufferedReader.readLine();
                    if (header == null) {
                        System.out.println("La tabla '" + tableName + "' está vacía.");
                        return;
                    }

                    bufferedWriter.write(header);
                    bufferedWriter.newLine();

                    String[] columnNames = header.split(",");

                    Map<String, Integer> columnIndexMap = new HashMap<>();
                    for (int i = 0; i < columnNames.length; i++) {
                        columnIndexMap.put(columnNames[i].trim(), i);
                    }

                    String[] setAssignments = setPart.split(",");
                    Map<Integer, String> updatedValues = new HashMap<>();
                    for (String assignment : setAssignments) {
                        String[] parts = assignment.split("=");
                        if (parts.length == 2) {
                            String column = parts[0].trim();
                            String value = parts[1].trim().replaceAll("'", "");

                            Integer index = columnIndexMap.get(column);
                            if (index != null) {
                                updatedValues.put(index, value);
                            }
                        } else {
                            System.out.println("Error: Sintaxis incorrecta en la parte SET.");
                            return;
                        }
                    }

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] rowData = line.split(",");

                        boolean matchesCondition = true;
                        if (whereCondition != null && !whereCondition.isEmpty()) {
                            String[] conditionParts = whereCondition.split("=");
                            if (conditionParts.length == 2) {
                                String column = conditionParts[0].trim();
                                String value = conditionParts[1].trim().replaceAll("'", "");

                                Integer index = columnIndexMap.get(column);
                                if (index == null || !rowData[index].equals(value)) {
                                    matchesCondition = false;
                                }
                            } else {
                                System.out.println("Error: Sintaxis incorrecta en la condición WHERE.");
                                return;
                            }
                        }

                        if (matchesCondition) {
                            for (Map.Entry<Integer, String> entry : updatedValues.entrySet()) {
                                rowData[entry.getKey()] = entry.getValue();
                            }
                        }

                        bufferedWriter.write(String.join(",", rowData));
                        bufferedWriter.newLine();
                    }

                    bufferedReader.close();
                    bufferedWriter.close();

                    if (tableFile.delete()) {
                        tempFile.renameTo(tableFile);
                        System.out.println("Registros actualizados en la tabla '" + tableName + "'.");
                    } else {
                        System.out.println("Error al actualizar registros en la tabla '" + tableName + "'.");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error al actualizar registros de la tabla: " + e.getMessage());
            }
        } else {
            System.out.println("Error: Sintaxis incorrecta para UPDATE.");
        }
    }
}
