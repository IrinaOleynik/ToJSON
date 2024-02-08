package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        List<Employee> listFromCSV = parseCSV(columnMapping, fileName);
        String json = listToJson(listFromCSV);
        writeString(json, "data.json");
        List<Employee> listFromXML = parseXML("data.xml");
        String json2 = listToJson(listFromXML);
        writeString(json2, "data2.json");
    }

    private static List<Employee> parseXML(String filename) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(filename));
        List<Employee> employeeList = new ArrayList<>();
        Node root = doc.getDocumentElement();
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            long id = 0L;
            String firstName = null, lastName = null, country = null;
            int age = 0;
            if (Node.ELEMENT_NODE == nodeList.item(i).getNodeType()) {
                NodeList employeeFields = nodeList.item(i).getChildNodes();
                for (int j = 0; j < employeeFields.getLength(); j++) {
                    if (Node.ELEMENT_NODE == employeeFields.item(j).getNodeType()) {
                        String attrName = employeeFields.item(j).getNodeName();
                        String attrValue = employeeFields.item(j).getTextContent();
                        if (attrName.equals("id")) {
                            id = Long.parseLong(attrValue);
                        } else if (attrName.equals("firstName")) {
                            firstName = attrValue;
                        } else if (attrName.equals("lastName")) {
                            lastName = attrValue;
                        } else if (attrName.equals("country")) {
                            country = attrValue;
                        } else if (attrName.equals("age")) {
                            age = Integer.parseInt(attrValue);
                        }
                    }
                }
                employeeList.add(new Employee(id, firstName, lastName, country, age));
            }
        }
        return employeeList;
    }

    private static void writeString(String json, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(json.toString());
            writer.flush();

        } catch (IOException ex) {
            ex.getMessage();
        }
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader reader = new CSVReader(new FileReader("data.csv"));) {
            ColumnPositionMappingStrategy<Employee> strategy =
                    new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping("id", "firstName", "lastName", "country", "age");
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            return csv.parse();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}