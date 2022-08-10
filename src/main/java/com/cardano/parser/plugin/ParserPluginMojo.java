package com.cardano.parser.plugin;

import com.cardano.parser.services.GenerateService;
import lombok.SneakyThrows;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "cddl-parser", defaultPhase = LifecyclePhase.INITIALIZE)
public class ParserPluginMojo extends AbstractMojo {

    @Parameter(property = "file.path", required = true)
    private String cddlFilePath;

    @Parameter(property = "output.folder", required = true)
    private String outputFolder;

    @SneakyThrows
    @Override
    public void execute() {
        getLog().info("Parser maven plugin is running....");
        GenerateService generateService = new GenerateService();
        generateService.generatePojoClass(outputFolder, cddlFilePath);
    }


}
