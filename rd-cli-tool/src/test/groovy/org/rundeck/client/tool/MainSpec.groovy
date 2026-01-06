package org.rundeck.client.tool

import spock.lang.Specification

/**
 * Test for Main class
 * Note: Extension loading security behavior is tested through integration tests
 * and verified by the default value of RD_EXT_DISABLED in Main.java
 */
class MainSpec extends Specification {
    
    def "RD_EXT_DISABLED constant is defined"() {
        expect: "RD_EXT_DISABLED constant exists"
        Main.RD_EXT_DISABLED == "RD_EXT_DISABLED"
    }
    
    def "RD_EXT_DIR constant is defined"() {
        expect: "RD_EXT_DIR constant exists"
        Main.RD_EXT_DIR == "RD_EXT_DIR"
    }
}

