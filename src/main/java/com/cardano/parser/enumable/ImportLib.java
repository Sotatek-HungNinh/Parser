package com.cardano.parser.enumable;

public enum ImportLib {
    LIST("List",  "import java.util.List;"),
    SET("Set", "import java.util.Set;"),
    MAP("Map", "import java.util.Map;"),
    DATE("Date", "import java.util.Date;"),
    OPTIONAL("Optional", "import java.util.Optional;"),
    JSON_PROPERTY("JsonProperty", "import com.fasterxml.jackson.annotation.JsonProperty;");

    public String define;
    public String importLib;

    ImportLib(String define, String importLib) {
        this.define = define;
        this.importLib = importLib;
    }

    public static ImportLib getImportLib(String define) {
        ImportLib lib = null;
        for (ImportLib value : ImportLib.values()) {
            if (value.define.equals(define)) {
                lib = value;
                break;
            }
        }
        if (lib == null) {
            throw new IllegalArgumentException("No matching constant for [" + define + "]");
        } else {
            return lib;
        }
    }
}
