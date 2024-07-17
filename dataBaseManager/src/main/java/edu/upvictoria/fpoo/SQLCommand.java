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
                            SHOW();
                        } else if (command.toUpperCase().startsWith("CREATE")) {
                            CREATE(command);
                        } else if (command.toUpperCase().startsWith("DROP")) {
                            String tableName = command.substring(10, command.length() - 1).trim();
                            DROP(tableName);
                        } else if (command.toUpperCase().startsWith("INSERT INTO")) {
                            INSERT(command);
                        } else if (command.toUpperCase().startsWith("SELECT")) {
                            SELECT(command);
                        } else if (command.toUpperCase().startsWith("UPDATE")) {
                            System.out.println("En proceso...");
                        } else if (command.toUpperCase().startsWith("DELETE")) {
                            System.out.println("En proceso...");
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
            boolean created = false;
            if (!created) {
                System.out.println("Error al entrar, la carpeta de trabajo " + path + " NO EXISTE.");
                return;
            }
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
            return;
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

    public void deleteData(String query) {
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

                    String headerLine = bufferedReader.readLine();
                    bufferedWriter.write(headerLine);
                    bufferedWriter.newLine();

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] columns = line.split(",");

                        if (!evaluateWhereCondition(headerLine, columns, whereCondition)) {
                            bufferedWriter.write(line);
                            bufferedWriter.newLine();
                        }
                    }
                }

                if (tempFile.renameTo(tableFile)) {
                    System.out.println("Datos eliminados de la tabla '" + tableName + "'.");
                } else {
                    System.out.println("Error al eliminar datos de la tabla '" + tableName + "'.");
                }
            } catch (IOException e) {
                System.out.println("Error al eliminar datos de la tabla: " + e.getMessage());
            }
        } else {
            System.out.println("Error: Sintaxis incorrecta para DELETE FROM.");
        }
    }

    public boolean evaluateWhereCondition(String headerLine, String[] columns, String condition) {
        String[] columnNames = headerLine.split(",");
        String[] conditionComponents = condition.trim().split("\\s+");

        if (conditionComponents.length != 3) {
            System.out.println("Error: Sintaxis incorrecta para la condición WHERE.");
            return false;
        }

        String leftOperand = conditionComponents[0];
        String operator = conditionComponents[1];
        String rightOperand = conditionComponents[2];

        int columnIndex = -1;
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].trim().equalsIgnoreCase(leftOperand)) {
                columnIndex = i;
                break;
            }
        }

        if (columnIndex == -1) {
            System.out.println("Error: La columna especificada en la condición WHERE no existe.");
            return false;
        }

        String columnValue = columns[columnIndex].trim();

        if (operator.equals("=")) {
            return columnValue.equals(rightOperand);
        } else if (operator.equals("<>")) {
            return !columnValue.equals(rightOperand);
        } else {
            System.out.println("Error: Operador de comparación no válido en la condición WHERE.");
            return false;
        }
    }

    public void SELECT(String query) {
        Pattern patternWithWhere = Pattern.compile("SELECT (.+) FROM (\\w+) WHERE (.+);", Pattern.CASE_INSENSITIVE);
        Matcher matcherWithWhere = patternWithWhere.matcher(query);

        Pattern patternWithoutWhere = Pattern.compile("SELECT (.+) FROM (\\w+);", Pattern.CASE_INSENSITIVE);
        Matcher matcherWithoutWhere = patternWithoutWhere.matcher(query);

        if (matcherWithWhere.find()) {
            try {
                String columnsPart = matcherWithWhere.group(1).trim();
                String tableName = matcherWithWhere.group(2).trim();
                String whereCondition = matcherWithWhere.group(3);

                if (whereCondition != null && !whereCondition.isEmpty()) {
                    String tableFilePath = currentDatabase + File.separator + tableName + ".csv";

                    try {
                        File tableFile = new File(tableFilePath);

                        if (!tableFile.exists()) {
                            System.out.println("La tabla '" + tableName + "' no existe.");
                            return;
                        }

                        BufferedReader bufferedReader = new BufferedReader(new FileReader(tableFile));

                        String headerLine = bufferedReader.readLine();
                        String[] columnNames = headerLine.split(",");

                        if (columnsPart.equals("*")) {
                            System.out.println(headerLine);

                            while ((line = bufferedReader.readLine()) != null) {
                                try {
                                    rowData = line.split(",");
                                    if (evaluateWhereCondition(columnNames, rowData, whereCondition)) {
                                        System.out.println(line);
                                    }
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    continue;
                                }
                            }
                        } else {
                            String[] selectedColumns = columnsPart.split(",");
                            for (String column : selectedColumns) {
                                System.out.print(column + "\t");
                            }
                            System.out.println();

                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                try {
                                    rowData = line.split(",");
                                    if (evaluateWhereCondition(columnNames, rowData, whereCondition)) {
                                        for (String columnName : selectedColumns) {
                                            int columnIndex = Arrays.asList(columnNames).indexOf(columnName.trim());
                                            if (columnIndex >= 0) {
                                                System.out.print(rowData[columnIndex] + "\t");
                                            } else {
                                                System.out.print("\t");
                                            }
                                        }
                                        System.out.println();
                                    }
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    continue;
                                }
                            }
                        }

                        bufferedReader.close();
                    } catch (IOException e) {
                        System.err.println("Error al seleccionar datos de la tabla: " + e.getMessage());
                    }
                } else {
                    System.out.println("Error: Falta la cláusula WHERE en la consulta SELECT.");
                    return;
                }

            } catch (Exception e) {
                System.err.println("Error al ejecutar SELECT con WHERE: " + e.getMessage());
            }
        } else if (matcherWithoutWhere.find()) {
            try {
                String columnsPart = matcherWithoutWhere.group(1).trim();
                String tableName = matcherWithoutWhere.group(2).trim();
                handleSelectAll(columnsPart, tableName);
            } catch (Exception e) {
                System.err.println("Error al ejecutar SELECT sin WHERE: " + e.getMessage());
            }
        } else {
            System.out.println("Error: Sintaxis incorrecta para SELECT.");
        }
    }

    public boolean evaluateWhereCondition(String[] columnNames, String[] rowData, String condition) {
        String[] conditionComponents = condition.trim().split("\\s+");

        if (conditionComponents.length != 3) {
            System.out.println("Error: Sintaxis incorrecta para la condición WHERE.");
            return false;
        }

        String leftOperand = conditionComponents[0];
        String operator = conditionComponents[1];
        String rightOperand = conditionComponents[2];

        int columnIndex = -1;
        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i].trim().equalsIgnoreCase(leftOperand)) {
                columnIndex = i;
                break;
            }
        }

        if (columnIndex == -1) {
            System.out.println("Error: La columna especificada en la condición WHERE no existe.");
            return false;
        }

        String columnValue = rowData[columnIndex].trim();

        if (operator.equals("=")) {
            return columnValue.equals(rightOperand);
        } else if (operator.equals("<>")) {
            return !columnValue.equals(rightOperand);
        } else {
            System.out.println("Error: Operador de comparación no válido en la condición WHERE.");
            return false;
        }
    }

    public void updateData(String query) {
        Pattern pattern = Pattern.compile("UPDATE (\\w+) SET (\\w+ = .+) WHERE (.+);", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);

        if (matcher.find()) {
            String tableName = matcher.group(1).trim();
            String setClause = matcher.group(2).trim();
            String whereCondition = matcher.group(3).trim();

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

                    String headerLine = bufferedReader.readLine();
                    bufferedWriter.write(headerLine);
                    bufferedWriter.newLine();

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] columns = line.split(",");

                        if (evaluateWhereCondition(headerLine, columns, whereCondition)) {
                            applyUpdate(setClause, columns);
                            bufferedWriter.write(String.join(",", columns));
                        } else {
                            bufferedWriter.write(String.join(",", columns));
                        }

                        bufferedWriter.newLine();
                    }
                }

                if (tempFile.renameTo(tableFile)) {
                    System.out.println("Datos actualizados en la tabla '" + tableName + "'.");
                } else {
                    System.out.println("Error al actualizar datos en la tabla '" + tableName + "'.");
                }
            } catch (IOException e) {
                System.out.println("Error al actualizar datos en la tabla: " + e.getMessage());
            }
        } else {
            System.out.println("Error: Sintaxis incorrecta para UPDATE.");
        }
    }

    public void applyUpdate(String setClause, String[] columns) {
        String[] updateComponents = setClause.split(",");

        for (String updateComponent : updateComponents) {
            String[] parts = updateComponent.trim().split("=");

            if (parts.length != 2) {
                System.out.println("Error: Sintaxis incorrecta en la cláusula SET.");
                return;
            }

            String columnName = parts[0].trim();
            String newValue = parts[1].trim();

            int columnIndex = getColumnIndex(columns, columnName);

            if (columnIndex != -1) {
                columns[columnIndex] = newValue;
            } else {
                System.out.println("Error: La columna especificada en la cláusula SET no existe.");
            }
        }
    }

    public int getColumnIndex(String[] columns, String columnName) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].trim().equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }

    public void handleSelectAll(String columnsPart, String tableName) {
        String tableFilePath = currentDatabase + File.separator + tableName + ".csv";

        try {
            File tableFile = new File(tableFilePath);
            if (!tableFile.exists()) {
                System.out.println("La tabla '" + tableName + "' no existe.");
                return;
            }

            BufferedReader bufferedReader = new BufferedReader(new FileReader(tableFile));
            String headerLine = bufferedReader.readLine();
            String[] columnNames = headerLine.split(",");
            System.out.println("\n");

            String[] selectedColumns = columnsPart.split(",");

            for (String selectedColumn : selectedColumns) {
                String columnName = selectedColumn.trim();
                if (!Arrays.asList(columnNames).contains(columnName)) {
                    System.out.println("Error: La columna '" + columnName + "' no existe en la tabla.");
                    return;
                }
            }

            for (String selectedColumn : selectedColumns) {
                System.out.print(selectedColumn.trim() + "\t");
            }
            System.out.println();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                try {
                    String[] rowData = line.split(",");
                    for (String selectedColumn : selectedColumns) {
                        String columnName = selectedColumn.trim();
                        int columnIndex = getColumnIndex(columnNames, columnName);
                        if (columnIndex >= 0) {
                            System.out.print(rowData[columnIndex] + "\t");
                        } else {
                            System.out.print("\t");
                        }
                    }
                    System.out.println();
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }

            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Error al seleccionar datos de la tabla: " + e.getMessage());
        }
    }
}
