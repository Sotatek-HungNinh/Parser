package com.cardano.parser.services;

import com.cardano.parser.enumable.CddlDataTypes;
import com.cardano.parser.enumable.FileType;
import com.cardano.parser.enumable.PropertyReferenceType;
import com.cardano.parser.type.*;
import com.cardano.parser.utils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.util.*;

import static com.cardano.parser.utils.Constants.CLASS_NAME;
import static com.cardano.parser.utils.Constants.GENERATE_PACKAGE_NAME;

public class GenerateService {

    private ObjectMapper objectMapper;

    public GenerateService() {
         objectMapper = new ObjectMapper();
    }

    public List<Assignment> convertCddlToAbstractSyntaxTree(String cddlFilePath) throws Exception {
        String cddlContent = FileUtils.readFile(cddlFilePath);
        Lexer lexer = new Lexer(cddlContent);
        Parser parser = new Parser(lexer);
        List<Assignment> assignments = parser.parse();

        Gson gson = new Gson();
        String json = gson.toJson(assignments);
        System.out.println(json);

        return assignments;
    }

    public void generatePojoClass(String outputFolder, String cddlFilePath) throws Exception {
        System.out.printf("gen pojo to folder %s\n", outputFolder);
        List<Assignment> assignments = convertCddlToAbstractSyntaxTree(cddlFilePath);
        Map<String, String> mapClassComment = mapCommentToClass(assignments);

        for (Assignment assignment : assignments) {
            if (assignment instanceof Comment) {
                continue;
            }
            if (assignment instanceof Group || assignment instanceof Array || assignment instanceof Variable) {
                Map<String, List<String>> mapFields = convertAbstractSyntaxTreeToMapField(assignment);
                String className = mapFields.get(CLASS_NAME).get(0);
                String classComment = mapClassComment.get(className);
                GenerateUtils.generatePojoClass(outputFolder, GENERATE_PACKAGE_NAME, className, classComment, FileType.CLASS, mapFields);
            } else {
                System.out.println("not implement to support convert this type " + assignment);
            }
        }
    }

    private Map<String, String> mapCommentToClass(List<Assignment> assignments) {
        // get all continuous comment and assign to each class
        Map<String, String> mapCommentClass = new HashMap<>(); // key - className, value - content of comment
        StringBuilder commentOfClass = new StringBuilder();
        String className = "";
        for (int i = 0; i < assignments.size(); i++) {
            Assignment assignment = assignments.get(i);
            if (assignment instanceof Group) {
                className = ((Group) assignment).getName();
            } else if (assignment instanceof Array) {
                className = ((Array) assignment).getName();
            }
            if (i < assignments.size() - 1) {
                Assignment nextAssignment = assignments.get(i + 1);
                if (nextAssignment instanceof Comment) {
                    Comment comment = (Comment) nextAssignment;
                    commentOfClass.append("*\t").append(comment.getContent()).append("\n");
                } else {
                    mapCommentClass.put(className, commentOfClass.toString());
                    commentOfClass.setLength(0);
                }
            } else {
                mapCommentClass.put(className, commentOfClass.toString());
                commentOfClass.setLength(0);
            }
        }

        return mapCommentClass;
    }

    private Map<String, List<String>> convertAbstractSyntaxTreeToMapField(Assignment assignment) throws Exception {
        Map<String, List<String>> mapFields = new HashMap<>();

        if (assignment instanceof Group) {
            mapFields = convertGroupType(assignment, null, false);
        } else if (assignment instanceof Array) {
            Array array = (Array) assignment;
            Group group = Group.builder()
                    .name(array.getName())
                    .properties(array.getValues())
                    .build();
            mapFields = convertGroupType(group, null, true);

        } else if (assignment instanceof Variable) {
            Variable variable = (Variable) assignment;
            mapFields = convertTypeVariable(variable);
        }
        else {
            System.out.println("Unexpected format!" + assignment.getClass());
        }
        return mapFields;
    }

    private Map<String, List<String>> convertTypeVariable(Variable variable) throws Exception {
        List<PropertyType> propertyTypes = (List<PropertyType>) variable.getPropertyTypes();
        Property property = Property.builder()
                .name(variable.getName())
                .propertyType(propertyTypes)
                .occurrence(new Occurrence())
                .comment("")
                .build();

        Group group = Group.builder()
                .name(variable.getName())
                .isChoiceAddition(variable.isChoiceAddition())
                .properties(List.of(List.of(property)))
                .build();
        return convertGroupType(group, null, false);
    }

    /**
     * convert Group type in CDDL to map
     * @param assignment Assigment
     * @param parentPropertyName  String- if group's name is empty, get parent group name to set to this group name. if not have parent pass null value
     * @param isObjectArray boolean - check if object js array. exp: reputon-array = [* reputon]  => isObjectArray = true
     * */
    private Map<String, List<String>> convertGroupType(Assignment assignment, String parentPropertyName, boolean isObjectArray) throws Exception {
        Map<String, List<String>> mapFields = new HashMap<>();
        Group group = (Group) assignment;
        String javaClassName = group.getName();

        List<List<Property>> properties = group.getProperties();
        for (List<Property> property : properties) {
            if (property.size() == 1) {
                Property pro = property.get(0);
                boolean fieldNullable = pro.getOccurrence().getN() == 0;
                String javaType = "";
                if (pro.getPropertyType().getClass().getSimpleName().equals("String")) {
                    String propertyType = String.valueOf(pro.getPropertyType());
                    javaType = getJavaDataType(propertyType, fieldNullable);

                    if (Utils.isEmpty(pro.getName())) {
                        pro.setName(GenerateUtils.uncapitalize(propertyType));
                    }
                    javaType = isObjectArray ? GenerateUtils.genListProperty(javaType) : javaType;
                    mapFields.put(pro.getName(), List.of(javaType, pro.getComment(), String.valueOf(fieldNullable)));
                } else {
                    PropertyType propertyType = ((List<PropertyType>) pro.getPropertyType()).get(0);
                    if (propertyType instanceof StringType) {
                        StringType propertyTypeSt = (StringType) propertyType;
                        String fieldType = propertyTypeSt.getValue();
                        javaType = DataTypeConverter.getJavaDataType(CddlDataTypes.getType(fieldType), fieldNullable);

                        javaType = isObjectArray ? GenerateUtils.genListProperty(javaType) : javaType;
                        mapFields.put(pro.getName(), List.of(javaType, pro.getComment(), String.valueOf(fieldNullable)));

                    } else if (propertyType instanceof Group) {
                        Group embeddedGroup = (Group) propertyType;
                        Map<String, List<String>> embeddedObject = convertGroupType(embeddedGroup, pro.getName(), false);
                        mapFields.put(Constants.EMBEDDED_OBJECT, Collections.singletonList(objectMapper.writeValueAsString(embeddedObject)));

                        String dataType = isObjectArray ? GenerateUtils.genListProperty(pro.getName()) : pro.getName();
                        mapFields.put(pro.getName(), List.of(dataType, pro.getComment(), String.valueOf(fieldNullable)));
                    } else if (propertyType instanceof Array) {
                        convertArrayType(mapFields, (Array) propertyType,pro.getName(), pro.getComment());
                        break;
                    } else if (propertyType instanceof PropertyReference) {
                        PropertyReference propertyReference = (PropertyReference) propertyType;
                        if (propertyReference.getType().equals(PropertyReferenceType.GROUP)) {
                            String propertyReferenceValue;
                            if (propertyReference.getValue() instanceof StringType) {
                                propertyReferenceValue = ((StringType) propertyReference.getValue()).getValue();
                            } else {
                                propertyReferenceValue = propertyReference.getValue().toString();
                            }
                            javaType = getJavaDataType(propertyReferenceValue, fieldNullable);
                        }
                    }
                    if (Utils.isEmpty(pro.getName())) {
                        pro.setName(GenerateUtils.uncapitalize(javaType));
                    }
                    javaType = isObjectArray ? GenerateUtils.genListProperty(javaType) : javaType;
                    mapFields.put(pro.getName(), List.of(javaType, pro.getComment(), String.valueOf(fieldNullable)));
                }
            }
        }

        if (!GenerateUtils.hasLength(javaClassName)) {
            javaClassName = parentPropertyName;
        }
        mapFields.put(Constants.CLASS_NAME, Collections.singletonList(javaClassName));
        return mapFields;
    }

    private String getJavaDataType(String propertyReferenceValue, boolean fieldNullable) {
        CddlDataTypes cddlDataType = CddlDataTypes.getType(propertyReferenceValue);
        if (cddlDataType != null) {
            return DataTypeConverter.getJavaDataType(cddlDataType, fieldNullable);
        } else {
            // Type Embedded Object
            return  propertyReferenceValue;
        }
    }

    private void convertArrayType(Map<String, List<String>> mapFields, Array array, String arrayName, String comment) throws Exception {
        String dataType = "";
        if (array.getValues().size() == 1) {
            List<Property> properties = array.getValues().get(0);
            Property property = properties.get(0);
            boolean fieldNullable = property.getOccurrence().getN() == 0;

            if (property.getPropertyType() instanceof List) {
                PropertyType propertyType = ((List<PropertyType>) property.getPropertyType()).get(0);
                if (propertyType instanceof PropertyReference) {
                    PropertyReference propertyReferences = (PropertyReference) propertyType;
                    String fieldType = propertyReferences.getValue().toString();
                    dataType = GenerateUtils.genListProperty(fieldType);
                }
            } else {
                String propertyType = (String) property.getPropertyType();
                dataType = DataTypeConverter.getJavaDataType(CddlDataTypes.getType(propertyType), fieldNullable);
                dataType = GenerateUtils.genListProperty(dataType);

            }
            mapFields.put(arrayName, Arrays.asList(dataType, comment, String.valueOf(fieldNullable)));
        } else {
            throw new Exception("Unexpected array property format");
        }
    }
}
