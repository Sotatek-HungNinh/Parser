package com.cardano.parser.utils;

import com.cardano.parser.enumable.FileType;
import com.cardano.parser.enumable.ImportLib;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.util.*;

import static com.cardano.parser.enumable.ImportLib.*;


public class GenerateUtils {

    /**
     * Generate pojo class
     *
     * @param outputFolder String: path to generate file
     * @param packageName  String: package name
     * @param className    String: the name of pojo class
     * @param commentOfClass    String: comment of class
     * @param mapFields    map<String,List<String>>:  key is field name, value is List contains 3 element- firstElement is dataType of field, secondElement is a comment, thirdElement is boolean field check nullable
     * @return boolean - Status
     */
    public static boolean generatePojoClass(String outputFolder, String packageName, String className, String commentOfClass, FileType fileType, Map<String, List<String>> mapFields) {
        className = correctClassName(className);
        String path = FileUtils.createFile(getOutputFolder(outputFolder, packageName), className + ".java");
        String content = prepareContentOfPojo(outputFolder, packageName, className, commentOfClass, fileType, mapFields);

        boolean writeStatus = FileUtils.writeFile(path, content);
        System.out.printf("Generate Pojo class %s %s to path %s \n", className, writeStatus ? "successful" : "failed", path);
        return writeStatus;
    }

    private static String prepareContentOfPojo(String outputFolder, String packageName, String className, String commentOfClass, FileType fileType, Map<String, List<String>> mapFields) {
        StringBuilder content = new StringBuilder();
        content.append("package ").append(packageName).append(";").append("\n");

        Set<String> importLibs = getImportLibs(mapFields);
        importLibs.forEach(importLib -> content.append(importLib).append("\n"));

        if (hasLength(commentOfClass)) {
            content.append("/**").append("\n").append(commentOfClass).append("*/\n");
        }
        content.append("public ");
        if (fileType.equals(FileType.ABSTRACT)) {
            content.append("abstract class ");
        } else {
            content.append("class ");
        }
        content.append(capitalize(className));

        // generate extends abstract class
        if (mapFields.containsKey(Constants.ABSTRACT_CLASS)) {
            appendExtendsAbstractClass(content, mapFields.get(Constants.ABSTRACT_CLASS));
            generateEmbeddedClass(outputFolder, packageName, FileType.ABSTRACT, mapFields.get(Constants.ABSTRACT_CLASS));
        }
        content.append(" {\n");

        content.append(GenerateUtils.generateFields(mapFields));
        content.append(GenerateUtils.generateGetterAndSetter(mapFields)).append("}");

        if (mapFields.containsKey(Constants.EMBEDDED_OBJECT)) {
            generateEmbeddedClass(outputFolder, packageName, FileType.CLASS, mapFields.get(Constants.EMBEDDED_OBJECT));
        }
        return content.toString();
    }

    private static void appendExtendsAbstractClass(StringBuilder pojoBuilder, List<String> listMapAbstractString) {
        String abstractClassName = getAbstractClassName(listMapAbstractString);
        if (abstractClassName != null) {
            pojoBuilder.append(" extends ").append(abstractClassName);
        }
    }

    private static String getAbstractClassName(List<String> listMapAbstractString) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (listMapAbstractString.size() > 0) {
            try {
                Map<String, List<String>> mapFieldAbstract = objectMapper.readValue(listMapAbstractString.get(0), Map.class);
                List<String> listClassName = mapFieldAbstract.get(Constants.CLASS_NAME);
                return listClassName.get(0);
            } catch (Exception e) {
                System.out.println("getAbstractClassName error " + e);
            }

        }

        return null;
    }

    private static void generateEmbeddedClass(String outputFolder, String packageName, FileType fileType, List<String> listEmbeddedObject) {
        ObjectMapper objectMapper = new ObjectMapper();
        for (int i = 0; i < listEmbeddedObject.size(); i++) {
            String mapObjectString = listEmbeddedObject.get(i);
            try {
                Map<String, List<String>> mapEmbeddedObject = objectMapper.readValue(mapObjectString, Map.class);
                if (mapEmbeddedObject.get(Constants.CLASS_NAME).size() == 0) {
                    throw new Exception(Constants.CLASS_NAME + "must is be null");
                }
                String className = mapEmbeddedObject.get(Constants.CLASS_NAME).get(0);

                generatePojoClass(outputFolder, packageName, className, null, fileType, mapEmbeddedObject);
            } catch (Exception e) {
                System.out.println("generateEmbeddedClass error " + e);
            }
        }
    }

    private static Set<String> getImportLibs(Map<String, List<String>> mapFields) {
        Set<String> importLibs = new HashSet<>();
        importLibs.add(JSON_PROPERTY.importLib);

        for (String field : mapFields.keySet()) {
            // import libraries
            List<String> metadata = mapFields.get(field);
            if (metadata.size() > 0) {
                String dataType = metadata.get(0);
                String importLib = getImportLibFromDataType(dataType);
                importLibs.add(importLib);
            }
        }
        return importLibs;
    }

    private static String getImportLibFromDataType(String dataType) {
        if (dataType.contains("<")) {
            dataType = dataType.split("<")[0];
        }
        try {
            ImportLib importLib = ImportLib.getImportLib(dataType);
            switch (importLib) {
                case LIST:
                    return LIST.importLib;
                case SET:
                    return SET.importLib;
                case MAP:
                    return MAP.importLib;
                case DATE:
                    return DATE.importLib;
                default:
                    return "";
            }
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * Generate getter and setter function
     *
     * @param mapFields map<String,List<String>>: key is field name, value list string that first element is dataType of field
     * @return String - getter and setter functions
     */
    private static String generateGetterAndSetter(Map<String, List<String>> mapFields) {
        StringBuilder result = new StringBuilder();
        Set<String> fields = mapFields.keySet();
        fields.forEach(field -> {
            if (!field.equals(Constants.EMBEDDED_OBJECT) && !field.equals(Constants.CLASS_NAME) && !field.equals(Constants.ABSTRACT_CLASS)) {
                List<String> metaData = mapFields.get(field);
                String dataType = correctDataType(metaData.get(0));
                try {
                    result.append(generateGetter(field, dataType)).append("\n");
                    result.append(generateSetter(field, dataType)).append("\n");
                } catch (IOException e) {
                    System.out.println("generateGetterAndSetter error " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        return result.toString();
    }

    private static String correctDataType(String dataType) {
        if (DataTypeConverter.isJavaDataType(dataType)) {
            return dataType;
        }
        return correctClassName(dataType);
    }
    /**
     * Generate fields
     *
     * @param mapFields map<String,List<String>>: key is field name, value list string that first element is dataType of field
     * @return String - fields definition
     */
    private static String generateFields(Map<String, List<String>> mapFields) {
        StringBuilder result = new StringBuilder();
        Set<String> fields = mapFields.keySet();

        fields.forEach(field -> {
            if (!field.equals(Constants.EMBEDDED_OBJECT) && !field.equals(Constants.CLASS_NAME) && !field.equals(Constants.ABSTRACT_CLASS)) {
                List<String> metaData = mapFields.get(field);
                try {
                    String dataType = correctDataType(metaData.get(0));
                    String comment = metaData.get(1);
                    if (hasLength(comment)) {
                        Map<String, Object> commentParams = new HashMap<>(){{
                            put("comment", comment);
                        }};
                        result.append(fillDataToTemplate(commentParams, Templates.COMMENT_TEMPLATE));
                    }

                    Map<String, Object> propertyParams = new HashMap<>() {{
                        put("json_property", field);
                        put("required", !Boolean.parseBoolean(metaData.get(2)));
                        put("data_type", dataType);
                        put("field_name", GenerateUtils.correctFieldName(field));
                    }};
                    result.append(fillDataToTemplate(propertyParams, Templates.PROPERTY_TEMPLATE)).append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return result.toString();
    }

    private static String generateGetter(String fieldName, String dataType) throws IOException {
        Map<String, Object> params = new HashMap<>(){{
            put("return_type", dataType);
            put("getter_name", genGetterFieldName(fieldName));
            put("field", correctFieldName(fieldName));
        }};

        return fillDataToTemplate(params, Templates.GETTER_TEMPLATE);
    }

    private static String generateSetter(String fieldName, String dataType) throws IOException {
        Map<String, Object> params = new HashMap<>(){{
            put("name", genSetterFieldName(fieldName));
            put("data_type", dataType);
            put("field_name", correctFieldName(fieldName));
        }};

        return fillDataToTemplate(params, Templates.SETTER_TEMPLATE);
    }

    private static String genGetterFieldName(String fieldName) {
        fieldName = capitalize(correctFieldName(fieldName));
        return "get" + fieldName;
    }

    private static String genSetterFieldName(String fieldName) {
        fieldName = capitalize(correctFieldName(fieldName));
        return "set" + fieldName;
    }

    private static String getOutputFolder(String parent, String packageName) {
        String[] packages = packageName.split("\\.");
        StringBuilder result = new StringBuilder();
        result.append(parent);
        for (String ele : packages) {
            result.append("/").append(ele);
        }
        return result.toString();
    }

    public static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    public static String capitalize(String str) {
        return changeFirstCharacterCase(str, true);
    }

    private static String changeFirstCharacterCase(String str, boolean capitalize) {
        if (!hasLength(str)) {
            return str;
        } else {
            char baseChar = str.charAt(0);
            char updatedChar;
            if (capitalize) {
                updatedChar = Character.toUpperCase(baseChar);
            } else {
                updatedChar = Character.toLowerCase(baseChar);
            }

            if (baseChar == updatedChar) {
                return str;
            } else {
                char[] chars = str.toCharArray();
                chars[0] = updatedChar;
                return new String(chars);
            }
        }
    }

    public static boolean hasLength(String str) {
        return str != null && !str.isEmpty();
    }

    public static String correctFieldName(String input) {
        StringBuilder result = new StringBuilder();
        if (input.contains("-")) {
            String[] elements = input.split("-");
            for (int i = 0; i < elements.length; i++) {
                String ele = elements[i];
                if (i == 0) {
                    result.append(uncapitalize(ele.toLowerCase()));
                } else {
                    result.append(capitalize(ele.toLowerCase()));
                }
            }
            return result.toString();
        } else if (input.contains("_")) {
            String[] elements = input.split("_");
            for (int i = 0; i < elements.length; i++) {
                String ele = elements[i];
                if (i == 0) {
                    result.append(uncapitalize(ele.toLowerCase()));
                } else {
                    result.append(capitalize(ele.toLowerCase()));
                }
            }
            return result.toString();
        } else {
            return uncapitalize(input);
        }
    }

    public static String correctClassName(String input) {
        return capitalize(correctFieldName(input));
    }

    public static String genListProperty(String dataType) {
        return "List<" + correctClassName(dataType) + ">";
    }
    public static String fillDataToTemplate(Map<String, Object> params, String template) throws IOException {
        StringSubstitutor sub = new StringSubstitutor(params);
        return sub.replace(template);
    }

}
