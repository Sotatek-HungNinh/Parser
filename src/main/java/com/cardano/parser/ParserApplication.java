package com.cardano.parser;


import com.cardano.parser.services.GenerateService;

public class ParserApplication {

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            String filePath = args[0];  // example.cddl
            String outputFolder = args[1];

            GenerateService generateService = new GenerateService();
            generateService.generatePojoClass(outputFolder, filePath);
        } else {
            System.out.println("Missing 2 args filePath and outputFolder");
        }
    }

}
