package com.cardano.parser.utils;

public class Templates {
    public static final String PROPERTY_TEMPLATE =
            "    @JsonProperty(value = \"${json_property}\", required = ${required})\n" +
            "    private ${data_type} ${field_name};\n";

    public static final String COMMENT_TEMPLATE =
            "   /**\n" +
            "    * ${comment}\n" +
            "    */\n";

    public static final String GETTER_TEMPLATE =
            "    public ${return_type} ${getter_name}() {\n" +
            "\t\treturn this.${field};\n" +
            "\t}\n";

    public static final String SETTER_TEMPLATE =
            "\tpublic void ${name}(${data_type} ${field_name}) {\n" +
            "    \t\tthis.${field_name} = ${field_name};\n" +
            "    }\n" +
            "\n";


}
